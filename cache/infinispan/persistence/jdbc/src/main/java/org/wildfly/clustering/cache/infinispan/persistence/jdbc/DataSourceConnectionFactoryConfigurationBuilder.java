/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.jdbc;

import javax.sql.DataSource;

import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.persistence.jdbc.common.configuration.AbstractJdbcStoreConfigurationBuilder;
import org.infinispan.persistence.jdbc.common.configuration.AbstractJdbcStoreConfigurationChildBuilder;
import org.infinispan.persistence.jdbc.common.configuration.ConnectionFactoryConfigurationBuilder;

/**
 * Builds a {@link DataSourceConnectionFactoryConfiguration}.
 * @author Paul Ferraro
 * @param <S> the store type
 */
public class DataSourceConnectionFactoryConfigurationBuilder<S extends AbstractJdbcStoreConfigurationBuilder<?, S>> extends AbstractJdbcStoreConfigurationChildBuilder<S> implements ConnectionFactoryConfigurationBuilder<DataSourceConnectionFactoryConfiguration> {

	private volatile DataSource dataSource;

	/**
	 * Creates a connection factory configuration builder.
	 * @param builder the parent builder
	 */
	public DataSourceConnectionFactoryConfigurationBuilder(AbstractJdbcStoreConfigurationBuilder<?, S> builder) {
		super(builder);
	}

	/**
	 * Uses the specified DataSource.
	 * @param dataSource a data source
	 * @return a reference to this builder
	 */
	public DataSourceConnectionFactoryConfigurationBuilder<S> withDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	@Override
	public void validate() {
		// Nothing to validate
	}

	@Override
	public void validate(GlobalConfiguration globalConfig) {
		if (this.dataSource == null) {
			throw new CacheConfigurationException(DataSource.class.getName());
		}
	}

	@Override
	public DataSourceConnectionFactoryConfiguration create() {
		return new DataSourceConnectionFactoryConfiguration(this.dataSource);
	}

	@Override
	public DataSourceConnectionFactoryConfigurationBuilder<S> read(DataSourceConnectionFactoryConfiguration template, Combine combine) {
		this.dataSource = template.getDataSource();
		return this;
	}

	@Override
	public AttributeSet attributes() {
		return AttributeSet.EMPTY;
	}
}
