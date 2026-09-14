/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;

/**
 * Simplified {@link java.io.ObjectOutput} interface
 * @author Paul Ferraro
 */
public interface ObjectOutput extends DataOutput, java.io.ObjectOutput {

	@Override
	default void write(int b) throws IOException {
		DataOutput.super.write(b);
	}

	@Override
	default void write(byte[] bytes) throws IOException {
		DataOutput.super.write(bytes);
	}
}
