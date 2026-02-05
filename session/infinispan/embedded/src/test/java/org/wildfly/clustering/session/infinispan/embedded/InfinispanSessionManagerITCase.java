/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.h2.jdbcx.JdbcDataSource;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.persistence.jdbc.common.DatabaseType;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.infinispan.transaction.TransactionMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.persistence.jdbc.DataSourceConnectionFactoryConfigurationBuilder;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.cache.AbstractSessionManagerITCase;

/**
 * Session manager integration test using an embedded Infinispan cache tested under a combination of settings.
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerITCase extends AbstractSessionManagerITCase<InfinispanSessionManagerParameters> {

	static class InfinispanSessionManagerArgumentsProvider implements ArgumentsProvider {
		Class<? extends MarshallingTesterFactory> marshallerClass = MarshallingTesterFactory.class;
		Map<CacheType, Set<TransactionMode>> types = new EnumMap<>(CacheType.class);

		InfinispanSessionManagerArgumentsProvider() {
			this.types.put(CacheType.DISTRIBUTION, EnumSet.allOf(TransactionMode.class));
			this.types.put(CacheType.REPLICATION, EnumSet.allOf(TransactionMode.class));
			this.types.put(CacheType.INVALIDATION, EnumSet.allOf(TransactionMode.class));
		}

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (MarshallingTesterFactory factory : ServiceLoader.load(this.marshallerClass, this.marshallerClass.getClassLoader())) {
				ByteBufferMarshaller marshaller = factory.getMarshaller();
				for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
					for (CacheType type : this.types.keySet()) {
						for (TransactionMode mode : this.types.get(type)) {
							builder.add(Arguments.of(new InfinispanSessionManagerParameters() {
								@Override
								public ByteBufferMarshaller getSessionAttributeMarshaller() {
									return marshaller;
								}

								@Override
								public SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy() {
									return strategy;
								}

								@Override
								public CacheType getCacheType() {
									return type;
								}

								@Override
								public TransactionMode getTransactionMode() {
									return mode;
								}

								@Override
								public Runnable persistence(GlobalConfiguration global, PersistenceConfigurationBuilder builder) {
									if (this.getCacheType() == CacheType.INVALIDATION) {
										Class<? extends TwoWayKey2StringMapper> mapperClass = ServiceLoader.load(TwoWayKey2StringMapper.class, this.getClass().getClassLoader()).findFirst().map(TwoWayKey2StringMapper::getClass).orElse(null);
										JdbcDataSource dataSource = new JdbcDataSource();
										dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
										dataSource.setUser("sa");
										builder.addStore(JdbcStringBasedStoreConfigurationBuilder.class)
												.dialect(DatabaseType.H2)
												.key2StringMapper(mapperClass)
												.shared(true)
												.table().createOnStart(true).tableNamePrefix("ispn").idColumnName("id").idColumnType("VARCHAR").dataColumnName("data").dataColumnType("VARBINARY").segmentColumnName("segment").segmentColumnType("NUMERIC").timestampColumnName("ts").timestampColumnType("BIGINT")
												.connectionFactory(DataSourceConnectionFactoryConfigurationBuilder.class).withDataSource(dataSource)
												;
									}
									return Runner.empty();
								}

								@Override
								public String toString() {
									return Map.ofEntries(
											Map.entry(ByteBufferMarshaller.class.getSimpleName(), marshaller.toString()),
											Map.entry(SessionAttributePersistenceStrategy.class.getSimpleName(), strategy.name()),
											Map.entry(CacheType.class.getSimpleName(), type.name()),
											Map.entry(TransactionMode.class.getSimpleName(), mode.name())
										).toString();
								}
							}));
						}
					}
				}
			}
			return builder.build();
		}
	}

	static class ConcurrentInfinispanSessionManagerArgumentsProvider extends InfinispanSessionManagerArgumentsProvider {
		ConcurrentInfinispanSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
			this.types = Map.of(CacheType.DISTRIBUTION, EnumSet.allOf(TransactionMode.class));
		}
	}

	static class ExpirationInfinispanSessionManagerArgumentsProvider extends InfinispanSessionManagerArgumentsProvider {
		ExpirationInfinispanSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
			this.types = Map.of(CacheType.DISTRIBUTION, EnumSet.allOf(TransactionMode.class));
		}
	}

	InfinispanSessionManagerITCase() {
		super(InfinispanSessionManagerFactoryContext::new);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(InfinispanSessionManagerArgumentsProvider.class)
	public void basic(InfinispanSessionManagerParameters parameters) {
		super.basic(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(ConcurrentInfinispanSessionManagerArgumentsProvider.class)
	public void concurrent(InfinispanSessionManagerParameters parameters) throws InterruptedException, ExecutionException {
		super.concurrent(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(ExpirationInfinispanSessionManagerArgumentsProvider.class)
	public void expiration(InfinispanSessionManagerParameters parameters) throws InterruptedException {
		super.expiration(parameters);
	}
}
