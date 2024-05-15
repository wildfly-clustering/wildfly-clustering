/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

	default Supplier<Context> provide(C context) {
		ContextReference<C> reference = this;
		return new Supplier<>() {
			@Override
			public Context get() {
				C currentContext = reference.get();
				reference.accept(context);
				return new Context() {
					@Override
					public void close() {
						reference.accept(currentContext);
					}
				};
			}
		};
	}
}
