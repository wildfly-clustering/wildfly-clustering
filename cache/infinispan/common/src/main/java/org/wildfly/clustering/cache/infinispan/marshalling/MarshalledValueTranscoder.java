/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling;

import static org.infinispan.commons.logging.Log.CONTAINER;

import java.io.IOException;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.OneToManyTranscoder;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.Util;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;

/**
 * A transcoder that converts between an object and a {@link MarshalledValue}.
 * @param <C> the marshalling context type
 * @author Paul Ferraro
 */
public class MarshalledValueTranscoder<C> extends OneToManyTranscoder {

	private final MarshalledValueFactory<C> factory;
	private final Marshaller marshaller;

	public MarshalledValueTranscoder(MediaType type, MarshalledValueFactory<C> factory, Marshaller marshaller) {
		super(type, MediaType.APPLICATION_OBJECT, MediaType.APPLICATION_OCTET_STREAM);
		this.factory = factory;
		this.marshaller = marshaller;
	}

	@Override
	protected Object doTranscode(Object content, MediaType contentType, MediaType destinationType) {
		if (contentType.match(destinationType)) {
			return content;
		}
		if (contentType.match(this.mainType)) {
			MarshalledValue<Object, C> value = this.factory.createMarshalledValue(content);
			if (destinationType.match(MediaType.APPLICATION_OBJECT)) {
				return value;
			}
			if (destinationType.match(MediaType.APPLICATION_OCTET_STREAM)) {
				try {
					return this.marshaller.objectToByteBuffer(value);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw CONTAINER.errorTranscoding(Util.toStr(content), contentType, destinationType, e);
				} catch (IOException e) {
					throw CONTAINER.errorTranscoding(Util.toStr(content), contentType, destinationType, e);
				}
			}
		} else if (destinationType.match(this.mainType)) {
			if (contentType.match(MediaType.APPLICATION_OBJECT) || contentType.match(MediaType.APPLICATION_OCTET_STREAM)) {
				try {
					Object object = contentType.match(MediaType.APPLICATION_OCTET_STREAM) ? this.marshaller.objectFromByteBuffer((byte[]) content) : content;
					@SuppressWarnings("unchecked")
					MarshalledValue<Object, C> value = (MarshalledValue<Object, C>) object;
					return value.get(this.factory.getMarshallingContext());
				} catch (IOException | ClassNotFoundException e) {
					throw CONTAINER.errorTranscoding(Util.toStr(content), contentType, destinationType, e);
				}
			}
		}
		throw CONTAINER.unsupportedConversion(Util.toStr(content), contentType, destinationType);
	}
}
