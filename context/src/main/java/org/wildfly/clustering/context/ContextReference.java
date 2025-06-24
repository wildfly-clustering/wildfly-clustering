/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.atomic.AtomicReference;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Reference to some context.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public interface ContextReference<C> extends java.util.function.Supplier<C>, java.util.function.Consumer<C>, java.util.function.UnaryOperator<C> {

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

	static <C> ContextReference<C> of(Supplier<C> supplier, Consumer<C> consumer) {
		return new SimpleContextReference<>(supplier, consumer);
	}

	static <C> ContextReference<C> of(Supplier<C> supplier, Consumer<C> consumer, UnaryOperator<C> operator) {
		return new SimpleContextReference<>(supplier, consumer) {
			@Override
			public C apply(C context) {
				return operator.apply(context);
			}
		};
	}

	static <C> ContextReference<C> of(AtomicReference<C> reference) {
		return of(reference::get, reference::set, reference::getAndSet);
	}

	default <T extends C> T get(Class<T> contextType) {
		return contextType.cast(this.get());
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
	default java.util.function.Supplier<Context> provide(C target) {
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
		} : Supplier.of(Context.EMPTY);
	}

	class SimpleContextReference<C> implements ContextReference<C> {
		private final Supplier<C> supplier;
		private final Consumer<C> consumer;

		SimpleContextReference(Supplier<C> supplier, Consumer<C> consumer) {
			this.supplier = supplier;
			this.consumer = consumer;
		}

		@Override
		public C get() {
			return this.supplier.get();
		}

		@Override
		public void accept(C context) {
			this.consumer.accept(context);
		}
	}
}
