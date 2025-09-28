/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.jboss.marshalling.SerializabilityChecker;

/**
 * A {@link SerializabilityChecker} based on a fixed set of classes.
 * @author Paul Ferraro
 */
public class IdentitySerializabilityChecker implements SerializabilityChecker {

	private final Set<Class<?>> classes;

	/**
	 * Creates a serializability checker from the specified marshallable classes
	 * @param classes a collection of marshallable classes
	 */
	public IdentitySerializabilityChecker(Collection<Class<?>> classes) {
		this.classes = Collections.newSetFromMap(new IdentityHashMap<>(classes.size()));
		this.classes.addAll(classes);
	}

	@Override
	public boolean isSerializable(Class<?> targetClass) {
		return (targetClass != Object.class) && (this.classes.contains(targetClass) || DEFAULT.isSerializable(targetClass));
	}
}
