/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.listener;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;

/**
 * A registry of consumers.
 * @author Paul Ferraro
 * @param <T> the consumed type
 */
public interface ConsumerRegistry<T> extends Registrar<Consumer<T>>, Consumer<T> {
	/**
	 * Creates a new consumer registry.
	 * @param <T> the consumed type
	 * @return a new consumer registry.
	 */
	static <T> ConsumerRegistry<T> newInstance() {
		return newInstance(CopyOnWriteArrayList::new);
	}

	/**
	 * Creates a new consumer registry.
	 * @param <T> the consumed type
	 * @param factory the consumer collection factory
	 * @return a new consumer registry.
	 */
	static <T> ConsumerRegistry<T> newInstance(Supplier<Collection<Consumer<T>>> factory) {
		Collection<Consumer<T>> consumers = factory.get();
		System.Logger logger = System.getLogger(ConsumerRegistry.class.getName());
		return new ConsumerRegistry<>() {

			@Override
			public Registration register(Consumer<T> consumer) {
				consumers.add(consumer);
				return () -> consumers.remove(consumer);
			}

			@Override
			public void accept(T value) {
				for (Consumer<T> consumer : consumers) {
					try {
						consumer.accept(value);
					} catch (Throwable e) {
						logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
					}
				}
			}
		};
	}
}
