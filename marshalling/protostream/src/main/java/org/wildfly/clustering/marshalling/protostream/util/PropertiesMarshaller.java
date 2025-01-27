/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a {@link Properties} object.
 * @author Paul Ferraro
 */
public class PropertiesMarshaller implements ProtoStreamMarshaller<Properties> {
	private static final int PROPERTY_INDEX = 1;

	@Override
	public Class<? extends Properties> getJavaClass() {
		return Properties.class;
	}

	@Override
	public Properties readFrom(ProtoStreamReader reader) throws IOException {
		Properties properties = new Properties();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case PROPERTY_INDEX:
					Map.Entry<String, String> entry = reader.readObject(Property.class);
					properties.setProperty(entry.getKey(), entry.getValue());
					break;
				default:
					reader.skipField(tag);
			}
		}
		return properties;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Properties properties) throws IOException {
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key);
			writer.writeObject(PROPERTY_INDEX, new Property(key, value));
		}
	}
}
