/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.util.Deque;

/**
 * A concurrent deque that allows direct item removal without traversal.
 *
 * @author Jason T. Greene
 * @author Paul Ferraro
 * @param <E> the element type
 */
public interface ConcurrentDirectDeque<E> extends Deque<E> {

	static <K> ConcurrentDirectDeque<K> newInstance() {
		return new FastConcurrentDirectDeque<>();
	}

	/**
	 * Equivalent to {@link #offerFirst(Object)}, but returns a token used for fast removal.
	 * @param e the element to offer
	 * @return a token suitable for use by {@link #remove(Object)}
	 */
	Object offerFirstAndReturnToken(E e);

	/**
	 * Equivalent to {@link #offerLast(Object)}, but returns a token used for fast removal.
	 * @param e the element to offer
	 * @return a token suitable for use by {@link #remove(Object)}
	 */
	Object offerLastAndReturnToken(E e);

	/**
	 * Removes the element associated with the given token.
	 * @param token the token returned via {@link #offerFirstAndReturnToken(Object)} or {@link #offerLastAndReturnToken(Object)}.
	 */
	void removeToken(Object token);

	// Delegate collection methods to deque methods

	@Override
	default boolean add(E e) {
		return this.offerLast(e);
	}

	@Override
	default boolean remove(Object o) {
		return this.removeFirstOccurrence(o);
	}

	// Delegate stack methods to deque methods

	@Override
	default E peek() {
		return this.peekFirst();
	}

	@Override
	default E pop() {
		return this.removeFirst();
	}

	@Override
	default void push(E e) {
		this.addFirst(e);
	}

	// Delegate queue methods to deque methods

	@Override
	default E element() {
		return this.getFirst();
	}

	@Override
	default boolean offer(E e) {
		return this.offerLast(e);
	}

	@Override
	default E poll() {
		return this.pollFirst();
	}

	@Override
	default E remove() {
		return this.removeFirst();
	}
}
