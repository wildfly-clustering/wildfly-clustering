/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import static org.infinispan.util.logging.Log.CONTAINER;

import java.util.Set;

import org.infinispan.commons.dataconversion.ByteArrayWrapper;
import org.infinispan.commons.dataconversion.Encoder;
import org.infinispan.commons.dataconversion.EncoderIds;
import org.infinispan.commons.dataconversion.IdentityEncoder;
import org.infinispan.commons.dataconversion.IdentityWrapper;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.Transcoder;
import org.infinispan.commons.dataconversion.UTF8Encoder;
import org.infinispan.commons.dataconversion.Wrapper;
import org.infinispan.commons.dataconversion.WrapperIds;
import org.infinispan.encoding.DataConversion;

/**
 * Extends Infinispan's {@link EncoderRegistry} adding the ability to unregister transcoders.
 * @author Paul Ferraro
 */
public interface EncoderRegistry extends org.infinispan.marshall.core.EncoderRegistry {

	/**
	 * Unregisters the transcoding support for the specified media type.
	 * @param type a media type
	 */
	void unregisterTranscoder(MediaType type);

	@Override
	default Object convert(Object object, MediaType fromType, MediaType toType) {
		if (object == null) return null;
		Transcoder transcoder = this.getTranscoder(fromType, toType);
		if (transcoder == null) {
			throw CONTAINER.cannotFindTranscoder(fromType, toType);
		}
		return transcoder.transcode(object, fromType, toType);
	}

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default Encoder getEncoder(Class<? extends Encoder> encoderClass, short encoderId) {
		return switch (encoderId) {
			case EncoderIds.IDENTITY -> IdentityEncoder.INSTANCE;
			case EncoderIds.UTF8 -> UTF8Encoder.INSTANCE;
			case EncoderIds.NO_ENCODER -> (encoderClass != null) ? Set.of(IdentityEncoder.INSTANCE, UTF8Encoder.INSTANCE).stream().filter(encoder -> encoder.getClass().equals(encoderClass)).findFirst().orElseThrow(() -> new IllegalArgumentException(encoderClass.getName())) : null;
			default -> throw new IllegalArgumentException(Short.toString(encoderId));
		};
	}

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default boolean isRegistered(Class<? extends Encoder> encoderClass) {
		return false;
	}

	@Deprecated(forRemoval = true, since = "11.0")
	@Override
	default Wrapper getWrapper(Class<? extends Wrapper> wrapperClass, byte wrapperId) {
		return switch (wrapperId) {
			case WrapperIds.IDENTITY_WRAPPER -> IdentityWrapper.INSTANCE;
			case WrapperIds.BYTE_ARRAY_WRAPPER -> ByteArrayWrapper.INSTANCE;
			case WrapperIds.NO_WRAPPER -> (wrapperClass != null) ? Set.of(IdentityWrapper.INSTANCE, ByteArrayWrapper.INSTANCE).stream().filter(wrapper -> wrapper.getClass().equals(wrapperClass)).findFirst().orElseThrow(() -> new IllegalArgumentException(wrapperClass.getName())) : null;
			default -> throw new IllegalArgumentException(Byte.toString(wrapperId));
		};
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
}
