/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import static org.assertj.core.api.Assertions.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class SimpleObjectOutputTestCase {

	@Test
	public void test() throws IOException {
		Object[] objects = new Object[2];
		String[] strings = new String[2];
		int[] ints = new int[2];
		long[] longs = new long[2];
		double[] doubles = new double[2];

		ObjectOutput output = new SimpleObjectOutput.Builder().with(objects).with(strings).with(ints).with(longs).with(doubles).build();

		TestExternalizable test = new TestExternalizable();
		test.writeExternal(output);

		for (int i = 0; i < 2; ++i) {
			assertThat(objects[i]).isSameAs(test.objects[i]);
			assertThat(strings[i]).isSameAs(test.strings[i]);
			assertThat(ints[i]).isEqualTo(test.ints[i]);
			assertThat(longs[i]).isEqualTo(test.longs[i]);
			assertThat(doubles[i]).isEqualTo(test.doubles[i]);
		}
	}

	static class TestExternalizable implements Externalizable {

		final Object[] objects = new Object[] { UUID.randomUUID(), UUID.randomUUID() };
		final String[] strings = new String[] { "foo", "bar" };
		final int[] ints = new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE };
		final long[] longs = new long[] { Long.MIN_VALUE, Long.MAX_VALUE };
		final double[] doubles = new double[] { Double.MIN_VALUE, Double.MAX_VALUE };

		@Override
		public void writeExternal(ObjectOutput output) throws IOException {
			for (int i = 0; i < 2; ++i) {
				output.writeObject(this.objects[i]);
				output.writeUTF(this.strings[i]);
				output.writeInt(this.ints[i]);
				output.writeLong(this.longs[i]);
				output.writeDouble(this.doubles[i]);
			}
		}

		@Override
		public void readExternal(ObjectInput input) {
		}
	}
}
