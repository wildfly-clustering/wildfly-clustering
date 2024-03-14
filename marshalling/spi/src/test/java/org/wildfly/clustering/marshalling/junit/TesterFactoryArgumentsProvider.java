/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.junit;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * @author Paul Ferraro
 */
public class TesterFactoryArgumentsProvider extends AnnotationBasedArgumentsProvider<TesterFactorySource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TesterFactorySource annotation) {
		Stream.Builder<Arguments> builder = Stream.builder();
		for (Class<? extends TesterFactory> factoryClass : annotation.value()) {
			Iterator<? extends TesterFactory> factories = ServiceLoader.load(factoryClass, factoryClass.getClassLoader()).iterator();
			if (!factories.hasNext()) {
				throw new ServiceConfigurationError(factoryClass.getName());
			}
			while (factories.hasNext()) {
				TesterFactory factory = factories.next();
				builder.accept(Arguments.of(new TesterFactory() {
					@Override
					public <T> Tester<T> createTester(BiConsumer<T, T> assertion) {
						return factory.createTester(assertion);
					}

					@Override
					public String toString() {
						return factory.toString();
					}
				}));
			}
		}
		return builder.build();
	}
}
