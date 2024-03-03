/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A smoke test client for validating container integration.
 * @author Paul Ferraro
 */
public class SessionManagementTester implements ClientTester, SessionManagementEndpointConfiguration {

	enum HttpMethod {
		HEAD, GET, PUT, DELETE;
	}

	private static final int ITERATIONS = 4;
	private static final int CONCURRENCY = ITERATIONS * 10;
	private static final Duration FAILOVER_DURATION = Duration.ofSeconds(1);

	private final ExecutorService executor;
	private final SessionManagementTesterConfiguration configuration;

	public SessionManagementTester(SessionManagementTesterConfiguration configuration) {
		this.executor = Executors.newFixedThreadPool(CONCURRENCY);
		this.configuration = configuration;
	}

	@Override
	public void close() {
		this.executor.shutdown();
	}

	@Override
	public void test(URI baseURI1, URI baseURI2) {
		HttpClient client = this.configuration.getHttpClientConfigurator().apply(HttpClient.newBuilder()).cookieHandler(new CookieManager()).executor(this.executor).build();

		URI uri1 = baseURI1.resolve(ENDPOINT_NAME);
		URI uri2 = baseURI2.resolve(ENDPOINT_NAME);

		for (URI uri : Arrays.asList(uri1, uri2)) {
			// Verify a request that never starts its session
			request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
				assertEquals(HTTP_OK, response.statusCode());
				assertFalse(response.headers().firstValue(SESSION_ID).isPresent());
				assertFalse(response.headers().firstValueAsLong(IMMUTABLE).isPresent());
				assertFalse(response.headers().firstValueAsLong(COUNTER).isPresent());
			}).join();
		}

		// Force creation of a session
		Map.Entry<String, String> entry = request(client, uri1, HttpMethod.PUT).<Map.Entry<String, String>>thenApply(response -> {
			assertEquals(HTTP_OK, response.statusCode());
			String sessionId = response.headers().firstValue(SESSION_ID).orElse(null);
			String immutableValue = response.headers().firstValue(IMMUTABLE).orElse(null);
			return new AbstractMap.SimpleImmutableEntry<>(sessionId, immutableValue);
		}).join();
		String sessionId = entry.getKey();
		String immutableValue = entry.getValue();
		assertNotNull(sessionId);
		assertNotNull(immutableValue);

		AtomicLong expected = new AtomicLong(0);
		for (int i = 0; i < ITERATIONS; i++) {
			for (URI uri : Arrays.asList(uri1, uri2)) {
				int count = i;
				long value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertEquals(HTTP_OK, response.statusCode(), Integer.toString(count));
					assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null));
					assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null));
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertEquals(expected.incrementAndGet(), value);

				// Validate session is still "started"
				request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
					assertEquals(HTTP_OK, response.statusCode());
					assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null));
				}).join();

				// Perform a number of concurrent requests incrementing the session attribute
				List<CompletableFuture<Long>> futures = new ArrayList<>(CONCURRENCY);
				for (int j = 0; j < CONCURRENCY; j++) {
					CompletableFuture<Long> future = request(client, uri, HttpMethod.GET).thenApply(response -> {
						assertEquals(HTTP_OK, response.statusCode());
						assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null));
						assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null));
						return response.headers().firstValueAsLong(COUNTER).orElse(0);
					});
					futures.add(future);
				}
				expected.addAndGet(CONCURRENCY);
				// Verify the correct number of unique results
				assertEquals(CONCURRENCY, futures.stream().map(CompletableFuture::join).distinct().count());

				// Verify expected session attribute value following concurrent updates
				value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertEquals(HTTP_OK, response.statusCode());
					assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null));
					assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null));
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertEquals(expected.incrementAndGet(), value);

				if (!this.configuration.isTransactional()) {
					// Grace time between fail-over requests
					try {
						Thread.sleep(FAILOVER_DURATION.getSeconds(), FAILOVER_DURATION.getNano());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		// Invalidate session
		request(client, uri1, HttpMethod.DELETE).thenAccept(response -> {
			assertEquals(HTTP_OK, response.statusCode());
			assertNull(response.headers().firstValue(SESSION_ID).orElse(null));
		}).join();

		List<CompletableFuture<Void>> futures = new ArrayList<>(2);
		for (URI uri : Arrays.asList(uri1, uri2)) {
			// Verify session was truly invalidated
			futures.add(request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
				assertEquals(HTTP_OK, response.statusCode());
				assertFalse(response.headers().firstValue(SESSION_ID).isPresent());
				assertFalse(response.headers().firstValueAsLong(COUNTER).isPresent());
			}));
		}
		futures.stream().forEach(CompletableFuture::join);
	}

	private static CompletableFuture<HttpResponse<Void>> request(HttpClient client, URI uri, HttpMethod method) {
		return client.sendAsync(HttpRequest.newBuilder(uri).method(method.name(), BodyPublishers.noBody()).build(), BodyHandlers.discarding());
	}
}
