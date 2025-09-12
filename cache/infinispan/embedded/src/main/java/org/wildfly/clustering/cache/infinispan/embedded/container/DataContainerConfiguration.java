/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiPredicate;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.configuration.attributes.IdentityAttributeCopier;
import org.infinispan.commons.configuration.attributes.Matchable;
import org.infinispan.configuration.cache.MemoryConfiguration;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.factories.impl.ComponentRef;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Weigher;

/**
 * Configuration of a Caffeine-based data container.
 * @author Paul Ferraro
 */
@BuiltBy(DataContainerConfigurationBuilder.class)
public class DataContainerConfiguration implements Matchable<DataContainerConfiguration> {
	private static final Expiry<Object, Object> NEVER = new Expiry<>() {
		@Override
		public long expireAfterCreate(Object key, Object value, long currentTime) {
			return Long.MAX_VALUE;
		}

		@Override
		public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
			return currentDuration;
		}

		@Override
		public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
			return currentDuration;
		}
	};

	@SuppressWarnings("rawtypes")
	static final AttributeDefinition<Weigher> WEIGHER = AttributeDefinition.builder("weigher", Weigher.singletonWeigher(), Weigher.class)
			.copier(IdentityAttributeCopier.identityCopier())
			.immutable()
			.build();

	@SuppressWarnings("rawtypes")
	static final AttributeDefinition<Expiry> EXPIRY = AttributeDefinition.builder("expiry", NEVER, Expiry.class)
			.copier(IdentityAttributeCopier.identityCopier())
			.immutable()
			.build();

	private final AttributeSet attributes;

	DataContainerConfiguration(AttributeSet attributes) {
		this.attributes = attributes;
	}

	AttributeSet attributes() {
		return this.attributes;
	}

	public <K, V> Caffeine<K, V> builder(BasicComponentRegistry registry, MemoryConfiguration memory) {
		@SuppressWarnings("unchecked")
		Caffeine<K, V> builder = (Caffeine<K, V>) Caffeine.newBuilder();
		Weigher<K, V> weigher = this.attributes.attribute(WEIGHER).get();
		Expiry<K, V> expiry = this.attributes.attribute(EXPIRY).get();
		if (expiry != NEVER) {
			Scheduler scheduler = Optional.ofNullable(registry.getComponent(KnownComponentNames.EXPIRATION_SCHEDULED_EXECUTOR, ScheduledExecutorService.class))
					.map(ComponentRef::running)
					.map(Scheduler::forScheduledExecutorService)
					.orElse(Scheduler.systemScheduler());
			// If a weigher was defined, ensure we exclude entries with no weight
			builder.expireAfter((weigher != Weigher.singletonWeigher()) ? new Expiry<K, V>() {
				private final BiPredicate<K, V> expirable = new BiPredicate<>() {
					@Override
					public boolean test(K key, V value) {
						return weigher.weigh(key, value) > 0;
					}
				};

				@Override
				public long expireAfterCreate(K key, V value, long currentTime) {
					return this.expirable.test(key, value) ? expiry.expireAfterCreate(key, value, currentTime) : Long.MAX_VALUE;
				}

				@Override
				public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
					return this.expirable.test(key, value) ? expiry.expireAfterUpdate(key, value, currentTime, currentDuration) : currentDuration;
				}

				@Override
				public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
					return this.expirable.test(key, value) ? expiry.expireAfterRead(key, value, currentTime, currentDuration) : currentDuration;
				}
			} : expiry).scheduler(scheduler);
		}
		if (memory.maxCount() > 0) {
			if (weigher != Weigher.singletonWeigher()) {
				builder.maximumWeight(memory.maxCount()).weigher(weigher);
			} else {
				builder.maximumSize(memory.maxCount());
			}
		}
		return builder;
	}

	@Override
	public boolean matches(DataContainerConfiguration configuration) {
		return this.attributes.matches(configuration.attributes);
	}
}
