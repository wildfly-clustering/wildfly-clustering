/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * Defines the tester factory parameter of a marshalling test method.
 * @author Paul Ferraro
 */
@Retention(RUNTIME)
@Target(METHOD)
@ArgumentsSource(TesterFactoryArgumentsProvider.class)
public @interface TesterFactorySource {

	Class<? extends TesterFactory>[] value() default MarshallingTesterFactory.class;
}
