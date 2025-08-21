/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

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
public class DefaultEncoderRegistry implements EncoderRegistry, Function<Map.Entry<MediaType, MediaType>, Transcoder> {
	private final List<Transcoder> transcoders;
	private final Map<Map.Entry<MediaType, MediaType>, Transcoder> types = new ConcurrentHashMap<>();

	DefaultEncoderRegistry(Collection<Transcoder> transcoders) {
		this.transcoders = new CopyOnWriteArrayList<>(transcoders);
	}

	@Override
	public void registerTranscoder(Transcoder transcoder) {
		this.transcoders.add(transcoder);
	}

	@Override
	public Transcoder getTranscoder(MediaType fromType, MediaType toType) {
		return this.findTranscoder(fromType, toType);
	}

	@Override
	public <T extends Transcoder> T getTranscoder(Class<T> targetClass) {
		return targetClass.cast(this.transcoders.stream().filter(Predicate.<Class<?>>identicalTo(targetClass).compose(Object::getClass)).findFirst().orElse(null));
	}

	@Override
	public boolean isConversionSupported(MediaType fromType, MediaType toType) {
		return this.types.containsKey(Map.entry(fromType, toType)) || this.transcoders.stream().anyMatch(transcoder -> transcoder.supportsConversion(fromType, toType));
	}

	@Override
	public void unregisterTranscoder(MediaType type) {
		List<Transcoder> supporting = this.transcoders.stream().filter(transcoder -> transcoder.supports(type)).toList();
		if (!supporting.isEmpty()) {
			this.transcoders.removeAll(supporting);
		}
		for (Map.Entry<MediaType, MediaType> entry : this.types.keySet()) {
			if (entry.getKey().equals(type) || entry.getValue().equals(type)) {
				this.types.remove(entry);
			}
		}
	}

	@Override
	public Transcoder apply(Map.Entry<MediaType, MediaType> entry) {
		return this.transcoders.stream().filter(transcoder -> transcoder.supportsConversion(entry.getKey(), entry.getValue())).findFirst().orElse(null);
	}

	private Transcoder findTranscoder(MediaType fromType, MediaType toType) {
		return this.types.computeIfAbsent(Map.entry(fromType, toType), this);
	}
}
