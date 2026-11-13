/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.lang.System.Logger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStopping;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Produces a {@link DeploymentContainerRegistry} during arquillian initialisation.
 * @author Paul Ferraro
 */
public class DeploymentContainerRegistryProducer {

	@Inject
	private Instance<ContainerContext> context;

	@Inject
	private Instance<ServiceLoader> loader;

	@ApplicationScoped
	@Inject
	private InstanceProducer<CloseableDeploymentContainerRegistry> producer;

	/** Default constructor */
	public DeploymentContainerRegistryProducer() {
		// Do nothing
	}

	/**
	 * Observes production of a {@link ContainerRegistry}.
	 * @param registry a container registry
	 */
	public void whenProduced(@Observes ContainerRegistry registry) {
		ServiceLoader loader = Objects.requireNonNull(this.loader.get());
		LifecycleFactory factory = Optional.ofNullable(loader.onlyOne(LifecycleFactory.class)).orElse(LifecycleFactory.DEFAULT);
		ContainerContext context = Objects.requireNonNull(this.context.get());
		this.producer.set(new DeploymentContainerRegistryImpl(registry, factory, context));
	}

	/**
	 * Observes {@link ManagerStopping} events.
	 * @param event an event
	 */
	public void onStop(@SuppressWarnings("unused") @Observes ManagerStopping event) {
		this.producer.get().close();
	}

	static class DeploymentContainerRegistryImpl implements CloseableDeploymentContainerRegistry {
		private final Map<String, DeploymentContainer> containers;
		private final LifecycleFactory factory;

		@SuppressWarnings("unchecked")
		DeploymentContainerRegistryImpl(ContainerRegistry registry, LifecycleFactory factory, ContainerContext context) {
			this.factory = factory;
			this.containers = registry.getContainers().stream().collect(Collectors.toUnmodifiableMap(Container::getName, container -> new DeploymentContainerImpl(container, factory, context)));
		}

		@Override
		public DeploymentContainer getContainer(String name) {
			return this.containers.get(name);
		}

		@Override
		public Set<String> getContainerNames() {
			return Set.copyOf(this.containers.keySet());
		}

		@Override
		public List<DeploymentContainer> getContainers(Set<String> names) {
			return names.stream().sorted().map(this.containers::get).map(Objects::requireNonNull).toList();
		}

		@Override
		public void close() {
			this.factory.close();
		}
	}

	static class DeploymentContainerImpl implements DeploymentContainer {
		private static final System.Logger LOGGER = System.getLogger(DeploymentContainerImpl.class.getName());

		private final Container<ContainerConfiguration> container;
		private final Lifecycle lifecycle;
		private final ContainerContext context;

		@SuppressWarnings("resource")
		DeploymentContainerImpl(Container<ContainerConfiguration> container, LifecycleFactory factory, ContainerContext context) {
			this(container, factory.createContainerLifecycle(container), context);
		}

		DeploymentContainerImpl(Container<ContainerConfiguration> container, Lifecycle lifecycle, ContainerContext context) {
			this.container = container;
			this.lifecycle = lifecycle;
			this.context = context;
			this.context.activate(this.container.getName());
			try {
				this.container.setup();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			} finally {
				this.context.deactivate();
			}
		}

		private void lifecycle(Consumer<Lifecycle> consumer) {
			this.context.activate(this.container.getName());
			try {
				consumer.accept(this.lifecycle);
			} finally {
				this.context.deactivate();
			}
		}

		@Override
		public String getName() {
			return this.container.getName();
		}

		@Override
		public boolean isStarted() {
			return this.lifecycle.isStarted();
		}

		@Override
		public void start() {
			LOGGER.log(Logger.Level.INFO, "Starting container {0}", this.container.getName());
			this.lifecycle(Lifecycle::start);
		}

		@Override
		public void stop() {
			LOGGER.log(Logger.Level.INFO, "Stopping container {0}", this.container.getName());
			this.lifecycle(Lifecycle::stop);
		}

		@Override
		public void close() {
			this.lifecycle(Lifecycle::close);
		}

		@Override
		public String toString() {
			return this.getName();
		}

		@Override
		public int hashCode() {
			return this.getName().hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof DeploymentContainer container)) return false;
			return this.getName().equals(container.getName());
		}

		ProtocolMetaData deployArchive(Archive<?> archive) {
			LOGGER.log(System.Logger.Level.INFO, "Deploying {1} to {0}", this.container.getName(), archive.getName());
			this.context.activate(this.container.getName());
			try {
				return this.container.getDeployableContainer().deploy(archive);
			} catch (DeploymentException e) {
				throw new IllegalStateException(e);
			} finally {
				this.context.deactivate();
			}
		}

		void undeployArchive(Archive<?> archive) {
			LOGGER.log(System.Logger.Level.INFO, "Undeploying {1} from {0}", this.container.getName(), archive.getName());
			this.context.activate(this.container.getName());
			try {
				this.container.getDeployableContainer().undeploy(archive);
			} catch (DeploymentException e) {
				throw new IllegalStateException(e);
			} finally {
				this.context.deactivate();
			}
		}

		@Override
		public Deployment deploy(Archive<?> archive) {
			ProtocolMetaData metaData = this.deployArchive(archive);
			Map<String, URI> uris = new TreeMap<>();
			for (HTTPContext context : metaData.getContexts(HTTPContext.class)) {
				for (Servlet servlet : context.getServlets()) {
					uris.put(servlet.getName(), servlet.getBaseURI());
				}
			}
			LOGGER.log(System.Logger.Level.INFO, "{1} deployed to {0} provides {2}", this.container.getName(), archive.getName(), uris);
			AtomicBoolean started = new AtomicBoolean(true);
			return new Deployment() {
				@Override
				public String getName() {
					return this.getContainer().getName() + ":" + archive.getName();
				}

				@Override
				public boolean isStarted() {
					return started.get();
				}

				@Override
				public void start() {
					if (started.compareAndSet(false, true)) {
						DeploymentContainerImpl.this.deployArchive(archive);
					}
				}

				@Override
				public void stop() {
					if (started.compareAndSet(true, false)) {
						DeploymentContainerImpl.this.undeployArchive(archive);
					}
				}

				@Override
				public URI locate(String resourceName) {
					URI uri = uris.get(resourceName);
					// Workaround buggy container implementations, e.g. open-liberty
					if ((uri == null) && (uris.size() == 1)) {
						uri = uris.values().iterator().next();
					}
					return uri;
				}

				@Override
				public DeploymentContainer getContainer() {
					return DeploymentContainerImpl.this;
				}

				@Override
				public String toString() {
					return this.getName();
				}

				@Override
				public int hashCode() {
					return Objects.hash(this.getContainer(), this.getName());
				}

				@Override
				public boolean equals(Object object) {
					if (!(object instanceof Deployment deployment)) return false;
					return this.getContainer().equals(deployment.getContainer()) && this.getName().equals(deployment.getName());
				}
			};
		}
	}
}
