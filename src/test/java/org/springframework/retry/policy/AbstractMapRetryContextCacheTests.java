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

import org.junit.jupiter.api.Test;

import org.springframework.retry.RetryContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AbstractMapRetryContextCache}.
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractMapRetryContextCacheTests {

	protected abstract AbstractMapRetryContextCache<?> createCache(int capacity, boolean removeEldestEntries);

	@Test
	public void testPutAndGet() {
		AbstractMapRetryContextCache<?> cache = createCache(5, false);
		RetryContext context = createRetryContext();
		cache.put("foo", context);
		assertThat(cache.get("foo")).isEqualTo(context);
	}

	@Test
	public void testLruEviction() {
		AbstractMapRetryContextCache<?> cache = createCache(2, true);

		RetryContext context1 = createRetryContext();
		RetryContext context2 = createRetryContext();
		RetryContext context3 = createRetryContext();

		cache.put("1", context1);
		cache.put("2", context2);

		// Access "1" to make it most recently used
		cache.get("1");

		cache.put("3", context3);

		// "2" should be evicted because it's the least recently used
		assertThat(cache.containsKey("1")).isTrue();
		assertThat(cache.containsKey("2")).isFalse();
		assertThat(cache.containsKey("3")).isTrue();
	}

	@Test
	public void testRemove() {
		AbstractMapRetryContextCache<?> cache = createCache(5, true);
		RetryContext context = createRetryContext();
		cache.put("foo", context);
		assertThat(cache.containsKey("foo")).isTrue();

		cache.remove("foo");
		assertThat(cache.containsKey("foo")).isFalse();
		assertThat(cache.get("foo")).isNull();
	}

	@Test
	public void testContainsKey() {
		AbstractMapRetryContextCache<?> cache = createCache(5, true);
		RetryContext context = createRetryContext();
		cache.put("foo", context);
		assertThat(cache.containsKey("foo")).isTrue();
		assertThat(cache.containsKey("bar")).isFalse();
	}

	@Test
	public void testCapacityLimitBreachedThrowsException() {
		AbstractMapRetryContextCache<?> cache = createCache(1, false);
		RetryContext context1 = createRetryContext();
		RetryContext context2 = createRetryContext();

		cache.put("foo", context1);
		assertThatExceptionOfType(RetryCacheCapacityExceededException.class)
			.isThrownBy(() -> cache.put("bar", context2));
	}

	@Test
	public void testCapacityLimitBreachedAllowsUpdateOfExistingEntry() {
		AbstractMapRetryContextCache<?> cache = createCache(1, false);
		RetryContext context1 = createRetryContext();
		RetryContext context2 = createRetryContext();

		cache.put("foo", context1);
		cache.put("foo", context2);
	}

	@Test
	public void testCapacityCanBeUpdated() {
		AbstractMapRetryContextCache<?> cache = createCache(1, false);
		cache.put("1", createRetryContext());
		RetryContext context2 = createRetryContext();
		assertThatExceptionOfType(RetryCacheCapacityExceededException.class).isThrownBy(() -> cache.put("2", context2));
		cache.setCapacity(2);
		cache.put("2", context2);
	}

	protected RetryContext createRetryContext() {
		return mock(RetryContext.class);
	}

}
