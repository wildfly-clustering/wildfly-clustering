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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.wildfly.clustering.arquillian.Deployment;

/**
 * A smoke test client for validating container integration.
 * @author Paul Ferraro
 */
public class SessionManagementTester implements ClientTester, SessionManagementEndpointConfiguration {

	enum HttpMethod {
		HEAD, GET, PUT, DELETE;
	}

	private final ExecutorService executor;
	private final SessionManagementTesterConfiguration configuration;

	public SessionManagementTester(SessionManagementTesterConfiguration configuration) {
		this.executor = Executors.newFixedThreadPool(configuration.getConcurrency());
		this.configuration = configuration;
	}

	@Override
	public void close() {
		this.executor.shutdown();
	}

	@Override
	public void test(List<Deployment> deployments) {
		HttpClient client = this.configuration.getHttpClientConfigurator().apply(HttpClient.newBuilder()).cookieHandler(new CookieManager()).executor(this.executor).build();

		List<URI> endpoints = deployments.stream().map(this.configuration::locateEndpoint).collect(Collectors.toList());

		for (URI uri : endpoints) {
			// Verify no current session
			request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
				assertEquals(HTTP_OK, response.statusCode());
				assertFalse(response.headers().firstValue(SESSION_ID).isPresent());
				assertFalse(response.headers().firstValueAsLong(IMMUTABLE).isPresent());
				assertFalse(response.headers().firstValueAsLong(COUNTER).isPresent());
			}).join();
		}

		// Create a session
		Map.Entry<String, String> entry = request(client, endpoints.get(0), HttpMethod.PUT).<Map.Entry<String, String>>thenApply(response -> {
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
		int concurrency = this.configuration.getConcurrency();
		for (int i = 0; i < this.configuration.getIterations(); i++) {
			for (URI uri : endpoints) {
				String message = String.format("%s[%d]", uri, i);
				long value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertEquals(HTTP_OK, response.statusCode(), message);
					assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null), message);
					assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null), message);
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertEquals(expected.incrementAndGet(), value, message);

				// Perform a number of concurrent requests incrementing the mutable session attribute
				List<CompletableFuture<Long>> futures = new ArrayList<>(concurrency);
				for (int j = 0; j < concurrency; j++) {
					CompletableFuture<Long> future = request(client, uri, HttpMethod.GET).thenApply(response -> {
						assertEquals(HTTP_OK, response.statusCode(), message);
						assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null), message);
						assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null), message);
						return response.headers().firstValueAsLong(COUNTER).orElse(0);
					});
					futures.add(future);
				}
				expected.addAndGet(concurrency);
				// Verify the correct number of unique results
				assertEquals(concurrency, futures.stream().map(CompletableFuture::join).distinct().count(), message);

				// Grace time to increase likelihood that subsequent request does not overlap with post-request processing of previous requests
				try {
					Thread.sleep(this.configuration.getFailoverGracePeriod().toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				// Verify expected session attribute value following concurrent updates
				value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertEquals(HTTP_OK, response.statusCode(), message);
					assertEquals(sessionId, response.headers().firstValue(SESSION_ID).orElse(null), message);
					assertEquals(immutableValue, response.headers().firstValue(IMMUTABLE).orElse(null), message);
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertEquals(expected.incrementAndGet(), value, message);

				// Grace time between fail-over requests
				try {
					Thread.sleep(this.configuration.getFailoverGracePeriod().toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		// Invalidate session
		request(client, endpoints.get(0), HttpMethod.DELETE).thenAccept(response -> {
			assertEquals(HTTP_OK, response.statusCode());
			assertNull(response.headers().firstValue(SESSION_ID).orElse(null));
		}).join();

		List<CompletableFuture<Void>> futures = new ArrayList<>(2);
		for (URI uri : endpoints) {
			// Verify session was truly invalidated
			futures.add(request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
				assertEquals(HTTP_OK, response.statusCode());
				assertFalse(response.headers().firstValue(SESSION_ID).isPresent());
				assertFalse(response.headers().firstValueAsLong(COUNTER).isPresent());
			}));
		}
		futures.forEach(CompletableFuture::join);
	}

	private static CompletableFuture<HttpResponse<Void>> request(HttpClient client, URI uri, HttpMethod method) {
		return client.sendAsync(HttpRequest.newBuilder(uri).method(method.name(), BodyPublishers.noBody()).build(), BodyHandlers.discarding());
	}
}
