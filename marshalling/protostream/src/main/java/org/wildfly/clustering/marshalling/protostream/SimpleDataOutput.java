/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.DataOutput;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.DoubleConsumer;
import org.wildfly.clustering.function.IntConsumer;
import org.wildfly.clustering.function.LongConsumer;

/**
 * {@link DataOutput} implementation used to read the unexposed serializable fields of an object.
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

	/**
	 * Builds a simple data output.
	 */
	public static class Builder {
		Consumer<String> stringConsumer = Consumer.of();
		Consumer<Character> charConsumer = Consumer.of();
		Consumer<ByteBuffer> bufferConsumer = Consumer.of();
		Consumer<Boolean> booleanConsumer = Consumer.of();
		Consumer<Byte> byteConsumer = Consumer.of();
		Consumer<Short> shortConsumer = Consumer.of();
		IntConsumer intConsumer = IntConsumer.of();
		LongConsumer longConsumer = LongConsumer.of();
		Consumer<Float> floatConsumer = Consumer.of();
		DoubleConsumer doubleConsumer = DoubleConsumer.of();

		/**
		 * Creates a builder of a data output.
		 */
		public Builder() {
			// For javadoc comment
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeUTF(String)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(String[] values) {
			this.stringConsumer = new ArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeChar(int)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(char[] values) {
			this.charConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#write(byte[], int, int)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(ByteBuffer[] values) {
			this.bufferConsumer = new ArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeBoolean(boolean)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(boolean[] values) {
			this.booleanConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#write(byte[])}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(byte[] values) {
			this.byteConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeShort(int)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(short[] values) {
			this.shortConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeInt(int)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(int[] values) {
			this.intConsumer = new IntArrayConsumer(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeLong(long)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(long[] values) {
			this.longConsumer = new LongArrayConsumer(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeFloat(float)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(float[] values) {
			this.floatConsumer = new GenericArrayConsumer<>(values);
			return this;
		}

		/**
		 * Specifies the values to be consumed by consecutive calls to {@link DataOutput#writeDouble(double)}.
		 * @param values the consecutive values to be written
		 * @return a reference to this builder
		 */
		public Builder with(double[] values) {
			this.doubleConsumer = new DoubleArrayConsumer(values);
			return this;
		}

		/**
		 * Builds a simple data output.
		 * @return a simple data output.
		 */
		public DataOutput build() {
			return new SimpleDataOutput(this);
		}
	}

	static class ArrayConsumer<T> implements Consumer<T> {
		private T[] values;
		private int index;

		ArrayConsumer(T[] values) {
			this.values = values;
		}

		@Override
		public void accept(T value) {
			this.values[this.index++] = value;
		}
	}

	static class GenericArrayConsumer<T> implements Consumer<T> {
		private Object values;
		private int index;

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
		private int index;

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
		private int index;

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
		private int index;

		DoubleArrayConsumer(double[] values) {
			this.values = values;
		}

		@Override
		public void accept(double value) {
			this.values[this.index++] = value;
		}
	}
}
