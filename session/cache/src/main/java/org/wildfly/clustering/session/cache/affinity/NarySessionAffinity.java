/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.GroupMember;

/**
 * Session affinity to multiple members.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class NarySessionAffinity<M extends GroupMember> implements UnaryOperator<String> {

	private Function<String, List<M>> affinity;
	private Function<M, String> mapper;
	private String delimiter;
	private int maxMembers;

	/**
	 * Creates an affinity to multiple members.
	 * @param affinity an affinity function
	 * @param mapper a mapping of group member to name
	 * @param config the affinity configuration
	 */
	public NarySessionAffinity(Function<String, List<M>> affinity, Function<M, String> mapper, NarySessionAffinityConfiguration config) {
		this.affinity = affinity;
		this.mapper = mapper;
		this.delimiter = config.getDelimiter();
		this.maxMembers = config.getMaxMembers();
	}

	@Override
	public String apply(String id) {
		return this.affinity.apply(id).stream().map(this.mapper).distinct().limit(this.maxMembers).collect(Collectors.joining(this.delimiter));
	}
}
