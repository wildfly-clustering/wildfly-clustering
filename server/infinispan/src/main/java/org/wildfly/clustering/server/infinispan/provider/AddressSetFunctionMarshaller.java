/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.function.AbstractCollectionOperationFunction;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for an function that operates on a {@link Set} of {@link Address} instances.
 * @param <F> the function type
 * @author Paul Ferraro
 */
public class AddressSetFunctionMarshaller<F extends AbstractCollectionOperationFunction<Address, Set<Address>>> implements ProtoStreamMarshaller<F> {
	private static final int ELEMENT_INDEX = 1;

	private final Class<? extends F> targetClass;
	private final Function<Collection<Address>, F> factory;

	/**
	 * Creates an address set function of the specified implementation type
	 * @param targetClass the function implementation type
	 * @param factory the function factory
	 */
	public AddressSetFunctionMarshaller(Class<? extends F> targetClass, Function<Collection<Address>, F> factory) {
		this.targetClass = targetClass;
		this.factory = factory;
	}

	@Override
	public Class<? extends F> getJavaClass() {
		return this.targetClass;
	}

	@Override
	public F readFrom(ProtoStreamReader reader) throws IOException {
		List<Address> operand = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ELEMENT_INDEX -> operand.add(reader.readObject(Address.class));
				default -> reader.skipField(tag);
			}
		}
		// If no element were read, use singleton local address collection.
		return this.factory.apply(operand.isEmpty() ? Set.of(Address.LOCAL) : operand);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, F function) throws IOException {
		Collection<Address> operand = function.getOperand();
		// If operand is a singleton LocalModeAddress, don't write any elements
		if ((operand.size() > 1) || (operand.iterator().next() != Address.LOCAL)) {
			for (Address value : function.getOperand()) {
				writer.writeObject(ELEMENT_INDEX, value);
			}
		}
	}
}
