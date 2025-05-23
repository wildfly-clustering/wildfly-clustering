/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.DataOutput;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.wildfly.clustering.function.Consumer;

/**
 * {@link DataOutput} implementation used to write the unexposed serializable fields of an object.
 * @author Paul Ferraro
 */
public class SimpleDataOutput implements DataOutput {

	private final Consumer<String> stringConsumer;
	private final Consumer<ByteBuffer> bufferConsumer;
	private final Consumer<Character> charConsumer;
	private final Consumer<Boolean> booleanConsumer;
	private final Consumer<Byte> byteConsumer;
	private final Consumer<Short> shortConsumer;
	private final IntConsumer intConsumer;
	private final LongConsumer longConsumer;
	private final Consumer<Float> floatConsumer;
	private final DoubleConsumer doubleConsumer;

	SimpleDataOutput(Builder builder) {
		this.stringConsumer = builder.stringConsumer;
		this.bufferConsumer = builder.bufferConsumer;
		this.charConsumer = builder.charConsumer;
		this.booleanConsumer = builder.booleanConsumer;
		this.byteConsumer = builder.byteConsumer;
		this.shortConsumer = builder.shortConsumer;
		this.intConsumer = builder.intConsumer;
		this.longConsumer = builder.longConsumer;
		this.floatConsumer = builder.floatConsumer;
		this.doubleConsumer = builder.doubleConsumer;
	}

	@Override
	public void writeInt(int value) {
		this.intConsumer.accept(value);
	}

	@Override
	public void writeLong(long value) {
		this.longConsumer.accept(value);
	}

	@Override
	public void writeDouble(double value) {
		this.doubleConsumer.accept(value);
	}

	@Override
	public void writeUTF(String value) {
		this.stringConsumer.accept(value);
	}

	@Override
	public void write(byte[] buffer) {
		this.bufferConsumer.accept(ByteBuffer.wrap(buffer));
	}

	@Override
	public void write(byte[] buffer, int offset, int length) {
		this.bufferConsumer.accept(ByteBuffer.wrap(buffer, offset, length));
	}

	@Override
	public void write(int value) {
		this.writeByte(value);
	}

	@Override
	public void writeBoolean(boolean value) {
		this.booleanConsumer.accept(value);
	}

	@Override
	public void writeByte(int value) {
		this.byteConsumer.accept((byte) value);
	}

	@Override
	public void writeShort(int value) {
		this.shortConsumer.accept((short) value);
	}

	@Override
	public void writeChar(int value) {
		this.charConsumer.accept((char) value);
	}

	@Override
	public void writeFloat(float value) {
		this.floatConsumer.accept(value);
	}

	@Override
	public void writeBytes(String value) {
		this.stringConsumer.accept(value);
	}

	@Override
	public void writeChars(String value) {
		this.stringConsumer.accept(value);
	}

	public static class Builder {
		Consumer<String> stringConsumer = Consumer.empty();
		Consumer<Character> charConsumer = Consumer.empty();
		Consumer<ByteBuffer> bufferConsumer = Consumer.empty();
		Consumer<Boolean> booleanConsumer = Consumer.empty();
		Consumer<Byte> byteConsumer = Consumer.empty();
		Consumer<Short> shortConsumer = Consumer.empty();
		IntConsumer intConsumer = DiscardingConsumer.INSTANCE;
		LongConsumer longConsumer = DiscardingConsumer.INSTANCE;
		Consumer<Float> floatConsumer = Consumer.empty();
		DoubleConsumer doubleConsumer = DiscardingConsumer.INSTANCE;

		public Builder with(String[] values) {
			this.stringConsumer = new ArrayConsumer<>(values);
			return this;
		}

		public Builder with(char[] values) {
			this.charConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		public Builder with(ByteBuffer[] values) {
			this.bufferConsumer = new ArrayConsumer<>(values);
			return this;
		}

		public Builder with(boolean[] values) {
			this.booleanConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		public Builder with(byte[] values) {
			this.byteConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		public Builder with(short[] values) {
			this.shortConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		public Builder with(int[] values) {
			this.intConsumer = new IntArrayConsumer(values);
			return this;
		}

		public Builder with(long[] values) {
			this.longConsumer = new LongArrayConsumer(values);
			return this;
		}

		public Builder with(float[] values) {
			this.floatConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		public Builder with(double[] values) {
			this.doubleConsumer = new DoubleArrayConsumer(values);
			return this;
		}

		public DataOutput build() {
			return new SimpleDataOutput(this);
		}
	}

	enum DiscardingConsumer implements IntConsumer, LongConsumer, DoubleConsumer {
		INSTANCE;

		@Override
		public void accept(long value) {
		}

		@Override
		public void accept(int value) {
		}

		@Override
		public void accept(double value) {
		}
	}

	public static class ArrayConsumer<T> implements Consumer<T> {
		private T[] values;
		private int index = 0;

		ArrayConsumer(T[] values) {
			this.values = values;
		}

		@Override
		public void accept(T value) {
			this.values[this.index++] = value;
		}
	}

	public static class GenericArrayConsumer<T> implements Consumer<T> {
		private Object values;
		private int index = 0;

		GenericArrayConsumer(Object values) {
			this.values = values;
		}

		@Override
		public void accept(T value) {
			Array.set(this.values, this.index++, value);
		}
	}

	static class IntArrayConsumer implements IntConsumer {
		private int[] values;
		private int index = 0;

		IntArrayConsumer(int[] values) {
			this.values = values;
		}

		@Override
		public void accept(int value) {
			this.values[this.index++] = value;
		}
	}

	static class LongArrayConsumer implements LongConsumer {
		private long[] values;
		private int index = 0;

		LongArrayConsumer(long[] values) {
			this.values = values;
		}

		@Override
		public void accept(long value) {
			this.values[this.index++] = value;
		}
	}

	static class DoubleArrayConsumer implements DoubleConsumer {
		private double[] values;
		private int index = 0;

		DoubleArrayConsumer(double[] values) {
			this.values = values;
		}

		@Override
		public void accept(double value) {
			this.values[this.index++] = value;
		}
	}
}
