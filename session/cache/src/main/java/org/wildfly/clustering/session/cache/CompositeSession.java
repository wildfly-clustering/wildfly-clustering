/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * Generic session implementation composed of attributes and metadata.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CompositeSession<C> extends CompositeImmutableSession implements Session<C> {
	private static final System.Logger LOGGER = System.getLogger(CompositeSession.class.getName());

	private final InvalidatableSessionMetaData metaData;
	private final SessionAttributes attributes;
	private final Supplied<C> context;
	private final Supplier<C> contextFactory;
	private final CacheEntryRemover<String> remover;

	public CompositeSession(String id, InvalidatableSessionMetaData metaData, SessionAttributes attributes, Supplied<C> context, Supplier<C> contextFactory, CacheEntryRemover<String> remover) {
		super(id, metaData, attributes);
		this.metaData = metaData;
		this.attributes = attributes;
		this.context = context;
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
			LOGGER.log(System.Logger.Level.DEBUG, "Invalidating session {0}", this.getId());
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
			LOGGER.log(System.Logger.Level.TRACE, "Closing session {0}", this.getId());
			this.attributes.close();
			this.metaData.close();
		}
	}

	@Override
	public C getContext() {
		return this.context.get(this.contextFactory);
	}
}
