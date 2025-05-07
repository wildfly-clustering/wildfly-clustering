/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import static org.infinispan.util.logging.Log.CONTAINER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.Transcoder;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.wildfly.clustering.function.Predicate;

/**
 * Custom {@link EncoderRegistry} that supports transcoder removal.
 * @author Paul Ferraro
 */
@Scope(Scopes.GLOBAL)
public class DefaultEncoderRegistry implements EncoderRegistry {
	private final List<Transcoder> transcoders = Collections.synchronizedList(new ArrayList<>());
	private final Map<MediaType, Map<MediaType, Transcoder>> transcoderCache = new ConcurrentHashMap<>();

	@Override
	public void registerTranscoder(Transcoder transcoder) {
		this.transcoders.add(transcoder);
	}

	@Override
	public Transcoder getTranscoder(MediaType fromType, MediaType toType) {
		Transcoder transcoder = this.findTranscoder(fromType, toType);
		if (transcoder == null) {
			throw CONTAINER.cannotFindTranscoder(fromType, toType);
		}
		return transcoder;
	}

	@Override
	public <T extends Transcoder> T getTranscoder(Class<T> targetClass) {
		return targetClass.cast(this.transcoders.stream().filter(Predicate.<Class<?>>same(targetClass).map(Object::getClass)).findAny().orElse(null));
	}

	@Override
	public boolean isConversionSupported(MediaType fromType, MediaType toType) {
		return fromType.match(toType) || this.findTranscoder(fromType, toType) != null;
	}

	@Override
	public Object convert(Object object, MediaType fromType, MediaType toType) {
		if (object == null) return null;
		return this.getTranscoder(fromType, toType).transcode(object, fromType, toType);
	}

	@Override
	public void unregisterTranscoder(MediaType type) {
		synchronized (this.transcoders) {
			Iterator<Transcoder> transcoders = this.transcoders.iterator();
			while (transcoders.hasNext()) {
				if (transcoders.next().getSupportedMediaTypes().contains(type)) {
					transcoders.remove();
				}
			}
		}
		this.transcoderCache.remove(type);
		for (Map<MediaType, Transcoder> map : this.transcoderCache.values()) {
			map.remove(type);
		}
	}

	private Transcoder findTranscoder(MediaType fromType, MediaType toType) {
		return this.transcoderCache.computeIfAbsent(fromType, mt -> new ConcurrentHashMap<>(4)).computeIfAbsent(toType, mt -> this.transcoders.stream().filter(t -> t.supportsConversion(fromType, toType)).findFirst().orElse(null));
	}
}
