/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.infinispan.persistence.jdbc.common.configuration.ConnectionFactoryConfiguration;
import org.infinispan.persistence.jdbc.common.connectionfactory.ConnectionFactory;
import org.infinispan.persistence.spi.PersistenceException;

/**
 * A connection factory using an injected {@link DataSource}.
 * @author Paul Ferraro
 */
public class DataSourceConnectionFactory extends ConnectionFactory {
	private static final System.Logger LOGGER = System.getLogger(DataSourceConnectionFactory.class.getName());

	private volatile DataSource factory;

	/**
	 * Creates a {@link DataSource} connection factory.
	 */
	public DataSourceConnectionFactory() {
	}

	@Override
	public void start(ConnectionFactoryConfiguration configuration, ClassLoader classLoader) throws PersistenceException {
		this.factory = ((DataSourceConnectionFactoryConfiguration) configuration).getDataSource();
	}

	@Override
	public void stop() {
		this.factory = null;
	}

	@Override
	public Connection getConnection() throws PersistenceException {
		try {
			return this.factory.getConnection();
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public void releaseConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
	}
}
