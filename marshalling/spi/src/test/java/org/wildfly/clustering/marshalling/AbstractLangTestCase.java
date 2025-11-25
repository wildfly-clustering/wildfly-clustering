/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.lang.reflect.Proxy;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Boolean2DArrayAssert;
import org.assertj.core.api.BooleanArrayAssert;
import org.assertj.core.api.Byte2DArrayAssert;
import org.assertj.core.api.ByteArrayAssert;
import org.assertj.core.api.Char2DArrayAssert;
import org.assertj.core.api.CharArrayAssert;
import org.assertj.core.api.Double2DArrayAssert;
import org.assertj.core.api.DoubleArrayAssert;
import org.assertj.core.api.Float2DArrayAssert;
import org.assertj.core.api.FloatArrayAssert;
import org.assertj.core.api.Int2DArrayAssert;
import org.assertj.core.api.IntArrayAssert;
import org.assertj.core.api.Long2DArrayAssert;
import org.assertj.core.api.LongArrayAssert;
import org.assertj.core.api.Short2DArrayAssert;
import org.assertj.core.api.ShortArrayAssert;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.TestInvocationHandler;
import org.wildfly.clustering.marshalling.test.TestRecord;

/**
 * Validates marshalling of java.lang* objects.
 * @author Paul Ferraro
 */
public abstract class AbstractLangTestCase {

	private final Random random = new Random();
	private final MarshallingTesterFactory factory;

	public AbstractLangTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testUnmarshallable() {
		Tester<ThreadLocal<String>> tester = this.factory.createTester();
		tester.reject(ThreadLocal.withInitial(() -> "foo"));
	}

	@Test
	public void testBoolean() {
		this.factory.createTester().accept(true);
	}

	@Test
	public void testByte() {
		Consumer<Byte> tester = this.factory.createTester();
		for (int i = 0; i < Byte.SIZE; ++i) {
			tester.accept(Integer.valueOf((1 << i) - 1).byteValue());
			tester.accept(Integer.valueOf(-1 << i).byteValue());
		}
	}

	@Test
	public void testShort() {
		Consumer<Short> tester = this.factory.createTester();
		for (int i = 0; i < Short.SIZE; ++i) {
			tester.accept(Integer.valueOf((1 << i) - 1).shortValue());
			tester.accept(Integer.valueOf(-1 << i).shortValue());
		}
	}

	@Test
	public void testInteger() {
		Consumer<Integer> tester = this.factory.createTester();
		for (int i = 0; i < Integer.SIZE; ++i) {
			tester.accept(this.random.nextInt());
		}
	}

	@Test
	public void testLong() {
		Consumer<Long> tester = this.factory.createTester();
		for (int i = 0; i < Long.SIZE; ++i) {
			tester.accept(this.random.nextLong());
		}
	}

	@Test
	public void testFloat() {
		Consumer<Float> tester = this.factory.createTester();
		tester.accept(Float.NEGATIVE_INFINITY);
		tester.accept(Float.MIN_VALUE);
		tester.accept(0F);
		tester.accept(Float.MAX_VALUE);
		tester.accept(Float.POSITIVE_INFINITY);
		tester.accept(Float.NaN);
		tester.accept(this.random.nextFloat());
	}

	@Test
	public void testDouble() {
		Consumer<Double> tester = this.factory.createTester();
		tester.accept(Double.NEGATIVE_INFINITY);
		tester.accept(Double.MIN_VALUE);
		tester.accept(0D);
		tester.accept(Double.MAX_VALUE);
		tester.accept(Double.POSITIVE_INFINITY);
		tester.accept(Double.NaN);
		tester.accept(this.random.nextDouble());
	}

	@Test
	public void testCharacter() {
		Consumer<Character> tester = this.factory.createTester();
		tester.accept(Character.MIN_VALUE);
		tester.accept('A');
		tester.accept(Character.MAX_VALUE);
	}

	@Test
	public void testString() {
		Consumer<String> tester = this.factory.createTester();
		tester.accept("A");
		tester.accept(UUID.randomUUID().toString());
	}

	@Test
	public void testBooleanArray() {
		boolean[] array = new boolean[] { true, false };
		this.factory.createTester(BooleanArrayAssert::new, BooleanArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Boolean2DArrayAssert::new, Boolean2DArrayAssert::isDeepEqualTo).accept(new boolean[][] { array, array });
		Boolean[] objectArray = new Boolean[] { true, false };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Boolean[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testByteArray() {
		byte[] array = new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.createTester(ByteArrayAssert::new, ByteArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Byte2DArrayAssert::new, Byte2DArrayAssert::isDeepEqualTo).accept(new byte[][] { array, array });
		Byte[] objectArray = new Byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Byte[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testShortArray() {
		short[] array = new short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.createTester(ShortArrayAssert::new, ShortArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Short2DArrayAssert::new, Short2DArrayAssert::isDeepEqualTo).accept(new short[][] { array, array });
		Short[] objectArray = new Short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Short[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testIntegerArray() {
		int[] array = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.<IntArrayAssert, int[]>createTester(IntArrayAssert::new, IntArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Int2DArrayAssert::new, Int2DArrayAssert::isDeepEqualTo).accept(new int[][] { array, array });
		Integer[] objectArray = new Integer[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Integer[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testLongArray() {
		long[] array = new long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.<LongArrayAssert, long[]>createTester(LongArrayAssert::new, LongArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Long2DArrayAssert::new, Long2DArrayAssert::isDeepEqualTo).accept(new long[][] { array, array });
		Long[] objectArray = new Long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Long[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testFloatArray() {
		float[] array = new float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.createTester(FloatArrayAssert::new, FloatArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Float2DArrayAssert::new, Float2DArrayAssert::isDeepEqualTo).accept(new float[][] { array, array });
		Float[] objectArray = new Float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Float[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testDoubleArray() {
		double[] array = new double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.createTester(DoubleArrayAssert::new, DoubleArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Double2DArrayAssert::new, Double2DArrayAssert::isDeepEqualTo).accept(new double[][] { array, array });
		Double[] objectArray = new Double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Double[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testCharArray() {
		char[] array = new char[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.createTester(CharArrayAssert::new, CharArrayAssert::containsExactly).accept(array);
		this.factory.createTester(Char2DArrayAssert::new, Char2DArrayAssert::isDeepEqualTo).accept(new char[][] { array, array });
		Character[] objectArray = new Character[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.createArrayTester().accept(objectArray);
		this.factory.create2DArrayTester().accept(new Character[][] { objectArray, objectArray });
		this.factory.create2DArrayTester().accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testObjectArray() {
		String string1 = "foo";
		String string2 = "bar";
		String[] stringArray = new String[] { string1, string2 };
		this.factory.createArrayTester().accept(stringArray);
		// Test array with shared object references
		this.factory.createArrayTester().accept(new String[] { string1, string1 });
		this.factory.create2DArrayTester().accept(new String[][] { stringArray, stringArray });
		this.factory.create2DArrayTester().accept(new Object[][] { stringArray, stringArray });
	}

	@Test
	public void testNull() {
		this.factory.createIdentityTester().accept(null);
	}

	@Test
	public void testClass() {
		Consumer<Class<?>> tester = this.factory.createIdentityTester();
		tester.accept(Object.class);
		tester.accept(Integer.class);
		tester.accept(Throwable.class);
		tester.accept(Exception.class);
	}

	@Test
	public void testException() {
		Throwable exception = new RuntimeException("foo");
		exception.setStackTrace(new StackTraceElement[] { exception.getStackTrace()[0] });
		Throwable cause = new Exception("bar");
		cause.setStackTrace(new StackTraceElement[] { cause.getStackTrace()[0] });
		Throwable suppressed = new Error("baz");
		suppressed.setStackTrace(new StackTraceElement[] { suppressed.getStackTrace()[0] });
		exception.initCause(cause);
		exception.addSuppressed(suppressed);
		this.factory.<Throwable>createTester(AbstractLangTestCase::assertEquals).accept(exception);
	}

	@Test
	public void testStackTrace() {
		this.factory.<StackTraceElement>createTester(AbstractLangTestCase::assertEquals).accept(new StackTraceElement("class", "method", "file", -1));
		this.factory.<StackTraceElement>createTester(AbstractLangTestCase::assertEquals).accept(new StackTraceElement("loader", "module", "1.0", "class", "method", "file", 1));
	}

	@Test
	public void testProxy() {
		Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] { Iterable.class }, new TestInvocationHandler("foo"));

		this.factory.createTester(AbstractLangTestCase::assertProxyEquals).accept(proxy);
	}

	@Test
	public void testRecord() {
		Tester<TestRecord> tester = this.factory.createTester();
		tester.accept(new TestRecord("foo", null));
		tester.accept(new TestRecord("foo", this.random.nextInt()));
	}

	private static void assertProxyEquals(Object expected, Object actual) {
		Assertions.assertThat(actual).isNotNull();
		Assertions.assertThat(Proxy.isProxyClass(actual.getClass())).isTrue();
		Assertions.assertThat(Proxy.getInvocationHandler(actual)).isInstanceOf(TestInvocationHandler.class);
		TestInvocationHandler actualHandler = (TestInvocationHandler) Proxy.getInvocationHandler(actual);
		TestInvocationHandler expectedHandler = (TestInvocationHandler) Proxy.getInvocationHandler(expected);
		Assertions.assertThat(actualHandler.getValue()).isEqualTo(expectedHandler.getValue());
	}

	private static void assertEquals(Throwable expected, Throwable actual) {
		Assertions.assertThat(actual.getMessage()).isEqualTo(expected.getMessage());

		StackTraceElement[] expectedStackTrace = expected.getStackTrace();
		StackTraceElement[] actualStackTrace = expected.getStackTrace();
		Assertions.assertThat(actualStackTrace).hasSameSizeAs(expectedStackTrace);
		for (int i = 0; i < actualStackTrace.length; ++i) {
			assertEquals(expectedStackTrace[i], actualStackTrace[i]);
		}

		Throwable[] expectedSuppressed = expected.getSuppressed();
		Throwable[] actualSuppressed = actual.getSuppressed();
		Assertions.assertThat(actualSuppressed).hasSameSizeAs(expectedSuppressed);
		for (int i = 0; i < actualSuppressed.length; ++i) {
			assertEquals(expectedSuppressed[i], actualSuppressed[i]);
		}

		if (expected.getCause() != null) {
			Assertions.assertThat(actual.getCause()).isNotNull();
			assertEquals(expected.getCause(), actual.getCause());
		} else {
			Assertions.assertThat(actual.getCause()).isNull();
		}
	}

	private static void assertEquals(StackTraceElement expected, StackTraceElement actual) {
		Assertions.assertThat(actual.getClassName()).isEqualTo(expected.getClassName());
		Assertions.assertThat(actual.getMethodName()).isEqualTo(expected.getMethodName());
		Assertions.assertThat(actual.getFileName()).isEqualTo(expected.getFileName());
		Assertions.assertThat(actual.getLineNumber()).isEqualTo(expected.getLineNumber());
		Assertions.assertThat(actual.getClassLoaderName()).isEqualTo(expected.getClassLoaderName());
		Assertions.assertThat(actual.getModuleName()).isEqualTo(expected.getModuleName());
		Assertions.assertThat(actual.getModuleVersion()).isEqualTo(expected.getModuleVersion());
	}
}
