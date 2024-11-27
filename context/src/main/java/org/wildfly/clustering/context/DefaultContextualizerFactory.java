/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Default {@link Contextualizer} factory that applies a number of contexts.
 * @author Paul Ferraro
 */
public enum DefaultContextualizerFactory implements ContextualizerFactory {
	INSTANCE;

	private final List<ContextualizerFactory> factories = new LinkedList<>();

	DefaultContextualizerFactory() {
		this.factories.add(new ContextualizerFactory() {
			@Override
			public Contextualizer createContextualizer(ClassLoader loader) {
				return Contextualizer.withContextProvider(ContextClassLoaderReference.INSTANCE.provide(loader));
			}
		});
		AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Void run() {
				ServiceLoader.load(ContextualizerFactory.class, ContextualizerFactory.class.getClassLoader()).forEach(DefaultContextualizerFactory.this.factories::add);
				return null;
			}
		});
	}

	@Override
	public Contextualizer createContextualizer(ClassLoader loader) {
		List<Contextualizer> contextualizers = new ArrayList<>(this.factories.size());
		for (ContextualizerFactory factory : this.factories) {
			contextualizers.add(factory.createContextualizer(loader));
		}
		return Contextualizer.composite(contextualizers);
	}
}
