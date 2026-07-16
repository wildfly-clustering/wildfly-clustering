/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang.invoke;

import java.io.IOException;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.marshalling.protostream.FieldSetMarshaller;
import org.wildfly.clustering.marshalling.protostream.FieldSetReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a serialized lambda.
 * @author Paul Ferraro
 */
enum SerializedLambdaMarshaller implements ProtoStreamMarshaller<SerializedLambda> {
	INSTANCE;

	@SuppressWarnings("unchecked")
	private static final Function<SerializedLambda, Class<?>> SERIALIZED_LAMBDA_CAPTURING_CLASS_HANDLE = getFieldHandle(SerializedLambda.class, (Class<Class<?>>) (Class<?>) Class.class);

	private static final FieldSetMarshaller<Descriptor, Descriptor> METHOD_DESCRIPTOR_MARSHALLER = new FieldSetMarshaller.Simple<>() {
		private static final int CLASS_NAME_INDEX = 0;
		private static final int METHOD_NAME_INDEX = 1;
		private static final int METHOD_SIGNATURE_INDEX = 2;

		@Override
		public Descriptor createInitialValue() {
			return new Descriptor();
		}

		@Override
		public Descriptor readFrom(ProtoStreamReader reader, int index, WireType type, Descriptor descriptor) throws IOException {
			switch (index) {
				case CLASS_NAME_INDEX -> descriptor.withClassName(reader.readString());
				case METHOD_NAME_INDEX -> descriptor.withMethodName(reader.readString());
				case METHOD_SIGNATURE_INDEX -> descriptor.withMethodSignature(reader.readString());
			}
			return descriptor;
		}

		@Override
		public int getFields() {
			return 3;
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Descriptor descriptor) throws IOException {
			writer.writeString(CLASS_NAME_INDEX, descriptor.getClassName());
			writer.writeString(METHOD_NAME_INDEX, descriptor.getMethodName());
			writer.writeString(METHOD_SIGNATURE_INDEX, descriptor.getMethodSignature());
		}
	};

	private static final int CAPTURING_CLASS_INDEX = 1;
	private static final int METHOD_SIGNATURE_INDEX = 2;
	private static final int METHOD_KIND_INDEX = 3;
	private static final int CAPTURED_ARGUMENT_INDEX = 4;
	private static final int INTERFACE_METHOD_DESCRIPTOR_INDEX = 5;
	private static final int IMPLEMENTATION_METHOD_DESCRIPTOR_INDEX = METHOD_DESCRIPTOR_MARSHALLER.nextIndex(INTERFACE_METHOD_DESCRIPTOR_INDEX);

	private static final int DEFAULT_METHOD_KIND = MethodHandleInfo.REF_invokeStatic; // Most probable type

	static Class<?> getCapturingClass(SerializedLambda lambda) {
		return SERIALIZED_LAMBDA_CAPTURING_CLASS_HANDLE.apply(lambda);
	}

	static Descriptor createMethodDescriptor(SerializedLambda lambda, Function<SerializedLambda, String> className, Function<SerializedLambda, String> methodName, Function<SerializedLambda, String> methodSignature) {
		Descriptor descriptor = new Descriptor().withMethodName(methodName.apply(lambda));
		Optional.of(className.apply(lambda)).filter(Predicate.not(lambda.getCapturingClass()::equals)).ifPresent(descriptor::withClassName);
		Optional.of(methodSignature.apply(lambda)).filter(Predicate.not(lambda.getInstantiatedMethodType()::equals)).ifPresent(descriptor::withMethodSignature);
		return descriptor;
	}

	private static <T, R> Function<T, R> getFieldHandle(Class<T> sourceClass, Class<R> fieldType) {
		for (Field field : sourceClass.getDeclaredFields()) {
			if (field.getType() == fieldType) {
				field.setAccessible(true);
				return new Function<>() {
					@Override
					public R apply(T source) {
						try {
							return fieldType.cast(field.get(source));
						} catch (Throwable e) {
							if (e instanceof RuntimeException exception) {
								throw exception;
							}
							if (e instanceof RuntimeException error) {
								throw error;
							}
							throw new IllegalStateException(e);
						}
					}
				};
			}
		}
		throw new IllegalArgumentException(String.format("%s::%s", sourceClass.getCanonicalName(), fieldType.getCanonicalName()));
	}

	@Override
	public Class<? extends SerializedLambda> getJavaClass() {
		return SerializedLambda.class;
	}

	@Override
	public SerializedLambda readFrom(ProtoStreamReader reader) throws IOException {
		FieldSetReader<Descriptor> functionalInterfaceMethodDescriptorReader = reader.createFieldSetReader(METHOD_DESCRIPTOR_MARSHALLER, INTERFACE_METHOD_DESCRIPTOR_INDEX);
		FieldSetReader<Descriptor> implMethodDescriptorReader = reader.createFieldSetReader(METHOD_DESCRIPTOR_MARSHALLER, IMPLEMENTATION_METHOD_DESCRIPTOR_INDEX);

		Class<?> capturingClass = null;
		String methodSignature = null;
		int methodKind = DEFAULT_METHOD_KIND;
		Descriptor interfaceDescriptor = METHOD_DESCRIPTOR_MARSHALLER.createInitialValue();
		Descriptor implementationDescriptor = METHOD_DESCRIPTOR_MARSHALLER.createInitialValue();
		List<Object> capturedArguments = reader.repeatedElementCollector();

		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case CAPTURING_CLASS_INDEX -> capturingClass = reader.readObject(Class.class);
				case CAPTURED_ARGUMENT_INDEX -> capturedArguments.add(reader.readAny());
				case METHOD_SIGNATURE_INDEX -> methodSignature = reader.readString();
				case METHOD_KIND_INDEX -> methodKind = reader.readUInt32();
				default -> {
					if (functionalInterfaceMethodDescriptorReader.contains(index)) {
						functionalInterfaceMethodDescriptorReader.readField(interfaceDescriptor);
					} else if (implMethodDescriptorReader.contains(index)) {
						implMethodDescriptorReader.readField(implementationDescriptor);
					} else {
						reader.skipField(tag);
					}
				}
			}
		}
		String defaultClassName = capturingClass.getName().replace('.', '/');
		String interfaceClassName = Optional.ofNullable(interfaceDescriptor.getClassName()).orElse(defaultClassName);
		String interfaceMethodName = interfaceDescriptor.getMethodName();
		String interfaceMethodSignature = Optional.ofNullable(interfaceDescriptor.getMethodSignature()).orElse(methodSignature);
		String implementationClassName = Optional.ofNullable(implementationDescriptor.getClassName()).orElse(defaultClassName);
		String implementationMethodName = implementationDescriptor.getMethodName();
		String implementationMethodSignature = Optional.ofNullable(implementationDescriptor.getMethodSignature()).orElse(methodSignature);
		return new SerializedLambda(capturingClass, interfaceClassName, interfaceMethodName, interfaceMethodSignature, methodKind, implementationClassName, implementationMethodName, implementationMethodSignature, methodSignature, capturedArguments.toArray());
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, SerializedLambda lambda) throws IOException {
		writer.writeObject(CAPTURING_CLASS_INDEX, getCapturingClass(lambda));
		writer.writeString(METHOD_SIGNATURE_INDEX, lambda.getInstantiatedMethodType());
		int kind = lambda.getImplMethodKind();
		if (kind != DEFAULT_METHOD_KIND) {
			writer.writeUInt32(METHOD_KIND_INDEX, kind);
		}
		writer.createFieldSetWriter(METHOD_DESCRIPTOR_MARSHALLER, INTERFACE_METHOD_DESCRIPTOR_INDEX).writeFields(createMethodDescriptor(lambda, SerializedLambda::getFunctionalInterfaceClass, SerializedLambda::getFunctionalInterfaceMethodName, SerializedLambda::getFunctionalInterfaceMethodSignature));
		writer.createFieldSetWriter(METHOD_DESCRIPTOR_MARSHALLER, IMPLEMENTATION_METHOD_DESCRIPTOR_INDEX).writeFields(createMethodDescriptor(lambda, SerializedLambda::getImplClass, SerializedLambda::getImplMethodName, SerializedLambda::getImplMethodSignature));
		for (int i = 0; i < lambda.getCapturedArgCount(); ++i) {
			writer.writeAny(CAPTURED_ARGUMENT_INDEX, lambda.getCapturedArg(i));
		}
	}

	static class Descriptor {
		private String className;
		private String methodName;
		private String methodSignature;

		Descriptor withClassName(String className) {
			this.className = className;
			return this;
		}

		Descriptor withMethodName(String methodName) {
			this.methodName = methodName;
			return this;
		}

		Descriptor withMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
			return this;
		}

		String getClassName() {
			return this.className;
		}

		String getMethodName() {
			return this.methodName;
		}

		String getMethodSignature() {
			return this.methodSignature;
		}
	}
}
