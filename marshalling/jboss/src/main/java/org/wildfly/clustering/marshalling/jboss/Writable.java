/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;

import org.jboss.marshalling.Marshaller;

/**
 * @author Paul Ferraro
 * @param <T> the target type
 */
public interface Writable<T> {
	void write(Marshaller marshaller, T object) throws IOException;
}
