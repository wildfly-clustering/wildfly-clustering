/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.util.BlockingMapReference;
import org.wildfly.clustering.session.cache.attributes.AbstractSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributeActivationNotifier;

/**
 * Exposes session attributes for a fine granularity sessions.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class FineSessionAttributes<K, V> extends AbstractSessionAttributes {
	private final K key;
	private final BlockingMapReference<String, Object> attributes;
	private final Marshaller<Object, V> marshaller;
	private final CacheEntryMutatorFactory<K, Map<String, V>> mutatorFactory;
	private final Predicate<Object> mutable;
	private final SessionAttributeActivationNotifier notifier;
	// Guarded by attributes
	private final Map<String, Object> updates = new TreeMap<>();

	/**
	 * Creates a fine-granularity session attributes implementation.
	 * @param key the session attributes cache key
	 * @param attributes a map of session attributes
	 * @param mutatorFactory a factory for creating a mutator of the session attributes cache entry
	 * @param marshaller a marshaller of session attributes
	 * @param immutable a predicate used to determine whether a given session attribute is immutable
	 * @param notifier a notifier of session attribute activation/passivation
	 */
	public FineSessionAttributes(K key, Map<String, Object> attributes, CacheEntryMutatorFactory<K, Map<String, V>> mutatorFactory, Marshaller<Object, V> marshaller, java.util.function.Predicate<Object> immutable, SessionAttributeActivationNotifier notifier) {
		this(key, BlockingMapReference.of(attributes), mutatorFactory, marshaller, immutable, notifier);
		attributes.values().forEach(this.notifier::postActivate);
	}

	private FineSessionAttributes(K key, BlockingMapReference<String, Object> attributes, CacheEntryMutatorFactory<K, Map<String, V>> mutatorFactory, Marshaller<Object, V> marshaller, java.util.function.Predicate<Object> immutable, SessionAttributeActivationNotifier notifier) {
		super(attributes);
		this.key = key;
		this.attributes = attributes;
		this.mutatorFactory = mutatorFactory;
		this.marshaller = marshaller;
		this.mutable = Predicate.and(Objects::nonNull, Predicate.not(immutable));
		this.notifier = notifier;
	}

	@Override
	public Object get(Object key) {
		if (!(key instanceof String name)) return null;

		// If the object is mutable, we need to mutate this value on close
		// Bypass immutability check if attribute already updates on close
		return this.attributes.getReference(name).getWriter(this.mutable).getAndUpdate(value -> {
			if (value != null) {
				this.updates.put(name, value);
			}
			return value;
		});
	}

	@Override
	public Object remove(Object key) {
		if (!(key instanceof String name)) return null;

		return this.attributes.getReference(name).getWriter().getAndUpdate(value -> {
			if (value != null) {
				this.updates.put(name, null);
			}
			return null;
		});
	}

	@Override
	public Object put(String name, Object value) {
		if (value == null) return this.remove(name);

		if (!this.marshaller.test(value)) {
			throw new IllegalArgumentException(new NotSerializableException(value.getClass().getName()));
		}

		// Always trigger attribute update, even if called with an existing reference
		return this.attributes.getReference(name).getWriter().getAndSet(() -> {
			this.updates.put(name, value);
			return value;
		});
	}

	@Override
	public void close() {
		this.attributes.getReader().read(attributes -> {
			attributes.values().forEach(this.notifier::prePassivate);
			if (!this.updates.isEmpty()) {
				Map<String, V> updates = new TreeMap<>();
				for (Map.Entry<String, Object> entry : this.updates.entrySet()) {
					String name = entry.getKey();
					Object value = entry.getValue();
					updates.put(name, (value != null) ? this.write(value) : null);
				}
				this.mutatorFactory.createMutator(this.key, updates).run();
			}
		});
	}

	private V write(Object value) {
		try {
			return this.marshaller.write(value);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
