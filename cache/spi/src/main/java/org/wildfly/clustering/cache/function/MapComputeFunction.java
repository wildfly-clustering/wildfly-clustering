/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Map;

/**
 * Function that applies updates to a map.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class MapComputeFunction<K, V> extends MapFunction<K, V, Map<K, V>> {

	public MapComputeFunction(Map<K, V> operand) {
		super(operand, MapOperations.forOperandKey(operand.keySet().iterator().next()));
	}

	@Override
	public void accept(Map<K, V> map, Map<K, V> operand) {
		for (Map.Entry<K, V> entry : operand.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			if (value != null) {
				map.put(key, value);
			} else {
				map.remove(key);
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof MapComputeFunction)) return false;
		@SuppressWarnings("unchecked")
		MapComputeFunction<K, V> function = (MapComputeFunction<K, V>) object;
		Map<K, V> ourOperand = this.getOperand();
		Map<K, V> otherOperand = function.getOperand();
		return ourOperand.size() == otherOperand.size() && ourOperand.keySet().containsAll(otherOperand.keySet()) && ourOperand.equals(otherOperand);
	}
}
