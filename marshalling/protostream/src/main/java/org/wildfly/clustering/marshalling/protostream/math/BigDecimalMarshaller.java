/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.math;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for {@link BigDecimal}.
 * @author Paul Ferraro
 */
public class BigDecimalMarshaller implements ProtoStreamMarshaller<BigDecimal> {

	private static final int UNSCALED_VALUE_INDEX = 1;
	private static final int SCALE_INDEX = 2;

	private static final int DEFAULT_SCALE = 0;

	@Override
	public BigDecimal readFrom(ProtoStreamReader reader) throws IOException {
		BigInteger unscaledValue = BigInteger.ZERO;
		int scale = DEFAULT_SCALE;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case UNSCALED_VALUE_INDEX -> {
					unscaledValue = new BigInteger(reader.readByteArray());
				}
				case SCALE_INDEX -> {
					scale = reader.readSInt32();
				}
				default -> reader.skipField(tag);
			}
		}
		return new BigDecimal(unscaledValue, scale);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, BigDecimal value) throws IOException {
		BigInteger unscaledValue = value.unscaledValue();
		if (!unscaledValue.equals(BigInteger.ZERO)) {
			writer.writeBytes(UNSCALED_VALUE_INDEX, unscaledValue.toByteArray());
		}
		int scale = value.scale();
		if (scale != DEFAULT_SCALE) {
			writer.writeSInt32(SCALE_INDEX, scale);
		}
	}

	@Override
	public Class<? extends BigDecimal> getJavaClass() {
		return BigDecimal.class;
	}
}
