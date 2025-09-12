/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.util.EntrySizeCalculator;
import org.infinispan.configuration.cache.ConfigurationBuilder;

import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Weigher;

/**
 * Builder of a {@link DataContainerConfiguration}.
 * @author Paul Ferraro
 */
public class DataContainerConfigurationBuilder implements Builder<DataContainerConfiguration> {

	private final AttributeSet attributes;

	public DataContainerConfigurationBuilder(ConfigurationBuilder builder) {
		this();
	}

	DataContainerConfigurationBuilder() {
		this.attributes = new AttributeSet(DataContainerConfiguration.class, DataContainerConfiguration.WEIGHER, DataContainerConfiguration.EXPIRY);
	}

	public <K> DataContainerConfigurationBuilder evictableWhen(Predicate<K> evictable) {
		return this.withWeigher(new Weigher<K, Object>() {
			@Override
			public int weigh(K key, Object value) {
				return evictable.test(key) ? 1 : 0;
			}
		});
	}

	public <K, V> DataContainerConfigurationBuilder evictableWhen(BiPredicate<K, V> evictable) {
		return this.withWeigher(new Weigher<K, V>() {
			@Override
			public int weigh(K key, V value) {
				return evictable.test(key, value) ? 1 : 0;
			}
		});
	}

	public <K, V> DataContainerConfigurationBuilder withWeight(ToIntBiFunction<K, V> weight) {
		Weigher<K, V> weigher = weight::applyAsInt;
		return this.withWeigher(weigher);
	}

	public <K, V> DataContainerConfigurationBuilder withWeight(EntrySizeCalculator<K, V> calculator) {
		return this.withWeigher(new Weigher<K, V>() {
			@Override
			public int weigh(K key, V value) {
				return (int) Math.min(calculator.calculateSize(key, value), (long) Integer.MAX_VALUE);
			}
		});
	}

	private <K, V> DataContainerConfigurationBuilder withWeigher(Weigher<K, V> weigher) {
		this.attributes.attribute(DataContainerConfiguration.WEIGHER).set(weigher);
		return this;
	}

	public DataContainerConfigurationBuilder evictAfter(Duration maxIdle) {
		return this.evictAfter(new BiFunction<>() {
			@Override
			public Duration apply(Object key, Object value) {
				return maxIdle;
			}
		});
	}

	public <V> DataContainerConfigurationBuilder evictAfter(Function<V, Duration> maxIdle) {
		return this.evictAfter(new BiFunction<Object, V, Duration>() {
			@Override
			public Duration apply(Object key, V value) {
				return maxIdle.apply(value);
			}
		});
	}

	public <K, V> DataContainerConfigurationBuilder evictAfter(BiFunction<K, V, Duration> duration) {
		this.attributes.attribute(DataContainerConfiguration.EXPIRY).set(new Expiry<K, V>() {
			@Override
			public long expireAfterCreate(K key, V value, long currentTime) {
				return duration.apply(key, value).toNanos();
			}

			@Override
			public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
				return duration.apply(key, value).toNanos();
			}

			@Override
			public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
				return duration.apply(key, value).toNanos();
			}
		});
		return this;
	}

	@Override
	public void validate() {
	}

	@Override
	public DataContainerConfiguration create() {
		return new DataContainerConfiguration(this.attributes);
	}

	@Override
	public DataContainerConfigurationBuilder read(DataContainerConfiguration template, Combine combine) {
		this.attributes.read(template.attributes(), combine);
		return this;
	}

	@Override
	public AttributeSet attributes() {
		return this.attributes;
	}
}
