/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.server.GroupMember;

/**
 * @author Paul Ferraro
 */
public class UnarySessionAffinity<M extends GroupMember> implements UnaryOperator<String> {

	private Function<String, M> affinity;
	private Function<M, String> mapper;

	public UnarySessionAffinity(Function<String, M> affinity, Function<M, String> mapper) {
		this.affinity = affinity;
		this.mapper = mapper;
	}

	@Override
	public String apply(String id) {
		return this.affinity.andThen(this.mapper).apply(id);
	}
}
