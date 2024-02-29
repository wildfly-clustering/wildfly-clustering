/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * Generic session implementation composed of attributes and metadata.
 * @author Paul Ferraro
 */
public class CompositeSession<C> extends CompositeImmutableSession implements Session<C> {
	private static final Logger LOGGER = Logger.getLogger(CompositeSession.class);

	private final InvalidatableSessionMetaData metaData;
	private final SessionAttributes attributes;
	private final Supplied<C> context;
	private final Supplier<C> contextFactory;
	private final Remover<String> remover;

	public CompositeSession(String id, InvalidatableSessionMetaData metaData, SessionAttributes attributes, Supplied<C> context, Supplier<C> contextFactory, Remover<String> remover) {
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
			LOGGER.debugf("Invalidating session %s", this.getId());
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
			LOGGER.tracef("Closing session %s", this.getId());
			this.attributes.close();
			this.metaData.close();
		}
	}

	@Override
	public C getContext() {
		return this.context.get(this.contextFactory);
	}
}
