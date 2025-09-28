/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.offset;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.wildfly.clustering.function.Supplier;

/**
 * Encapsulates a value that is offset from some basis, and updated via {@link OffsetValue#set(Object)}.
 * @author Paul Ferraro
 * @param <V> the value type
 */
public interface OffsetValue<V> extends Value<V> {

	/**
	 * Returns the basis from which the associated offset will be applied.
	 * @return the basis from which the associated offset will be applied.
	 */
	V getBasis();

	/**
	 * Returns the current value, computed by applying the current offset to the basis.
	 * @return the computed value
	 */
	@Override
	default V get() {
		return this.getOffset().apply(this.getBasis());
	}

	/**
	 * The current offset.
	 * @return an offset
	 */
	Offset<V> getOffset();

	/**
	 * Sets the current offset.
	 * @param offset an offset
	 */
	default void setOffset(Offset<V> offset) {
		this.set(offset.apply(this.getBasis()));
	}

	/**
	 * Returns a new offset value based on the current value.
	 * @return a new offset value
	 */
	OffsetValue<V> rebase();

	/**
	 * Creates a duration-based offset value from the specified basis.
	 * @param duration the offset basis
	 * @return a duration-based offset value from the specified basis.
	 */
	static OffsetValue<Duration> from(Duration duration) {
		return new DurationOffsetValue(Supplier.of(duration));
	}

	/**
	 * Creates a instant-based offset value from the specified basis.
	 * @param instant the offset basis
	 * @return a instant-based offset value from the specified basis.
	 */
	static OffsetValue<Instant> from(Instant instant) {
		return new InstantOffsetValue(Supplier.of(instant));
	}

	/**
	 * The default offset value implementation.
	 * @param <O> the offset value type
	 * @param <V> the basis value type
	 */
	class DefaultOffsetValue<O, V> extends AbstractValue<V> implements OffsetValue<V> {
		private final BiFunction<V, V, O> factory;
		private final Function<O, Offset<V>> offsetFactory;
		private final Function<java.util.function.Supplier<V>, OffsetValue<V>> offsetValueFactory;
		private final java.util.function.Supplier<V> basis;
		private final O zero;

		private volatile Offset<V> offset;

		DefaultOffsetValue(java.util.function.Supplier<V> basis, O zero, BiFunction<V, V, O> factory, Function<O, Offset<V>> offsetFactory, Function<java.util.function.Supplier<V>, OffsetValue<V>> offsetValueFactory) {
			this.factory = factory;
			this.offsetFactory = offsetFactory;
			this.offsetValueFactory = offsetValueFactory;
			this.zero = zero;
			this.basis = basis;
			this.offset = this.offsetFactory.apply(zero);
		}

		@Override
		public V getBasis() {
			return this.basis.get();
		}

		@Override
		public void set(V value) {
			V basis = this.getBasis();
			this.offset = this.offsetFactory.apply(Objects.equals(basis, value) ? this.zero : this.factory.apply(basis, value));
		}

		@Override
		public void setOffset(Offset<V> offset) {
			this.offset = offset;
		}

		@Override
		public Offset<V> getOffset() {
			return this.offset;
		}

		@Override
		public OffsetValue<V> rebase() {
			return this.offsetValueFactory.apply(this);
		}
	}

	/**
	 * A temporal offset value implementation.
	 * @param <V> the basis value type
	 */
	class TemporalOffsetValue<V> extends DefaultOffsetValue<Duration, V> {

		TemporalOffsetValue(java.util.function.Supplier<V> basis, BiFunction<V, V, Duration> factory, Function<Duration, Offset<V>> offsetFactory, Function<java.util.function.Supplier<V>, OffsetValue<V>> offsetValueFactory) {
			super(basis, Duration.ZERO, factory, offsetFactory, offsetValueFactory);
		}
	}

	/**
	 * A duration-based offset value implementation.
	 */
	class DurationOffsetValue extends TemporalOffsetValue<Duration> {
		private static final BiFunction<Duration, Duration, Duration> MINUS = Duration::minus;
		private static final BiFunction<Duration, Duration, Duration> FACTORY = MINUS.andThen(Duration::negated);

		DurationOffsetValue(java.util.function.Supplier<Duration> basis) {
			super(basis, FACTORY, Offset::forDuration, DurationOffsetValue::new);
		}
	}

	/**
	 * An instant-based offset value implementation.
	 */
	class InstantOffsetValue extends TemporalOffsetValue<Instant> {
		private static final BiFunction<Instant, Instant, Duration> FACTORY = Duration::between;

		InstantOffsetValue(java.util.function.Supplier<Instant> basis) {
			super(basis, FACTORY, Offset::forInstant, InstantOffsetValue::new);
		}
	}
}
