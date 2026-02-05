/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian.extension;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;

/**
 * Exposes {@link DeploymentContainerRegistry} as an Arquillian resource.
 * @author Paul Ferraro
 */
public class DeploymentContainerRegistryResourceProvider implements ResourceProvider {
	private static final System.Logger LOGGER = System.getLogger(DeploymentContainerRegistryResourceProvider.class.getName());

	@Inject
	private Instance<ContainerRegistry> registry;

	/**
	 * Constructs a new resource provider of a deployment container registry.
	 */
	public DeploymentContainerRegistryResourceProvider() {
		// Do nothing
	}

	@Override
	public boolean canProvide(Class<?> type) {
		return type.isAssignableFrom(DeploymentContainerRegistry.class);
	}

	@Override
	public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
		return new WebContainerRegistryImpl(this.registry.get());
	}

	static class WebContainerRegistryImpl implements DeploymentContainerRegistry {
		private final Map<String, DeploymentContainer> containers;

		WebContainerRegistryImpl(ContainerRegistry registry) {
			this.containers = registry.getContainers().stream().collect(Collectors.toUnmodifiableMap(Container::getName, WebContainerImpl::new));
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
	}

	static class WebContainerImpl implements DeploymentContainer {
		private final Container<ContainerConfiguration> container;

		WebContainerImpl(Container<ContainerConfiguration> container) {
			this.container = container;
		}

		@Override
		public String getName() {
			return this.container.getName();
		}

		@Override
		public boolean isStarted() {
			return this.container.getState() == Container.State.STARTED;
		}

		@Override
		public void start() {
			try {
				LOGGER.log(System.Logger.Level.INFO, "Starting {0}", this.container.getName());
				this.container.start();
			} catch (LifecycleException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void stop() {
			try {
				LOGGER.log(System.Logger.Level.INFO, "Stopping {0}", this.container.getName());
				this.container.stop();
			} catch (LifecycleException e) {
				throw new IllegalStateException(e);
			}
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
			try {
				LOGGER.log(System.Logger.Level.INFO, "Deploying {0} to {1}", archive.getName(), WebContainerImpl.this.container.getName());
				return WebContainerImpl.this.container.getDeployableContainer().deploy(archive);
			} catch (DeploymentException e) {
				throw new IllegalStateException(e);
			}
		}

		void undeployArchive(Archive<?> archive) {
			try {
				LOGGER.log(System.Logger.Level.INFO, "Undeploying {0} from {1}", archive.getName(), this.container.getName());
				WebContainerImpl.this.container.getDeployableContainer().undeploy(archive);
			} catch (DeploymentException e) {
				throw new IllegalStateException(e);
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
						WebContainerImpl.this.deployArchive(archive);
					}
				}

				@Override
				public void stop() {
					if (started.compareAndSet(true, false)) {
						WebContainerImpl.this.undeployArchive(archive);
					}
				}

				@Override
				public URI locate(String resourceName) {
					return uris.get(resourceName);
				}

				@Override
				public DeploymentContainer getContainer() {
					return WebContainerImpl.this;
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
