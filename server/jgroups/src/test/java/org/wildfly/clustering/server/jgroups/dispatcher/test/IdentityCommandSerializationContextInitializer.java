/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher.test;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = { IdentityCommand.class }, service = true)
public interface IdentityCommandSerializationContextInitializer extends SerializationContextInitializer {
}
