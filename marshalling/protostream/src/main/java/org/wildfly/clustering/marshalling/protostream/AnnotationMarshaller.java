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
import java.util.Optional;
import java.util.stream.Stream;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Function;
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

	@SuppressWarnings("unchecked")
	private static final Function<InvocationHandler, Map<String, Object>> ANNOTATION_PROPERTIES_HANDLE = findFieldHandle((Class<Map<String, Object>>) (Class<?>) Map.class);
	@SuppressWarnings("unchecked")
	private static final BiFunction<Class<?>, Map<String, Object>, InvocationHandler> ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR_HANDLE = findConstructorHandle((Class<Class<?>>) (Class<?>) Class.class, (Class<Map<String, Object>>) (Class<?>) Map.class);

	private static <T> Function<InvocationHandler, T> findFieldHandle(Class<T> fieldClass) {
		Field field = Stream.of(ANNOTATION_INVOCATION_HANDLER.getDeclaredFields()).filter(Predicate.of(Field::getType, Predicate.identicalTo(fieldClass))).findFirst().get();
		try {
			MethodHandle handle = MethodHandles.privateLookupIn(ANNOTATION_INVOCATION_HANDLER, MethodHandles.lookup()).findGetter(ANNOTATION_INVOCATION_HANDLER, field.getName(), fieldClass);
			return Function.invoke(handle);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			return Function.throwing(e);
		}
	}

	private static BiFunction<Class<?>, Map<String, Object>, InvocationHandler> findConstructorHandle(Class<Class<?>> parameter1Type, Class<Map<String, Object>> parameter2Type) {
		try {
			MethodHandle handle = MethodHandles.privateLookupIn(ANNOTATION_INVOCATION_HANDLER, MethodHandles.lookup()).findConstructor(ANNOTATION_INVOCATION_HANDLER, MethodType.methodType(void.class, parameter1Type, parameter2Type));
			return BiFunction.invoke(handle);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			return BiFunction.throwing(e);
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

		InvocationHandler handler = ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR_HANDLE.apply(annotationType, properties);
		return (Annotation) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class<?>[] { annotationType }, handler);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Annotation annotation) throws IOException {
		ScalarClass.ANY.writeTo(writer, annotation.annotationType());

		InvocationHandler handler = Proxy.getInvocationHandler(annotation);
		Map<String, Object> properties = ANNOTATION_PROPERTIES_HANDLE.apply(handler);
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			writer.writeObject(AnyField.ANY.getIndex(), new StringKeyMapEntry<>(entry));
		}
	}

	@Override
	public WireType getWireType() {
		return ScalarClass.ANY.getWireType();
	}

	@Override
	public Optional<Class<?>> getOpenClass() {
		return Optional.of(ANNOTATION_INVOCATION_HANDLER);
	}

	@Deprecated
	private static class DeprecatedClass {
	}
}
