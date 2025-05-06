/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Map;
import java.util.TreeMap;

import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.server.util.ReferenceMap;
import org.wildfly.clustering.session.cache.attributes.AbstractSessionAttributes;

/**
 * Exposes session attributes for a fine granularity sessions.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class FineSessionAttributes<K, V> extends AbstractSessionAttributes {

	private final K key;
	private final Map<String, Object> attributes;
	private final Marshaller<Object, V> marshaller;
	private final CacheEntryMutatorFactory<K, Map<String, V>> mutatorFactory;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final SessionAttributeActivationNotifier notifier;
	private final ReferenceMap<String, Object> updates = ReferenceMap.of(new TreeMap<>());

	public FineSessionAttributes(K key, Map<String, Object> attributes, CacheEntryMutatorFactory<K, Map<String, V>> mutatorFactory, Marshaller<Object, V> marshaller, Immutability immutability, CacheProperties properties, SessionAttributeActivationNotifier notifier) {
		super(attributes);
		this.key = key;
		this.attributes = attributes;
		this.mutatorFactory = mutatorFactory;
		this.marshaller = marshaller;
		this.immutability = immutability;
		this.properties = properties;
		this.notifier = notifier;

		if (this.notifier != null) {
			for (Object value : this.attributes.values()) {
				this.notifier.postActivate(value);
			}
		}
	}

	@Override
	public Object get(Object key) {
		if (!(key instanceof String)) return null;
		String name = (String) key;
		Object value = this.attributes.get(name);

		if (value != null) {
			// If the object is mutable, we need to mutate this value on close
			// Bypass immutability check if attribute already updates on close
			this.updates.reference(name).writer(value).when(v -> (v == null) && !this.immutability.test(value)).get();
		}

		return value;
	}

	@Override
	public Object remove(Object key) {
		if (!(key instanceof String)) return null;
		String name = (String) key;
		Object result = this.attributes.remove(name);

		if (result != null) {
			this.updates.reference(name).writer(Supplier.of(null)).get();
		}

		return result;
	}

	@Override
	public Object put(String name, Object value) {
		if (value == null) {
			return this.remove(name);
		}

		if (this.properties.isMarshalling() && !this.marshaller.isMarshallable(value)) {
			throw new IllegalArgumentException(new NotSerializableException(value.getClass().getName()));
		}

		Object result = this.attributes.put(name, value);

		// Always trigger attribute update, even if called with an existing reference
		this.updates.reference(name).writer(value).get();
		return result;
	}

	@Override
	public void close() {
		if (this.notifier != null) {
			for (Object value : this.attributes.values()) {
				this.notifier.prePassivate(value);
			}
		}
		this.updates.reader().consume(map -> {
			if (!map.isEmpty()) {
				Map<String, V> updates = new TreeMap<>();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String name = entry.getKey();
					Object value = entry.getValue();
					updates.put(name, (value != null) ? this.write(value) : null);
				}
				this.mutatorFactory.createMutator(this.key, updates).mutate();
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
