/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.attributes.coarse;

import java.io.NotSerializableException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wildfly.clustering.function.BooleanSupplier;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.util.BlockingMapReference;
import org.wildfly.clustering.session.cache.attributes.AbstractSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributeActivationNotifier;

/**
 * Exposes session attributes for a coarse granularity session.
 * @author Paul Ferraro
 */
public class CoarseSessionAttributes extends AbstractSessionAttributes {
	private final BlockingMapReference<String, Object> attributes;
	private final Runnable mutator;
	private final java.util.function.Predicate<Object> marshallable;
	private final java.util.function.Predicate<Object> mutable;
	private final SessionAttributeActivationNotifier notifier;
	private final BooleanSupplier isDirty;
	private final Runner setDirty;
	private final UnaryOperator<Object> setDirtyOnMutableRead;
	private final UnaryOperator<Object> setDirtyOnRemove;

	/**
	 * Creates a coarse-granularity session attributes object.
	 * @param attributes a map of session attributes
	 * @param mutator a mutator for associated cache entry.
	 * @param marshallable a predicate used to determine whether a given session attribute is marshallable.
	 * @param immutable a predicate used to determine whether a given session attribute is immutable.
	 * @param notifier a notifier of session activation/passivation
	 */
	public CoarseSessionAttributes(Map<String, Object> attributes, Runnable mutator, java.util.function.Predicate<Object> marshallable, java.util.function.Predicate<Object> immutable, SessionAttributeActivationNotifier notifier) {
		this(BlockingMapReference.of(attributes), mutator, marshallable, immutable, notifier);
		attributes.values().forEach(this.notifier::postActivate);
	}

	private CoarseSessionAttributes(BlockingMapReference<String, Object> attributes, Runnable mutator, java.util.function.Predicate<Object> marshallable, java.util.function.Predicate<Object> immutable, SessionAttributeActivationNotifier notifier) {
		super(attributes);
		this.attributes = attributes;
		this.marshallable = marshallable;
		this.notifier = notifier;
		AtomicBoolean dirty = new AtomicBoolean(false);
		this.mutator = mutator;
		this.isDirty = dirty::get;
		this.setDirty = BooleanSupplier.of(true).thenAccept(dirty::set);
		// Bypass immutability check if session is already dirty
		this.mutable = Predicate.and(Predicate.and(Objects::nonNull, Predicate.of(Consumer.of(), this.isDirty).negate()), Predicate.not(immutable));
		this.setDirtyOnMutableRead = UnaryOperator.when(Objects::nonNull, UnaryOperator.identity().thenRun(this.setDirty), UnaryOperator.identity());
		this.setDirtyOnRemove = UnaryOperator.when(Objects::nonNull, UnaryOperator.of(null).compose(this.setDirty), UnaryOperator.of(null));
	}

	@Override
	public Object get(Object key) {
		if (!(key instanceof String name)) return null;

		// If the object is mutable, we need to mutate this value on close
		return this.attributes.getReference(name).getWriter(this.mutable).getAndUpdate(this.setDirtyOnMutableRead);
	}

	@Override
	public Object remove(Object key) {
		if (!(key instanceof String name)) return null;

		return this.attributes.getReference(name).getWriter().getAndUpdate(this.setDirtyOnRemove);
	}

	@Override
	public Object put(String name, Object value) {
		if (value == null) return this.remove(name);

		if (!this.marshallable.test(value)) {
			throw new IllegalArgumentException(new NotSerializableException(value.getClass().getName()));
		}

		// Always mark as dirty, even if called with an existing reference
		return this.attributes.getReference(name).getWriter().getAndSet(this.setDirty.thenReturn(Supplier.of(value)));
	}

	@Override
	public void close() {
		this.attributes.getReader().read(attributes -> {
			attributes.values().forEach(this.notifier::prePassivate);
			if (this.isDirty.getAsBoolean()) {
				this.mutator.run();
			}
		});
	}
}
