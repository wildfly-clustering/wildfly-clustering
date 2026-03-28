/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.test;

/**
 * @author Paul Ferraro
 * @param name a name
 * @param value a value
 */
public record TestRecord(String name, Integer value) implements java.io.Serializable {
}
