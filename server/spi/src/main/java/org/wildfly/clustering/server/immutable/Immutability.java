/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.immutable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Tests for immutability.
 * @author Paul Ferraro
 */
public interface Immutability extends Predicate<Object> {

	@Override
	default Immutability and(Predicate<? super Object> immutability) {
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				return Immutability.this.test(object) && immutability.test(object);
			}
		};
	}

	@Override
	default Immutability negate() {
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				return !Immutability.this.test(object);
			}
		};
	}

	@Override
	default Immutability or(Predicate<? super Object> immutability) {
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				return Immutability.this.test(object) || immutability.test(object);
			}
		};
	}

	static Immutability getDefault() {
		return composite(EnumSet.allOf(DefaultImmutability.class));
	}

	static Immutability composite(Collection<? extends Immutability> immutabilities) {
		return new Immutability() {
			private Immutability unmodifiable = new UnmodifiableImmutability(this);

			@Override
			public boolean test(Object object) {
				if (object == null) return true;
				// Arrays of non-zero length are inherently mutable
				if (object.getClass().isArray()) {
					return Array.getLength(object) == 0;
				}
				for (Immutability immutability : immutabilities) {
					if (immutability.test(object)) {
						return true;
					}
				}
				return this.unmodifiable.test(object);
			}
		};
	}

	static Immutability classes(Collection<Class<?>> immutableClasses) {
		Set<Class<?>> classes = UnmodifiableImmutability.identitySet(immutableClasses);
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				return (object == null) || classes.contains(object.getClass());
			}
		};
	}

	static Immutability identity(Collection<Object> immutableObjects) {
		Set<Object> objects = UnmodifiableImmutability.identitySet(immutableObjects);
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				return (object == null) || objects.contains(object);
			}
		};
	}

	static Immutability instanceOf(Collection<Class<?>> immutableClasses) {
		return new Immutability() {
			@Override
			public boolean test(Object object) {
				if (object == null) return true;
				for (Class<?> immutableClass : immutableClasses) {
					if (immutableClass.isInstance(object)) return true;
				}
				return false;
			}
		};
	}
}
