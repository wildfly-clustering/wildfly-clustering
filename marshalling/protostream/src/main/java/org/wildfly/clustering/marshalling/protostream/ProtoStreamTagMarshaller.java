/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.TagWriter;

/**
 * A ProtoStream tag marshaller extension.
 * @author Paul Ferraro
 * @param <T> the marshalled object type
 */
public interface ProtoStreamTagMarshaller<T> extends ProtobufTagMarshaller<T> {

	@Override
	default String getTypeName() {
		Class<?> targetClass = this.getJavaClass();
		Package targetPackage = targetClass.getPackage();
		return (targetPackage != null) ? (targetPackage.getName() + '.' + targetClass.getSimpleName()) : targetClass.getSimpleName();
	}

	/**
	 * Encapsulates a size operation context.
	 */
	interface SizeContext extends WriteContext {
		/**
		 * Returns the number of bytes written within this context.
		 * @return the number of bytes written within this context.
		 */
		int getWrittenBytes();

		/**
		 * Returns a size context.
		 * @param <C> the context type
		 * @param factory a write context factory
		 * @param writtenBytes a function returning the bytes written by a write context
		 * @return a size context.
		 */
		static <C extends ProtobufTagMarshaller.WriteContext> SizeContext of(Supplier<C> factory, ToIntFunction<C> writtenBytes) {
			return new SizeContext(){
				private final C context = factory.get();

				@Override
				public TagWriter getWriter() {
					return this.context.getWriter();
				}

				@Override
				public int depth() {
					return this.context.depth();
				}

				@Override
				public ImmutableSerializationContext getSerializationContext() {
					return this.context.getSerializationContext();
				}

				@Override
				public Object getParam(Object key) {
					return this.context.getParam(key);
				}

				@Override
				public void setParam(Object key, Object value) {
					this.context.setParam(key, value);
				}

				@Override
				public int getWrittenBytes() {
					return writtenBytes.applyAsInt(this.context);
				}
			};
		}
	}
}
