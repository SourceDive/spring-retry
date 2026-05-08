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

import org.springframework.retry.RetryContext;

/**
 * Map-based implementation of {@link RetryContextCache}. The map backing the cache of
 * contexts is synchronized.
 *
 * @author Dave Syer
 */
public class MapRetryContextCache extends AbstractMapRetryContextCache<RetryContext> {

	/**
	 * Create an instance with {@link #DEFAULT_CAPACITY the default capacity}
	 */
	public MapRetryContextCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Create an instance with the given capacity, removing the eldest entries when the
	 * cache is full
	 * @param capacity the initial capacity of the cache
	 */
	public MapRetryContextCache(int capacity) {
		this(capacity, true);
	}

	/**
	 * Create an instance with the given capacity and the policy to apply when the cache
	 * is full.
	 * @param capacity the size of the cache
	 * @param removeEldestEntries whether to remove the eldest entries when the cache is
	 * full
	 * @since 1.3.5
	 */
	public MapRetryContextCache(int capacity, boolean removeEldestEntries) {
		super(capacity, removeEldestEntries);
	}

	/**
	 * Update the capacity of this cache. Prevent the cache from growing unboundedly if
	 * items that fail are misidentified and two references to an identical item actually
	 * do not have the same key. This can happen when users implement equals and hashCode
	 * based on mutable fields, for instance.
	 * @param capacity the capacity to set
	 */
	@Override
	public void setCapacity(int capacity) {
		super.setCapacity(capacity);
	}

	@Override
	protected RetryContext toValue(RetryContext context) {
		return context;
	}

	@Override
	protected RetryContext fromValue(RetryContext value) {
		return value;
	}

}
