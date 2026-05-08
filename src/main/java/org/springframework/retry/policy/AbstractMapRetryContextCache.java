/*
 * Copyright 2006-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.retry.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.retry.RetryContext;

/**
 * Base {@link RetryContextCache} implementation that stores entries in memory, with a
 * configurable maximum capacity and LRU eviction policy. For a cache used in a global
 * state, any attempt to increase its capacity should result in an exception. However, a
 * regular cache should not have permanent entries, and removing the eldest entries is
 * sensible.
 *
 * @author Stephane Nicoll
 * @since 1.3.5
 */
public abstract class AbstractMapRetryContextCache<V> implements RetryContextCache {

	/**
	 * Default value for maximum capacity of the cache. This is set to a reasonably low
	 * value ({@value}) to avoid users inadvertently filling the cache with item keys that
	 * are inconsistent.
	 */
	public static final int DEFAULT_CAPACITY = 4096;

	private static final Log logger = LogFactory.getLog(AbstractMapRetryContextCache.class);

	private final Map<Object, V> map;

	private final boolean failIfFull;

	private int capacity;

	/**
	 * Create an instance with the given capacity and the policy to apply when the cache
	 * is full.
	 * @param capacity the size of the cache
	 * @param removeEldestEntries whether to remove the eldest entries when the cache is
	 * full
	 */
	protected AbstractMapRetryContextCache(int capacity, boolean removeEldestEntries) {
		this.capacity = capacity;
		this.map = Collections
			.synchronizedMap(removeEldestEntries ? new LinkedHashMap<Object, V>(capacity, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Object, V> eldest) {
					boolean evict = size() > AbstractMapRetryContextCache.this.capacity;
					if (evict && logger.isWarnEnabled()) {
						logger.warn("Retry cache capacity limit breached. "
								+ "Do you need to re-consider the implementation of the key generator, "
								+ "or the equals and hashCode of the items that failed?");
					}
					return evict;
				}
			} : new HashMap<>());
		this.failIfFull = !removeEldestEntries;
	}

	protected final Map<Object, V> getMap() {
		return this.map;
	}

	/**
	 * Update the capacity of the cache.
	 * @param capacity the new capacity
	 */
	protected void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public RetryContext get(Object key) {
		V value = this.map.get(key);
		return (value != null) ? fromValue(value) : null;
	}

	@Override
	public void put(Object key, RetryContext context) {
		if (this.failIfFull && !this.map.containsKey(key) && this.map.size() >= this.capacity) {
			throw new RetryCacheCapacityExceededException("Cache capacity limit breached. "
					+ "Do you need to re-consider the implementation of the key generator, "
					+ "or the equals and hashCode of the items that failed?");
		}
		this.map.put(key, toValue(context));
	}

	@Override
	public void remove(Object key) {
		this.map.remove(key);
	}

	/**
	 * Compute the value to store in the cache.
	 * @param context the {@link RetryContext} to store
	 * @return the cache value
	 */
	protected abstract V toValue(RetryContext context);

	/**
	 * Get the {@link RetryContext} from the cache value.
	 * @param value the cache value
	 * @return the retry context
	 */
	protected abstract RetryContext fromValue(V value);

}
