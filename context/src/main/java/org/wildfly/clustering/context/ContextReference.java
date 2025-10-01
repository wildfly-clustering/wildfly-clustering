/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.atomic.AtomicReference;

import org.wildfly.clustering.function.Supplier;

/**
 * Reference to some context.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public interface ContextReference<C> extends java.util.function.Supplier<C>, java.util.function.Consumer<C>, java.util.function.UnaryOperator<C> {

	/**
	 * Creates a context reference from the specified {@link ThreadLocal}.
	 * @param <C> the context type
	 * @param threadLocal a thread local used to store the context
	 * @return a context reference using the specified {@link ThreadLocal}.
	 */
	static <C> ContextReference<C> fromThreadLocal(ThreadLocal<C> threadLocal) {
		return new ContextReference<>() {
			@Override
			public C get() {
				return threadLocal.get();
			}

			@Override
			public void accept(C context) {
				if (context != null) {
					threadLocal.set(context);
				} else {
					threadLocal.remove();
				}
			}
		};
	}

	/**
	 * Creates a context reference from the specified {@link AtomicReference}.
	 * @param <C> the context type
	 * @param reference an atomic reference used to store the context
	 * @return a context reference using the specified {@link AtomicReference}.
	 */
	static <C> ContextReference<C> of(AtomicReference<C> reference) {
		return new ContextReference<>() {
			@Override
			public C get() {
				return reference.get();
			}

			@Override
			public void accept(C context) {
				reference.set(context);
			}

			@Override
			public C apply(C context) {
				return reference.getAndSet(context);
			}
		};
	}

	@Override
	default C apply(C context) {
		C current = this.get();
		this.accept(context);
		return current;
	}

	/**
	 * Returns a context provider for the specified value.
	 * @param target the target context
	 * @return a context provider
	 */
	default java.util.function.Supplier<Context<C>> provide(C target) {
		return (target != null) ? new Supplier<>() {
			@Override
			public Context<C> get() {
				C existing = ContextReference.this.get();
				if (existing == target) return Context.empty();
				ContextReference.this.accept(target);
				return new Context<>() {
					@Override
					public C get() {
						return target;
					}

					@Override
					public void close() {
						ContextReference.this.accept(existing);
					}
				};
			}
		} : Supplier.of(Context.empty());
	}
}
