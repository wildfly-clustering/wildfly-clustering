/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet.mutex;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import org.wildfly.clustering.session.container.SessionManagementEndpointConfiguration;
import org.wildfly.clustering.session.container.servlet.SessionServlet;

/**
 * Session servlet using a {@link Integer} counter where reads/updates require monitor locks.
 * @author Paul Ferraro
 */
@WebServlet(SessionManagementEndpointConfiguration.ENDPOINT_PATH)
public class MutexSessionServlet extends SessionServlet<Integer> {
	private static final Object MUTEX = new Object();
	private static final long serialVersionUID = 548028574662487833L;

	public MutexSessionServlet() {
		super(Integer::valueOf, new BiFunction<>() {
			@Override
			public OptionalInt apply(HttpSession session, String name) {
				synchronized (MUTEX) {
					Integer value = (Integer) session.getAttribute(name);
					return (value != null) ? OptionalInt.of(value.intValue()) : OptionalInt.empty();
				}
			}
		}, new BiFunction<>() {
			@Override
			public OptionalInt apply(HttpSession session, String name) {
				synchronized (MUTEX) {
					Integer value = (Integer) session.getAttribute(name);
					if (value != null) {
						value = Integer.valueOf(value.intValue() + 1);
						session.setAttribute(name, value);
					}
					return (value != null) ? OptionalInt.of(value.intValue()) : OptionalInt.empty();
				}
			}
		});
	}
}
