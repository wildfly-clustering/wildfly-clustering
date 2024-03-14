/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.TestInvocationHandler;

/**
 * Validates marshalling of java.lang* objects.
 * @author Paul Ferraro
 */
public abstract class AbstractLangTestCase {

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
			tester.accept((1 << i) - 1);
			tester.accept(-1 << i);
		}
	}

	@Test
	public void testLong() {
		Consumer<Long> tester = this.factory.createTester();
		for (int i = 0; i < Long.SIZE; ++i) {
			tester.accept((1L << i) - 1L);
			tester.accept(-1L << i);
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
		this.factory.<boolean[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<boolean[][]>createTester(Assertions::assertArrayEquals).accept(new boolean[][] { array, array });
		Boolean[] objectArray = new Boolean[] { true, false };
		this.factory.<Boolean[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Boolean[][]>createTester(Assertions::assertArrayEquals).accept(new Boolean[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testByteArray() {
		byte[] array = new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.<byte[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<byte[][]>createTester(Assertions::assertArrayEquals).accept(new byte[][] { array, array });
		Byte[] objectArray = new Byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
		this.factory.<Byte[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Byte[][]>createTester(Assertions::assertArrayEquals).accept(new Byte[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testShortArray() {
		short[] array = new short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.<short[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<short[][]>createTester(Assertions::assertArrayEquals).accept(new short[][] { array, array });
		Short[] objectArray = new Short[] { Short.MIN_VALUE, 0, Short.MAX_VALUE };
		this.factory.<Short[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Short[][]>createTester(Assertions::assertArrayEquals).accept(new Short[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testIntegerArray() {
		int[] array = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.<int[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<int[][]>createTester(Assertions::assertArrayEquals).accept(new int[][] { array, array });
		Integer[] objectArray = new Integer[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		this.factory.<Integer[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Integer[][]>createTester(Assertions::assertArrayEquals).accept(new Integer[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testLongArray() {
		long[] array = new long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.<long[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<long[][]>createTester(Assertions::assertArrayEquals).accept(new long[][] { array, array });
		Long[] objectArray = new Long[] { Long.MIN_VALUE, 0L, Long.MAX_VALUE };
		this.factory.<Long[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Long[][]>createTester(Assertions::assertArrayEquals).accept(new Long[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testFloatArray() {
		float[] array = new float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.<float[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<float[][]>createTester(Assertions::assertArrayEquals).accept(new float[][] { array, array });
		Float[] objectArray = new Float[] { Float.MIN_VALUE, 0f, Float.MAX_VALUE };
		this.factory.<Float[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Float[][]>createTester(Assertions::assertArrayEquals).accept(new Float[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testDoubleArray() {
		double[] array = new double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.<double[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<double[][]>createTester(Assertions::assertArrayEquals).accept(new double[][] { array, array });
		Double[] objectArray = new Double[] { Double.MIN_VALUE, 0d, Double.MAX_VALUE };
		this.factory.<Double[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Double[][]>createTester(Assertions::assertArrayEquals).accept(new Double[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testCharArray() {
		char[] array = new char[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.<char[]>createTester(Assertions::assertArrayEquals).accept(array);
		this.factory.<char[][]>createTester(Assertions::assertArrayEquals).accept(new char[][] { array, array });
		Character[] objectArray = new Character[] { Character.MIN_VALUE, 'A', Character.MAX_VALUE };
		this.factory.<Character[]>createTester(Assertions::assertArrayEquals).accept(objectArray);
		this.factory.<Character[][]>createTester(Assertions::assertArrayEquals).accept(new Character[][] { objectArray, objectArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { objectArray, objectArray });
	}

	@Test
	public void testObjectArray() {
		String string1 = "foo";
		String string2 = "bar";
		String[] stringArray = new String[] { string1, string2 };
		this.factory.<String[]>createTester(Assertions::assertArrayEquals).accept(stringArray);
		// Test array with shared object references
		this.factory.<String[]>createTester(Assertions::assertArrayEquals).accept(new String[] { string1, string1 });
		this.factory.<String[][]>createTester(Assertions::assertArrayEquals).accept(new String[][] { stringArray, stringArray });
		this.factory.<Object[][]>createTester(Assertions::assertArrayEquals).accept(new Object[][] { stringArray, stringArray });
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
