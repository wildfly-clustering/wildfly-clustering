/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.jdbc;

import javax.sql.DataSource;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.persistence.jdbc.common.configuration.ConnectionFactoryConfiguration;
import org.infinispan.persistence.jdbc.common.connectionfactory.ConnectionFactory;

/**
 * Configuration for {@link DataSourceConnectionFactory}.
 * @author Paul Ferraro
 */
@BuiltBy(DataSourceConnectionFactoryConfigurationBuilder.class)
public class DataSourceConnectionFactoryConfiguration implements ConnectionFactoryConfiguration {

	private final DataSource dataSource;

	/**
	 * Creates a connection factory configuration.
	 * @param dataSource a data source
	 */
	public DataSourceConnectionFactoryConfiguration(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Returns the data source associated with this connection factory.
	 * @return the data source associated with this connection factory.
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public Class<? extends ConnectionFactory> connectionFactoryClass() {
		return DataSourceConnectionFactory.class;
	}

	@Override
	public AttributeSet attributes() {
		return AttributeSet.EMPTY;
	}
}
