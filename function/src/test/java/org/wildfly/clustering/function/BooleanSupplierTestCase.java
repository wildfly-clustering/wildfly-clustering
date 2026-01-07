/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BooleanSupplier}.
 * @author Paul Ferraro
 */
public class BooleanSupplierTestCase {

	@Test
	public void of() {
		assertThat(BooleanSupplier.of(true).getAsBoolean()).isEqualTo(true);
		assertThat(BooleanSupplier.of(false).getAsBoolean()).isEqualTo(false);
	}
}
