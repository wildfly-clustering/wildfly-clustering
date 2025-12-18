/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet.atomic;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import org.wildfly.clustering.session.container.SessionManagementEndpointConfiguration;
import org.wildfly.clustering.session.container.servlet.SessionServlet;

/**
 * Session servlet using a {@link AtomicInteger} counter.
 * @author Paul Ferraro
 */
@WebServlet(SessionManagementEndpointConfiguration.ENDPOINT_PATH)
public class AtomicSessionServlet extends SessionServlet<AtomicInteger> {
	private static final long serialVersionUID = -4362246699099401997L;

	public AtomicSessionServlet() {
		super(AtomicInteger::new, new AtomicIntegerFunction(AtomicInteger::get), new AtomicIntegerFunction(AtomicInteger::incrementAndGet));
	}

	private static class AtomicIntegerFunction implements BiFunction<HttpSession, String, OptionalInt> {
		private final ToIntFunction<AtomicInteger> function;

		AtomicIntegerFunction(ToIntFunction<AtomicInteger> function) {
			this.function = function;
		}

		@Override
		public OptionalInt apply(HttpSession session, String name) {
			AtomicInteger count = (AtomicInteger) session.getAttribute(name);
			return (count != null) ? OptionalInt.of(this.function.applyAsInt(count)) : OptionalInt.empty();
		}
	}
}
