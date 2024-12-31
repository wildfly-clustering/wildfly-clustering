/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import org.infinispan.commons.dataconversion.ByteArrayWrapper;
import org.infinispan.commons.dataconversion.Encoder;
import org.infinispan.commons.dataconversion.EncoderIds;
import org.infinispan.commons.dataconversion.IdentityEncoder;
import org.infinispan.commons.dataconversion.IdentityWrapper;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.UTF8Encoder;
import org.infinispan.commons.dataconversion.Wrapper;
import org.infinispan.commons.dataconversion.WrapperIds;
import org.infinispan.encoding.DataConversion;

/**
 * Extends Infinispan's {@link EncoderRegistry} adding the ability to unregister transcoders.
 * @author Paul Ferraro
 */
public interface EncoderRegistry extends org.infinispan.marshall.core.EncoderRegistry {

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default Encoder getEncoder(Class<? extends Encoder> encoderClass, short encoderId) {
		switch (encoderId) {
			case EncoderIds.NO_ENCODER:
			case EncoderIds.IDENTITY:
				return IdentityEncoder.INSTANCE;
			case EncoderIds.UTF8:
				return UTF8Encoder.INSTANCE;
			default:
				throw new IllegalArgumentException(Short.toString(encoderId));
		}
	}

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default boolean isRegistered(Class<? extends Encoder> encoderClass) {
		return false;
	}

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default Wrapper getWrapper(Class<? extends Wrapper> wrapperClass, byte wrapperId) {
		switch (wrapperId) {
			case WrapperIds.NO_WRAPPER:
			case WrapperIds.IDENTITY_WRAPPER:
				return IdentityWrapper.INSTANCE;
			case WrapperIds.BYTE_ARRAY_WRAPPER:
				return ByteArrayWrapper.INSTANCE;
			default:
				throw new IllegalArgumentException(Short.toString(wrapperId));
		}
	}

	/**
	 * @param encoder {@link Encoder to be registered}.
	 */
	@Deprecated(forRemoval=true, since = "11.0")
	@Override
	default void registerEncoder(Encoder encoder) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated Since 11.0. To be removed in 14.0, with {@link DataConversion#getWrapper()}
	 */
	@Deprecated(forRemoval=true, since = "11.0")
	@Override
	default void registerWrapper(Wrapper wrapper) {
		throw new UnsupportedOperationException();
	}

	void unregisterTranscoder(MediaType type);
}
