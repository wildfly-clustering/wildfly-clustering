/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderResolver;
import org.wildfly.clustering.marshalling.protostream.Field;
import org.wildfly.clustering.marshalling.protostream.FieldMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.ScalarClass;

/**
 * A field for a resolved class.
 * @author Paul Ferraro
 */
public class ResolvedClassField implements Field<Class<?>>, FieldMarshaller<Class<?>> {
	private final ClassLoaderResolver resolver;
	private final int classNameIndex;
	private final int classLoaderIndex;

	ResolvedClassField(ClassLoaderResolver resolver, int index) {
		this.resolver = resolver;
		this.classNameIndex = index;
		this.classLoaderIndex = index + 1;
	}

	@Override
	public int getIndex() {
		return this.classNameIndex;
	}

	@Override
	public FieldMarshaller<Class<?>> getMarshaller() {
		return this;
	}

	@Override
	public Class<? extends Class<?>> getJavaClass() {
		return ScalarClass.ANY.getJavaClass();
	}

	@Override
	public WireType getWireType() {
		return WireType.LENGTH_DELIMITED;
	}

	@Override
	public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
		String className = Scalar.STRING.cast(String.class).readFrom(reader);
		ClassLoader loader = this.resolver.getDefaultClassLoader();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			if (WireType.getTagFieldNumber(tag) == this.classLoaderIndex) {
				loader = this.resolver.resolve((reader.readString()));
			} else {
				reader.skipField(tag);
			}
		}
		try {
			return loader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(className);
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Class<?> targetClass) throws IOException {
		Scalar.STRING.writeTo(writer, targetClass.getName());
		if (Privileged.getClassLoader(targetClass) != this.resolver.getDefaultClassLoader()) {
			String classLoaderName = this.resolver.classLoaderName(targetClass);
			if (classLoaderName != null) {
				writer.writeString(this.classLoaderIndex, classLoaderName);
			}
		}
	}
}
