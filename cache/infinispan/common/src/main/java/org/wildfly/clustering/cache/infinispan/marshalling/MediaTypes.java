/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling;

import java.util.function.Supplier;

import org.infinispan.commons.dataconversion.MediaType;

/**
 * Media types for supported marshallers.
 * @author Paul Ferraro
 */
public enum MediaTypes implements Supplier<MediaType> {

	JAVA_SERIALIZATION(MediaType.APPLICATION_SERIALIZED_OBJECT),
	JBOSS_MARSHALLING(MediaType.APPLICATION_JBOSS_MARSHALLING),
	INFINISPAN_PROTOSTREAM(MediaType.APPLICATION_PROTOSTREAM),
	WILDFLY_PROTOSTREAM(MediaType.APPLICATION_PROTOSTREAM.withParameter("agent", "wildfly")),
	;
	private final MediaType mediaType;

	MediaTypes(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public MediaType get() {
		return this.mediaType;
	}
}
