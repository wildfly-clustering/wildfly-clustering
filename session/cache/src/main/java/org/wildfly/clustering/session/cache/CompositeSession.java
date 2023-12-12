/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * Generic session implementation - independent of cache mapping strategy.
 * @author Paul Ferraro
 */
public class CompositeSession<C> extends CompositeImmutableSession implements Session<C> {

	private final InvalidatableSessionMetaData metaData;
	private final SessionAttributes attributes;
	private final Contextual<C> contextual;
	private final Supplier<C> contextFactory;
	private final Remover<String> remover;

	public CompositeSession(String id, InvalidatableSessionMetaData metaData, SessionAttributes attributes, Contextual<C> contextual, Supplier<C> contextFactory, Remover<String> remover) {
		super(id, metaData, attributes);
		this.metaData = metaData;
		this.attributes = attributes;
		this.contextual = contextual;
		this.contextFactory = contextFactory;
		this.remover = remover;
	}

	@Override
	public SessionAttributes getAttributes() {
		return this.attributes;
	}

	@Override
	public boolean isValid() {
		return this.metaData.isValid();
	}

	@Override
	public void invalidate() {
		if (this.metaData.invalidate()) {
			this.remover.remove(this.getId());
		}
	}

	@Override
	public SessionMetaData getMetaData() {
		return this.metaData;
	}

	@Override
	public void close() {
		if (this.metaData.isValid()) {
			this.attributes.close();
			this.metaData.close();
		}
	}

	@Override
	public C getContext() {
		return this.contextual.getContext(this.contextFactory);
	}
}
