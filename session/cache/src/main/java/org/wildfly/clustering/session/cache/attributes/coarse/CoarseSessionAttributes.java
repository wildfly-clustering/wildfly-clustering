/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.attributes.coarse;

import java.io.NotSerializableException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.marshalling.Marshallability;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SimpleImmutableSessionAttributes;

/**
 * Exposes session attributes for a coarse granularity session.
 * @author Paul Ferraro
 */
public class CoarseSessionAttributes extends SimpleImmutableSessionAttributes implements SessionAttributes {
	private final Map<String, Object> attributes;
	private final CacheEntryMutator mutator;
	private final Marshallability marshallability;
	private final Immutability immutability;
	private final SessionActivationNotifier notifier;
	private final AtomicBoolean dirty = new AtomicBoolean(false);

	public CoarseSessionAttributes(Map<String, Object> attributes, CacheEntryMutator mutator, Marshallability marshallability, Immutability immutability, SessionActivationNotifier notifier) {
		super(attributes);
		this.attributes = attributes;
		this.mutator = mutator;
		this.marshallability = marshallability;
		this.immutability = immutability;
		this.notifier = notifier;
		if (this.notifier != null) {
			this.notifier.postActivate();
		}
	}

	@Override
	public Object removeAttribute(String name) {
		Object value = this.attributes.remove(name);
		if (value != null) {
			this.dirty.set(true);
		}
		return value;
	}

	@Override
	public Object setAttribute(String name, Object value) {
		if (value == null) {
			return this.removeAttribute(name);
		}
		if (!this.marshallability.isMarshallable(value)) {
			throw new IllegalArgumentException(new NotSerializableException(value.getClass().getName()));
		}
		Object old = this.attributes.put(name, value);
		// Always trigger mutation, even if this is an immutable object that was previously retrieved via getAttribute(...)
		this.dirty.set(true);
		return old;
	}

	@Override
	public Object getAttribute(String name) {
		Object value = this.attributes.get(name);
		if (value != null) {
			if (!this.immutability.test(value)) {
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
			this.mutator.mutate();
		}
	}
}
