/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;

/**
 * A marshaller for the fields of a {@link ClassLoader}.
 * @author Paul Ferraro
 */
public interface ClassLoaderMarshaller extends FieldSetMarshaller.Simple<ClassLoader> {

	/**
	 * Creates a simple marshaller for the specified class loader.
	 * @param loader a class loader
	 * @return a simple marshaller for the specified class loader.
	 */
	static ClassLoaderMarshaller of(ClassLoader loader) {
		return new ClassLoaderMarshaller() {
			@Override
			public ClassLoader createInitialValue() {
				return loader;
			}

			@Override
			public ClassLoader readFrom(ProtoStreamReader reader, int index, WireType type, ClassLoader current) throws IOException {
				reader.skipField(type);
				return loader;
			}

			@Override
			public int getFields() {
				return 0;
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, ClassLoader value) {
			}
		};
	}
}
