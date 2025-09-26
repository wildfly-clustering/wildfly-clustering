/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reference that can be associated with an arbitrary thread.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public class ThreadContextReference<C> implements ContextReference<C> {
	private final Supplier<Thread> reference;
	private final Function<Thread, C> accessor;
	private final BiConsumer<Thread, C> applicator;

	/**
	 * Constructs a context reference for a provided thread.
	 * @param reference a provider of a thread
	 * @param accessor a function used to access the context of a thread.
	 * @param applicator a consumer used to apply context to a thread.
	 */
	public ThreadContextReference(Supplier<Thread> reference, Function<Thread, C> accessor, BiConsumer<Thread, C> applicator) {
		this.reference = reference;
		this.accessor = accessor;
		this.applicator = applicator;
	}

	@SuppressWarnings("removal")
	@Override
	public void accept(C context) {
		Thread thread = this.reference.get();
		if (System.getSecurityManager() == null) {
			this.applicator.accept(thread, context);
		} else {
			BiConsumer<Thread, C> applicator = this.applicator;
			AccessController.doPrivileged(new PrivilegedAction<>() {
				@Override
				public Void run() {
					applicator.accept(thread, context);
					return null;
				}
			});
		}
	}

	@SuppressWarnings("removal")
	@Override
	public C get() {
		Thread thread = this.reference.get();
		if (System.getSecurityManager() == null) {
			return this.accessor.apply(thread);
		}
		Function<Thread, C> accessor = this.accessor;
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public C run() {
				return accessor.apply(thread);
			}
		});
	}
}
