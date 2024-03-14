/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractMathTestCase {

	private final MarshallingTesterFactory factory;
	private final Random random = new Random(System.currentTimeMillis());

	public AbstractMathTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	private BigInteger probablePrime() {
		return BigInteger.probablePrime(Byte.MAX_VALUE, this.random);
	}

	@Test
	public void testBigInteger() {
		this.testBigInteger(BigInteger.ZERO);
		this.testBigInteger(BigInteger.ONE);
		this.testBigInteger(BigInteger.TEN);
		this.testBigInteger(this.probablePrime());
	}

	private void testBigInteger(BigInteger value) {
		Consumer<BigInteger> tester = this.factory.createTester();
		tester.accept(value);
		tester.accept(value.negate());
	}

	@Test
	public void testBigDecimal() {
		this.testBigDecimal(BigDecimal.ZERO);
		this.testBigDecimal(BigDecimal.ONE);
		this.testBigDecimal(BigDecimal.TEN);
		this.testBigDecimal(new BigDecimal(this.probablePrime(), Integer.MAX_VALUE));
		this.testBigDecimal(new BigDecimal(this.probablePrime(), Integer.MIN_VALUE));
	}

	private void testBigDecimal(BigDecimal value) {
		Consumer<BigDecimal> tester = this.factory.createTester();
		tester.accept(value);
		tester.accept(value.negate());
	}

	@Test
	public void testMathContext() {
		Consumer<MathContext> tester = this.factory.createTester();
		tester.accept(new MathContext(0));
		tester.accept(new MathContext(10, RoundingMode.UNNECESSARY));
	}

	@Test
	public void testRoundingMode() {
		this.factory.createTester(RoundingMode.class).run();
	}
}
