/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.TestInvocationHandler;

/**
 * Validates marshalling of java.lang* objects.
 * 
 * @author Paul Ferraro
 */
public abstract class AbstractLangTestCase {

	private final MarshallingTesterFactory factory;

	public AbstractLangTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testBoolean() throws IOException {
		this.factory.createTester().test(true);
	}

	@Test
	public void testByte() throws IOException {
		Tester<Byte> tester = this.factory.createTester();
		for (int i = 0; i < Byte.SIZE; ++i) {
			tester.test(Integer.valueOf((1 << i) - 1).byteValue());
			tester.test(Integer.valueOf(-1 << i).byteValue());
		}
	}

	@Test
	public void testShort() throws IOException {
		Tester<Short> tester = this.factory.createTester();
		for (int i = 0; i < Short.SIZE; ++i) {
			tester.test(Integer.valueOf((1 << i) - 1).shortValue());
			tester.test(Integer.valueOf(-1 << i).shortValue());
		}
	}

	@Test
	public void testInteger() throws IOException {
		Tester<Integer> tester = this.factory.createTester();
		for (int i = 0; i < Integer.SIZE; ++i) {
			tester.test((1 << i) - 1);
			tester.test(-1 << i);
		}
	}

	@Test
	public void testLong() throws IOException {
		Tester<Long> tester = this.factory.createTester();
		for (int i = 0; i < Long.SIZE; ++i) {
			tester.test((1L << i) - 1L);
			tester.test(-1L << i);
		}
	}

	@Test
	public void testFloat() throws IOException {
		Tester<Float> tester = this.factory.createTester();
		tester.test(Float.NEGATIVE_INFINITY);
		tester.test(Float.MIN_VALUE);
		tester.test(0F);
		tester.test(Float.MAX_VALUE);
		tester.test(Float.POSITIVE_INFINITY);
		tester.test(Float.NaN);
	}

	@Test
	public void testDouble() throws IOException {
		Tester<Double> tester = this.factory.createTester();
		tester.test(Double.NEGATIVE_INFINITY);
		tester.test(Double.MIN_VALUE);
		tester.test(0D);
		tester.test(Double.MAX_VALUE);
		tester.test(Double.POSITIVE_INFINITY);
		tester.test(Double.NaN);
	}

	@Test
	public void testCharacter() throws IOException {
		Tester<Character> tester = this.factory.createTester();
		tester.test(Character.MIN_VALUE);
		tester.test('A');
		tester.test(Character.MAX_VALUE);
	}

	@Test
	public void testString() throws IOException {
		Tester<String> tester = this.factory.createTester();
		tester.test("A");
		tester.test(UUID.randomUUID().toString());
	}

	@Test
	public void testBooleanArray() throws IOException {
		boolean[] array = new boolean[] { true, false };
		this.factory.<boolean[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<boolean[][]> createTester().test(new boolean[][] { array, array }, Assertions::assertArrayEquals);
		Boolean[] objectArray = new Boolean[] { true, false };
		this.factory.<Boolean[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Boolean[][]> createTester().test(new Boolean[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testByteArray() throws IOException {
		byte[] array = new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.<byte[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<byte[][]> createTester().test(new byte[][] { array, array }, Assertions::assertArrayEquals);
		Byte[] objectArray = new Byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.<Byte[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Byte[][]> createTester().test(new Byte[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testShortArray() throws IOException {
		short[] array = new short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.<short[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<short[][]> createTester().test(new short[][] { array, array }, Assertions::assertArrayEquals);
		Short[] objectArray = new Short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.<Short[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Short[][]> createTester().test(new Short[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testIntegerArray() throws IOException {
		int[] array = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.<int[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<int[][]> createTester().test(new int[][] { array, array }, Assertions::assertArrayEquals);
		Integer[] objectArray = new Integer[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.<Integer[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Integer[][]> createTester().test(new Integer[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testLongArray() throws IOException {
		long[] array = new long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.<long[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<long[][]> createTester().test(new long[][] { array, array }, Assertions::assertArrayEquals);
		Long[] objectArray = new Long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.<Long[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Long[][]> createTester().test(new Long[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testFloatArray() throws IOException {
		float[] array = new float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.<float[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<float[][]> createTester().test(new float[][] { array, array }, Assertions::assertArrayEquals);
		Float[] objectArray = new Float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.<Float[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Float[][]> createTester().test(new Float[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testDoubleArray() throws IOException {
		double[] array = new double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.<double[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<double[][]> createTester().test(new double[][] { array, array }, Assertions::assertArrayEquals);
		Double[] objectArray = new Double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.<Double[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Double[][]> createTester().test(new Double[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testCharArray() throws IOException {
		char[] array = new char[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.<char[]> createTester().test(array, Assertions::assertArrayEquals);
		this.factory.<char[][]> createTester().test(new char[][] { array, array }, Assertions::assertArrayEquals);
		Character[] objectArray = new Character[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.<Character[]> createTester().test(objectArray, Assertions::assertArrayEquals);
		this.factory.<Character[][]> createTester().test(new Character[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { objectArray, objectArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testObjectArray() throws IOException {
		String string1 = "foo";
		String string2 = "bar";
		String[] stringArray = new String[] { string1, string2 };
		this.factory.<String[]> createTester().test(stringArray, Assertions::assertArrayEquals);
		// Test array with shared object references
		this.factory.<String[]> createTester().test(new String[] { string1, string1 }, Assertions::assertArrayEquals);
		this.factory.<String[][]> createTester().test(new String[][] { stringArray, stringArray }, Assertions::assertArrayEquals);
		this.factory.<Object[][]> createTester().test(new Object[][] { stringArray, stringArray }, Assertions::assertArrayEquals);
	}

	@Test
	public void testNull() throws IOException {
		this.factory.createTester().test(null, Assertions::assertSame);
	}

	@Test
	public void testClass() throws IOException {
		Tester<Class<?>> tester = this.factory.createTester();
		tester.test(Object.class, Assertions::assertSame);
		tester.test(Integer.class, Assertions::assertSame);
		tester.test(Throwable.class, Assertions::assertSame);
		tester.test(Exception.class, Assertions::assertSame);
	}

	@Test
	public void testException() throws IOException {
		Throwable exception = new RuntimeException("foo");
		exception.setStackTrace(new StackTraceElement[] { exception.getStackTrace()[0] });
		Throwable cause = new Exception("bar");
		cause.setStackTrace(new StackTraceElement[] { cause.getStackTrace()[0] });
		Throwable suppressed = new Error("baz");
		suppressed.setStackTrace(new StackTraceElement[] { suppressed.getStackTrace()[0] });
		exception.initCause(cause);
		exception.addSuppressed(suppressed);
		this.factory.<Throwable>createTester().test(exception, AbstractLangTestCase::assertEquals);
	}

	@Test
	public void testStackTrace() throws IOException {
		this.factory.<StackTraceElement>createTester().test(new StackTraceElement("class", "method", "file", -1), AbstractLangTestCase::assertEquals);
		this.factory.<StackTraceElement>createTester().test(new StackTraceElement("loader", "module", "1.0", "class", "method", "file", 1), AbstractLangTestCase::assertEquals);
	}

	@Test
	public void testProxy() throws IOException {
		Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] { Iterable.class }, new TestInvocationHandler("foo"));

		this.factory.createTester().test(proxy, AbstractLangTestCase::assertProxyEquals);
	}

	private static void assertProxyEquals(Object expected, Object actual) {
		assertTrue(Proxy.isProxyClass(actual.getClass()));
		TestInvocationHandler actualHandler = (TestInvocationHandler) Proxy.getInvocationHandler(actual);
		TestInvocationHandler expectedHandler = (TestInvocationHandler) Proxy.getInvocationHandler(expected);
		Assertions.assertEquals(expectedHandler.getValue(), actualHandler.getValue());
	}

	private static void assertEquals(Throwable expected, Throwable actual) {
		Assertions.assertEquals(expected.getMessage(), actual.getMessage());

		StackTraceElement[] expectedStackTrace = expected.getStackTrace();
		StackTraceElement[] actualStackTrace = expected.getStackTrace();
		Assertions.assertEquals(expectedStackTrace.length, actualStackTrace.length);
		for (int i = 0; i < expectedStackTrace.length; ++i) {
			assertEquals(expectedStackTrace[i], actualStackTrace[i]);
		}

		Throwable[] expectedSuppressed = expected.getSuppressed();
		Throwable[] actualSuppressed = actual.getSuppressed();
		Assertions.assertEquals(expectedSuppressed.length, actualSuppressed.length);
		for (int i = 0; i < expectedSuppressed.length; ++i) {
			assertEquals(expectedSuppressed[i], actualSuppressed[i]);
		}

		Throwable cause1 = expected.getCause();
		Throwable cause2 = actual.getCause();
		if ((cause1 != null) && (cause2 != null)) {
			assertEquals(cause1, cause2);
		} else {
			assertSame(cause1, cause2);
		}
	}

	private static void assertEquals(StackTraceElement expected, StackTraceElement actual) {
		Assertions.assertEquals(expected.getClassName(), actual.getClassName());
		Assertions.assertEquals(expected.getMethodName(), actual.getMethodName());
		Assertions.assertEquals(expected.getFileName(), actual.getFileName());
		Assertions.assertEquals(expected.getLineNumber(), actual.getLineNumber());
		Assertions.assertEquals(expected.getClassLoaderName(), actual.getClassLoaderName());
		Assertions.assertEquals(expected.getModuleName(), actual.getModuleName());
		Assertions.assertEquals(expected.getModuleVersion(), actual.getModuleVersion());
	}
}
