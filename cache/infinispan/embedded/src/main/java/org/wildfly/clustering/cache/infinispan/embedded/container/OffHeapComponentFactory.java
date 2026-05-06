/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.offheap.OffHeapEntryFactory;
import org.infinispan.container.offheap.OffHeapMemoryAllocator;
import org.infinispan.factories.EmptyConstructorNamedCacheFactory;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.metadata.Metadata;
import org.infinispan.metadata.impl.PrivateMetadata;

/**
 * Avoids loading classes requiring native access when not required by configuration.
 * @author Paul Ferraro
 */
@DefaultFactoryFor(classes = { OffHeapMemoryAllocator.class, OffHeapEntryFactory.class })
public class OffHeapComponentFactory extends EmptyConstructorNamedCacheFactory {
	/**
	 * Default constructor.
	 */
	public OffHeapComponentFactory() {
		// Do nothing
	}

	@Override
	public Object construct(String componentName) {
		if (this.configuration.memory().storage() != StorageType.OFF_HEAP) {
			// If not using off-heap memory storage, use implementation stubs.
			if (componentName.equals(OffHeapMemoryAllocator.class.getName())) {
				return new OffHeapMemoryAllocator() {
					@Override
					public long getAllocatedAmount() {
						return 0;
					}

					@Override
					public void deallocate(long memoryAddress, long size) {
						// Do nothing
					}

					@Override
					public long allocate(long memoryLength) {
						return 0;
					}
				};
			}
			if (componentName.equals(OffHeapEntryFactory.class.getName())) {
				return new OffHeapEntryFactory() {
					@Override
					public long create(WrappedBytes key, int hashCode, InternalCacheEntry<WrappedBytes, WrappedBytes> ice) {
						return 0;
					}

					@Override
					public long getSize(long address, boolean includeAllocationOverhead) {
						return 0;
					}

					@Override
					public long getNext(long address) {
						return 0;
					}

					@Override
					public void setNext(long address, long value) {
					}

					@Override
					public int getHashCode(long address) {
						return 0;
					}

					@Override
					public byte[] getKey(long address) {
						return null;
					}

					@Override
					public InternalCacheEntry<WrappedBytes, WrappedBytes> fromMemory(long address) {
						return null;
					}

					@Override
					public boolean equalsKey(long address, WrappedBytes wrappedBytes, int hashCode) {
						return false;
					}

					@Override
					public boolean isExpired(long address) {
						return false;
					}

					@Override
					public long calculateSize(WrappedBytes key, WrappedBytes value, Metadata metadata, PrivateMetadata internalMetadata) {
						return 0;
					}

					@Override
					public long updateMaxIdle(long address, long accessTime) {
						return 0;
					}
				};
			}
		}
		return super.construct(componentName);
	}
}
