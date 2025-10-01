/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.attributes.coarse;

import java.io.NotSerializableException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.wildfly.clustering.session.cache.attributes.AbstractSessionAttributes;

/**
 * Exposes session attributes for a coarse granularity session.
 * @author Paul Ferraro
 */
public class CoarseSessionAttributes extends AbstractSessionAttributes {
	private final Map<String, Object> attributes;
	private final Runnable mutator;
	private final Predicate<Object> marshallable;
	private final Predicate<Object> immutable;
	private final SessionActivationNotifier notifier;
	private final AtomicBoolean dirty = new AtomicBoolean(false);

	/**
	 * Creates a coarse-granularity session attributes object.
	 * @param attributes a map of session attributes
	 * @param mutator a mutator for associated cache entry.
	 * @param marshallable a predicate used to determine whether a given session attribute is marshallable.
	 * @param immutable a predicate used to determine whether a given session attribute is immutable.
	 * @param notifier a notifier of session activation/passivation
	 */
	public CoarseSessionAttributes(Map<String, Object> attributes, Runnable mutator, Predicate<Object> marshallable, Predicate<Object> immutable, SessionActivationNotifier notifier) {
		super(attributes);
		this.attributes = attributes;
		this.mutator = mutator;
		this.marshallable = marshallable;
		this.immutable = immutable;
		this.notifier = notifier;
		if (this.notifier != null) {
			this.notifier.postActivate();
		}
	}

	@Override
	public Object remove(Object key) {
		Object value = this.attributes.remove(key);
		if (value != null) {
			this.dirty.set(true);
		}
		return value;
	}

	@Override
	public Object put(String key, Object value) {
		if (value == null) {
			return this.remove(key);
		}
		if (!this.marshallable.test(value)) {
			throw new IllegalArgumentException(new NotSerializableException(value.getClass().getName()));
		}
		Object old = this.attributes.put(key, value);
		// Always trigger mutation, even if this is an immutable object that was previously retrieved via getAttribute(...)
		this.dirty.set(true);
		return old;
	}

	@Override
	public Object get(Object key) {
		Object value = this.attributes.get(key);
		if (value != null) {
			// Bypass immutability check if session is already dirty
			if (!this.dirty.get() && !this.immutable.test(value)) {
				this.dirty.set(true);
			}
		}
		return value;
	}

	@Override
	public void close() {
		if (this.notifier != null) {
			this.notifier.prePassivate();
		}
		if (this.dirty.compareAndSet(true, false)) {
			this.mutator.run();
		}
	}
}
