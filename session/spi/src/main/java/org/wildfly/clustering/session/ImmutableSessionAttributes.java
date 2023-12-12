/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Provides read-only access to a session's attributes.
 * @author Paul Ferraro
 */
public interface ImmutableSessionAttributes {
	/**
	 * Returns the names of the attributes of this session.
	 * @return a set of unique attribute names
	 */
	Set<String> getAttributeNames();

	/**
	 * Retrieves the value of the specified attribute.
	 * @param name a unique attribute name
	 * @return the attribute value, or null if the attribute does not exist.
	 */
	Object getAttribute(String name);

	/**
	 * Convenience method returning a map of attributes whose values are instances of the specified class.
	 * @param targetClass a target class
	 * @return an unmodifiable map of attributes implementing the specified class
	 */
	default <T> Map<String, T> getAttributes(Class<T> targetClass) {
        Set<String> names = this.getAttributeNames();
        if (names.isEmpty()) return Map.of();
        Map<String, T> result = new TreeMap<>();
        for (String name : names) {
            Object attribute = this.getAttribute(name);
            if (targetClass.isInstance(attribute)) {
                result.put(name, targetClass.cast(attribute));
            }
        }
        return Collections.unmodifiableMap(result);
	}
}
