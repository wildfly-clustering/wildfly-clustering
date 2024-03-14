/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;

/**
 * Offers semantics similar to a {@link java.rmi.MarshalledObject#get()}, but supports an independent marshalling context.
 * @author Paul Ferraro
 * @param <T> value type
 * @param <C> marshalling context type
 */
public interface MarshalledValue<T, C> {
	/**
	 * Returns the value, unmarshalling using the specified context if necessary.
	 * @param context a marshalling context
	 * @return the value wrapped by this marshalled value.
	 * @throws IOException if the value could not be unmarshalled.
	 */
	T get(C context) throws IOException;
}
