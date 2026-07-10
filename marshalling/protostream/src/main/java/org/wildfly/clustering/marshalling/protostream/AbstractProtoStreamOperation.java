/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.ProtobufTagMarshaller.OperationContext;

/**
 * A protostream operation.
 * @author Paul Ferraro
 */
public abstract class AbstractProtoStreamOperation implements ProtoStreamOperation, OperationContext {
	private final ImmutableSerializationContext context;
	private final OperationContext operationContext;

	AbstractProtoStreamOperation(OperationContext operationContext, ImmutableSerializationContext context) {
		this.operationContext = operationContext;
		this.context = context;
	}

	@Override
	public ImmutableSerializationContext getSerializationContext() {
		return this.context;
	}

	@Override
	public Object getParam(Object key) {
		return this.operationContext.getParam(key);
	}

	@Override
	public void setParam(Object key, Object value) {
		this.operationContext.setParam(key, value);
	}
}
