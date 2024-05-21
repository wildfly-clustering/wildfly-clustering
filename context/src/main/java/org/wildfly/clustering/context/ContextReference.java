/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.wildfly.common.function.Functions;

/**
 * Reference to some context.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public interface ContextReference<C> extends Supplier<C>, Consumer<C> {

	static <C> ContextReference<C> fromThreadLocal(ThreadLocal<C> threadLocal) {
		return new ContextReference<>() {
			@Override
			public C get() {
				return threadLocal.get();
			}

			@Override
			public void accept(C value) {
				threadLocal.set(value);
			}
		};
	}

	/**
	 * Returns a context provider for the specified value.
	 * @param target the target context
	 * @return a context provider
	 */
	default Supplier<Context> provide(C target) {
		return (target != null) ? new Supplier<>() {
			@Override
			public Context get() {
				C existing = ContextReference.this.get();
				if (existing == target) return Context.EMPTY;
				ContextReference.this.accept(target);
				return new Context() {
					@Override
					public void close() {
						ContextReference.this.accept(existing);
					}
				};
			}
		} : Functions.constantSupplier(Context.EMPTY);
	}
}
