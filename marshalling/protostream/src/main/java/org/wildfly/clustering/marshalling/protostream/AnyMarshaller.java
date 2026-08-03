/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.Function;

/**
 * Marshaller for an {@link Any} object.
 * @author Paul Ferraro
 */
class AnyMarshaller implements ProtoStreamMarshaller<Any> {
	private final Set<Class<?>> knownSerializableLambdas = Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));

	private final Set<AnyField> fields;

	AnyMarshaller(Set<AnyField> fields) {
		this.fields = fields;
	}

	@Override
	public Class<? extends Any> getJavaClass() {
		return Any.class;
	}

	@Override
	public boolean test(ImmutableSerializationContext context, Any any) {
		Object value = any.get();
		try {
			return (value == null) || (this.getField(context, value) != null);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public Any readFrom(ProtoStreamReader reader) throws IOException {
		Object value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			AnyField field = AnyField.forIndex(WireType.getTagFieldNumber(tag));
			if ((field != null) && this.fields.contains(field)) {
				value = field.getMarshaller().readFrom(reader);
			} else {
				reader.skipField(tag);
			}
		}
		return (value != null) ? new Any(value) : Any.NULL;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Any value) throws IOException {
		Object object = value.get();
		if (object != null) {
			AnyField field = this.getField(writer.getSerializationContext(), object);
			writer.writeTag(field);
			field.getMarshaller().writeTo(writer, object);
		}
	}

	private AnyField getField(ImmutableSerializationContext context, Object value) {
		if (value instanceof Reference) return AnyField.REFERENCE;

		Class<?> valueClass = value.getClass();

		if (valueClass.isSynthetic() && !valueClass.isLocalClass() && !valueClass.isAnonymousClass() && (value instanceof Serializable) && this.fields.contains(AnyField.LAMBDA)) {
			if (this.knownSerializableLambdas.contains(valueClass)) {
				return AnyField.LAMBDA;
			}
			try {
				MethodHandle handle = MethodHandles.privateLookupIn(valueClass, MethodHandles.lookup()).findVirtual(valueClass, "writeReplace", MethodType.methodType(Object.class));
				if (Function.invoke(handle).apply(value) instanceof SerializedLambda) {
					this.knownSerializableLambdas.add(valueClass);
					return AnyField.LAMBDA;
				}
			} catch (NoSuchMethodException | IllegalAccessException e) {
				// Not a serializable lambda
			}
		}

		return getField(context, valueClass);
	}

	private static AnyField getField(ImmutableSerializationContext context, Class<?> valueClass) {
		AnyField field = AnyField.forClass(valueClass);
		if (field != null) return field;

		Class<?> declaringClass = valueClass.getDeclaringClass();
		if ((declaringClass != null) && declaringClass.isEnum()) {
			BaseMarshaller<?> marshaller = context.getMarshaller(declaringClass);
			return hasTypeId(context, marshaller) ? AnyField.IDENTIFIED_ENUM : AnyField.NAMED_ENUM;
		}

		if (valueClass.isArray()) {
			Class<?> componentType = valueClass.getComponentType();
			AnyField componentTypeField = AnyField.forClass(componentType);
			if (componentTypeField != null) return AnyField.FIELD_ARRAY;
			try {
				BaseMarshaller<?> marshaller = context.getMarshaller(componentType);
				return hasTypeId(context, marshaller) ? AnyField.IDENTIFIED_ARRAY : AnyField.NAMED_ARRAY;
			} catch (IllegalArgumentException e) {
				return AnyField.ANY_ARRAY;
			}
		}

		if (!valueClass.isAnnotation() && Annotation.class.isAssignableFrom(valueClass)) return AnyField.ANNOTATION;
		if (Proxy.isProxyClass(valueClass)) return AnyField.PROXY;

		BaseMarshaller<?> marshaller = context.findMarshaller(valueClass);
		return hasTypeId(context, marshaller) ? AnyField.IDENTIFIED_OBJECT : AnyField.NAMED_OBJECT;
	}

	private static boolean hasTypeId(ImmutableSerializationContext context, BaseMarshaller<?> marshaller) {
		return context.getDescriptorByName(marshaller.getTypeName()).getTypeId() != null;
	}
}
