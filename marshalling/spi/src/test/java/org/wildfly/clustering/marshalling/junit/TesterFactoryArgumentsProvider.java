/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.junit;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * Provides marshalling test method arguments via a {@link TesterFactorySource}.
 * @author Paul Ferraro
 */
public class TesterFactoryArgumentsProvider extends AnnotationBasedArgumentsProvider<TesterFactorySource> {

	@Deprecated
	@Override
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TesterFactorySource annotation) {
		Stream.Builder<Arguments> builder = Stream.builder();
		for (Class<? extends TesterFactory> factoryClass : annotation.value()) {
			Iterator<? extends TesterFactory> factories = ServiceLoader.load(factoryClass, factoryClass.getClassLoader()).iterator();
			if (!factories.hasNext()) {
				throw new ServiceConfigurationError(factoryClass.getName());
			}
			while (factories.hasNext()) {
				builder.accept(Arguments.of(factories.next()));
			}
		}
		return builder.build();
	}

	@Override
	protected Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context, TesterFactorySource annotation) {
		return this.provideArguments(context, annotation);
	}
}
