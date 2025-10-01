/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;

import org.jboss.marshalling.Marshaller;

/**
 * Encapsulates a writable object.
 * @author Paul Ferraro
 * @param <T> the writable object type
 */
public interface Writable<T> {
	/**
	 * Writes the specified object to the specified marshaller.
	 * @param marshaller a marshaller
	 * @param object the object to be written
	 * @throws IOException if the object could not be written.
	 */
	void write(Marshaller marshaller, T object) throws IOException;
}
