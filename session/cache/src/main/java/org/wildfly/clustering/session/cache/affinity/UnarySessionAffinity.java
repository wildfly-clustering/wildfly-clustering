/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.function.Function;

import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.GroupMember;

/**
 * Session affinity to a multiple members.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class UnarySessionAffinity<M extends GroupMember> implements UnaryOperator<String> {

	private Function<String, M> affinity;
	private Function<M, String> mapper;

	/**
	 * Creates a session affinity to a single member.
	 * @param affinity an affinity function
	 * @param mapper an affinity mapper
	 */
	public UnarySessionAffinity(Function<String, M> affinity, Function<M, String> mapper) {
		this.affinity = affinity;
		this.mapper = mapper;
	}

	@Override
	public String apply(String id) {
		return this.affinity.andThen(this.mapper).apply(id);
	}
}
