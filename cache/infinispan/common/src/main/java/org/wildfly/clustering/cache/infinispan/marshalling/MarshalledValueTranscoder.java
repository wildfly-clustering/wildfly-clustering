/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling;

import static org.infinispan.commons.logging.Log.CONTAINER;

import java.io.IOException;
import java.util.Set;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.dataconversion.AbstractTranscoder;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.util.Util;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;

/**
 * A transcoder that converts between an object and a {@link MarshalledValue}.
 * @param <C> the marshalling context type
 * @author Paul Ferraro
 */
public class MarshalledValueTranscoder<C> extends AbstractTranscoder {

	private final MarshalledValueFactory<C> factory;
	private final MediaType type;

	public MarshalledValueTranscoder(MediaType type, MarshalledValueFactory<C> factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public Set<MediaType> getSupportedMediaTypes() {
		return Set.of(this.type, MediaType.APPLICATION_OBJECT);
	}

	@Override
	protected Object doTranscode(Object content, MediaType contentType, MediaType destinationType) {
		if (contentType.match(destinationType)) {
			return content;
		}
		if (contentType.match(this.type) && destinationType.match(MediaType.APPLICATION_OBJECT)) {
			return this.factory.createMarshalledValue(content);
		}
		if (contentType.match(MediaType.APPLICATION_OBJECT) && destinationType.match(this.type)) {
			@SuppressWarnings("unchecked")
			MarshalledValue<Object, C> value = (MarshalledValue<Object, C>) content;
			try {
				return value.get(this.factory.getMarshallingContext());
			} catch (IOException e) {
				throw new CacheException(e);
			}
		}
		throw CONTAINER.unsupportedConversion(Util.toStr(content), contentType, destinationType);
	}
}
