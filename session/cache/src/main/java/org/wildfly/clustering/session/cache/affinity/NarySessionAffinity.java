/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.wildfly.clustering.server.GroupMember;

/**
 * @author Paul Ferraro
 */
public class NarySessionAffinity<M extends GroupMember> implements UnaryOperator<String> {

	private Function<String, List<M>> affinity;
	private Function<M, String> mapper;
	private String delimiter;
	private int maxServers;

	public NarySessionAffinity(Function<String, List<M>> affinity, Function<M, String> mapper, NarySessionAffinityConfiguration config) {
		this.affinity = affinity;
		this.mapper = mapper;
		this.delimiter = config.getDelimiter();
		this.maxServers = config.getMaxServers();
	}

	@Override
	public String apply(String id) {
		return this.affinity.apply(id).stream().map(this.mapper).filter(Objects::nonNull).limit(this.maxServers).collect(Collectors.joining(this.delimiter));
	}
}
