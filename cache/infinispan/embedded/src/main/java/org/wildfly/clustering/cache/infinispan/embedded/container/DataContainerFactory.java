/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.function.Supplier;

import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.commons.util.ByteQuantity;
import org.infinispan.configuration.cache.ClusteringConfiguration;
import org.infinispan.configuration.cache.MemoryConfiguration;
import org.infinispan.container.DataContainer;
import org.infinispan.container.impl.BoundedSegmentedDataContainer;
import org.infinispan.container.impl.DefaultDataContainer;
import org.infinispan.container.impl.DefaultSegmentedDataContainer;
import org.infinispan.container.impl.InternalDataContainer;
import org.infinispan.container.impl.L1SegmentedDataContainer;
import org.infinispan.container.impl.PeekableTouchableContainerMap;
import org.infinispan.container.impl.PeekableTouchableMap;
import org.infinispan.container.offheap.BoundedOffHeapDataContainer;
import org.infinispan.container.offheap.OffHeapConcurrentMap;
import org.infinispan.container.offheap.OffHeapDataContainer;
import org.infinispan.container.offheap.OffHeapEntryFactory;
import org.infinispan.container.offheap.OffHeapMemoryAllocator;
import org.infinispan.container.offheap.SegmentedBoundedOffHeapDataContainer;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.factories.AbstractNamedCacheComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.factories.annotations.SurvivesRestarts;

/**
 * Factory for creating the data container of a cache.
 * @author Paul Ferraro
 */
@DefaultFactoryFor(classes = InternalDataContainer.class)
@SurvivesRestarts
public class DataContainerFactory extends AbstractNamedCacheComponentFactory implements AutoInstantiableFactory {
	/**
	 * Creates a new data container factory.
	 */
	public DataContainerFactory() {
	}

	@Override
	public Object construct(String componentName) {
		DataContainer<?, ?> container = this.createDataContainer();
		MemoryConfiguration memory = this.configuration.memory();
		memory.attributes().attribute(MemoryConfiguration.MAX_SIZE).addListener((newSize, oldSize) -> container.resize(ByteQuantity.parse(newSize.get())));
		memory.attributes().attribute(MemoryConfiguration.MAX_COUNT).addListener((newSize, oldSize) -> container.resize(newSize.get()));
		return container;
	}

	private DataContainer<?, ?> createDataContainer() {
		MemoryConfiguration memory = this.configuration.memory();
		ClusteringConfiguration clustering = this.configuration.clustering();
		EvictionStrategy strategy = memory.whenFull();
		boolean segmented = clustering.cacheMode().needsStateTransfer();
		int segments = clustering.hash().numSegments();
		boolean offHeap = this.configuration.memory().isOffHeap();

		if (strategy.isExceptionBased() || !strategy.isEnabled()) {
			// Create unbounded container
			if (segmented) {
				Supplier<PeekableTouchableMap<WrappedBytes, WrappedBytes>> factory = offHeap ? this::createAndStartOffHeapConcurrentMap : PeekableTouchableContainerMap::new;
				return clustering.l1().enabled() ? new L1SegmentedDataContainer<>(factory, segments) : new DefaultSegmentedDataContainer<>(factory, segments);
			}
			return offHeap ? new OffHeapDataContainer() : DefaultDataContainer.unBoundedDataContainer(this.configuration.locking().concurrencyLevel());
		}

		boolean hasSize = memory.maxSize() != null;
		if (offHeap) {
			return segmented ? new SegmentedBoundedOffHeapDataContainer(segments, memory.maxCount(), hasSize) : new BoundedOffHeapDataContainer(memory.maxCount(), hasSize);
		}
		if (hasSize) {
			return segmented ? new BoundedSegmentedDataContainer<>(segments, memory.maxSizeBytes(), hasSize) : DefaultDataContainer.boundedDataContainer(this.configuration.locking().concurrencyLevel(), memory.maxSizeBytes(), hasSize);
		}
		return segmented ? new SegmentedEvictableDataContainer<>(this.basicComponentRegistry, this.configuration) : new EvictableDataContainer<>(this.basicComponentRegistry, this.configuration);
	}

	private OffHeapConcurrentMap createAndStartOffHeapConcurrentMap() {
		OffHeapEntryFactory entryFactory = this.basicComponentRegistry.getComponent(OffHeapEntryFactory.class).wired();
		OffHeapMemoryAllocator memoryAllocator = this.basicComponentRegistry.getComponent(OffHeapMemoryAllocator.class).wired();
		return new OffHeapConcurrentMap(memoryAllocator, entryFactory, null);
	}
}
