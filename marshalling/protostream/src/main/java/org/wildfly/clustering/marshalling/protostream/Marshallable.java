/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.Optional;
import java.util.OptionalInt;

import org.wildfly.clustering.function.BiPredicate;

/**
 * Interface inherited by marshallable components.
 * @author Paul Ferraro
 * @param <T> the type of this marshaller
 */
public interface Marshallable<T> extends Readable<T>, Writable<T>, BiPredicate<ImmutableSerializationContext, T> {

	/**
	 * Computes the size of the specified object.
	 * @param operation the marshalling operation
	 * @param value the value whose size is to be calculated
	 * @return an optional buffer size, only present if the buffer size could be computed
	 */
	default OptionalInt size(ProtoStreamSizeOperation operation, T value) {
		return operation.computeSize(this, value);
	}

	/**
	 * Returns the type of object handled by this marshallable instance.
	 * @return the type of object handled by this marshallable instance.
	 */
	Class<? extends T> getJavaClass();

	@Override
	default boolean test(ImmutableSerializationContext context, T value) {
		return true;
	}

	/**
	 * Returns an optional class, the associated module/package of which, when present, is required to be open to this module.
	 * @return an optional class, the associated module/package of which, when present, is required to be open to this module.
	 */
	default Optional<Class<?>> getOpenClass() {
		return Optional.empty();
	}
}
