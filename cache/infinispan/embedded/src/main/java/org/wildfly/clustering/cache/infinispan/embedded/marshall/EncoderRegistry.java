/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import static org.infinispan.util.logging.Log.CONTAINER;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.Transcoder;

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
}
