/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.Map;

/**
 * An immutable view of a session.
 * @author Paul Ferraro
 */
public interface ImmutableSession {
	/**
	 * Specifies this session's unique identifier.
	 * @return a unique identifier for this session.
	 */
	String getId();

	/**
	 * Indicates whether or not this session is valid.
	 * @return true, if this session is valid, false otherwise
	 */
	boolean isValid();

	/**
	 * Returns this session's meta data.
	 * @return this session's meta data
	 */
	ImmutableSessionMetaData getMetaData();

	/**
	 * Returns this session's attributes.
	 * @return this session's attributes
	 */
	Map<String, Object> getAttributes();
}
