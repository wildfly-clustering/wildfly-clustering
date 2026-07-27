/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.marshalling.protostream.util.StringKeyMapEntry;

/**
 * ProtoStream marshaller for an annotation field.
 * @author Paul Ferraro
 */
public enum AnnotationMarshaller implements FieldMarshaller<Annotation> {
	/** Singleton instance */
	INSTANCE;

	private static final Class<? extends InvocationHandler> ANNOTATION_INVOCATION_HANDLER = Proxy.getInvocationHandler(DeprecatedClass.class.getAnnotation(Deprecated.class)).getClass().asSubclass(InvocationHandler.class);

	private static final MethodHandle ANNOTATION_PROPERTIES_HANDLE = findFieldHandle(Map.class);
	private static final MethodHandle ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR_HANDLE = findConstructorHandle(MethodType.methodType(void.class, Class.class, Map.class));

	private static MethodHandle findFieldHandle(Class<?> fieldClass) {
		Field field = Stream.of(ANNOTATION_INVOCATION_HANDLER.getDeclaredFields()).filter(Predicate.of(Field::getType, Predicate.identicalTo(fieldClass))).findFirst().get();
		try {
			return MethodHandles.privateLookupIn(ANNOTATION_INVOCATION_HANDLER, MethodHandles.lookup()).findGetter(ANNOTATION_INVOCATION_HANDLER, field.getName(), fieldClass);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}

	private static MethodHandle findConstructorHandle(MethodType type) {
		try {
			return MethodHandles.privateLookupIn(ANNOTATION_INVOCATION_HANDLER, MethodHandles.lookup()).findConstructor(ANNOTATION_INVOCATION_HANDLER, type);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Class<? extends Annotation> getJavaClass() {
		return Annotation.class;
	}

	@Override
	public Annotation readFrom(ProtoStreamReader reader) throws IOException {
		Class<?> annotationType = ScalarClass.ANY.readFrom(reader);
		Map<String, Object> properties = reader.repeatedEntryCollector();

		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			if (index == AnyField.ANY.getIndex()) {
				Map.Entry<String, Object> entry = reader.readObject(StringKeyMapEntry.class);
				properties.put(entry.getKey(), entry.getValue());
			} else {
				reader.skipField(tag);
			}
		}

		try {
			InvocationHandler handler = (InvocationHandler) ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR_HANDLE.invoke(annotationType, properties);
			return (Annotation) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class<?>[] { annotationType }, handler);
		} catch (Throwable e) {
			if (e instanceof RuntimeException exception) {
				throw exception;
			}
			if (e instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Annotation annotation) throws IOException {
		ScalarClass.ANY.writeTo(writer, annotation.annotationType());

		InvocationHandler handler = Proxy.getInvocationHandler(annotation);
		try {
			Map<String, Object> properties = (Map<String, Object>) ANNOTATION_PROPERTIES_HANDLE.invoke(handler);
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				writer.writeObject(AnyField.ANY.getIndex(), new StringKeyMapEntry<>(entry));
			}
		} catch (Throwable e) {
			if (e instanceof RuntimeException exception) {
				throw exception;
			}
			if (e instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException(e);
		}
	}

	@Override
	public WireType getWireType() {
		return ScalarClass.ANY.getWireType();
	}

	@Deprecated
	private static class DeprecatedClass {
	}
}
