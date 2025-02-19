/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.*;
import static org.wildfly.clustering.session.container.SessionManagementEndpointConfiguration.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
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

import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.Tester;

/**
 * A smoke test client for validating container integration.
 * @author Paul Ferraro
 */
public class SessionManagementTester implements Tester {

	public enum HttpMethod {
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
	public void accept(List<Deployment> deployments) {
		HttpClient client = this.configuration.getHttpClientConfigurator().apply(HttpClient.newBuilder()).cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).executor(this.executor).build();

		List<URI> endpoints = deployments.stream().map(this.configuration::locateEndpoint).toList();

		boolean nullableSession = this.configuration.isNullableSession();
		if (nullableSession) {
			for (URI uri : endpoints) {
				// Verify no current session
				request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
					assertThat(response.statusCode()).isEqualTo(HTTP_OK);
					assertThat(response.headers().firstValue(SESSION_ID)).isEmpty();
					assertThat(response.headers().firstValueAsLong(IMMUTABLE)).isEmpty();
					assertThat(response.headers().firstValueAsLong(COUNTER)).isEmpty();
				}).join();
			}
		}

		// Create a session
		Map.Entry<String, String> entry = request(client, endpoints.get(0), HttpMethod.PUT).<Map.Entry<String, String>>thenApply(response -> {
			assertThat(response.statusCode()).isEqualTo(HTTP_OK);
			String sessionId = response.headers().firstValue(SESSION_ID).orElse(null);
			String immutableValue = response.headers().firstValue(IMMUTABLE).orElse(null);
			return new AbstractMap.SimpleImmutableEntry<>(sessionId, immutableValue);
		}).join();
		String sessionId = entry.getKey();
		String immutableValue = entry.getValue();
		assertThat(sessionId).isNotNull();
		assertThat(immutableValue).isNotNull();

		AtomicLong expected = new AtomicLong(0);
		int concurrency = this.configuration.getConcurrency();
		for (int i = 0; i < this.configuration.getIterations(); i++) {
			for (URI uri : endpoints) {
				String message = String.format("%s[%d]", uri, i);
				long value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertThat(response.statusCode()).as(message).isEqualTo(HTTP_OK);
					assertThat(response.headers().firstValue(SESSION_ID)).as(message).hasValue(sessionId);
					assertThat(response.headers().firstValue(IMMUTABLE)).as(message).hasValue(immutableValue);
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertThat(value).as(message).isEqualTo(expected.incrementAndGet());

				// Perform a number of concurrent requests incrementing the mutable session attribute
				List<CompletableFuture<Long>> futures = new ArrayList<>(concurrency);
				for (int j = 0; j < concurrency; j++) {
					CompletableFuture<Long> future = request(client, uri, HttpMethod.GET).thenApply(response -> {
						assertThat(response.statusCode()).as(message).isEqualTo(HTTP_OK);
						assertThat(response.headers().firstValue(SESSION_ID)).as(message).hasValue(sessionId);
						assertThat(response.headers().firstValue(IMMUTABLE)).as(message).hasValue(immutableValue);
						return response.headers().firstValueAsLong(COUNTER).orElse(0);
					});
					futures.add(future);
				}
				expected.addAndGet(concurrency);
				// Verify the correct number of unique results
				assertThat(futures.stream().map(CompletableFuture::join).distinct().count()).as(message).isEqualTo(concurrency);

				// Grace time to increase likelihood that subsequent request does not overlap with post-request processing of previous requests
				this.failoverGracePeriod();

				// Verify expected session attribute value following concurrent updates
				value = request(client, uri, HttpMethod.GET).thenApply(response -> {
					assertThat(response.statusCode()).as(message).isEqualTo(HTTP_OK);
					assertThat(response.headers().firstValue(SESSION_ID)).as(message).hasValue(sessionId);
					assertThat(response.headers().firstValue(IMMUTABLE)).as(message).hasValue(immutableValue);
					return response.headers().firstValueAsLong(COUNTER).orElse(0);
				}).join();
				assertThat(value).as(message).isEqualTo(expected.incrementAndGet());

				this.failoverGracePeriod();
			}
		}

		// Invalidate session
		request(client, endpoints.get(0), HttpMethod.DELETE).thenAccept(response -> {
			assertThat(response.statusCode()).isEqualTo(HTTP_OK);
			assertThat(response.headers().firstValue(SESSION_ID)).isEmpty();
		}).join();

		List<CompletableFuture<Void>> futures = new ArrayList<>(endpoints.size());
		for (URI uri : endpoints) {
			// Verify session was truly invalidated
			futures.add(request(client, uri, HttpMethod.HEAD).thenAccept(response -> {
				assertThat(response.statusCode()).isEqualTo(HTTP_OK);
				if (nullableSession) {
					assertThat(response.headers().firstValue(SESSION_ID)).isEmpty();
				} else {
					assertThat(response.headers().firstValue(SESSION_ID)).isNotEqualTo(sessionId);
				}
				assertThat(response.headers().firstValueAsLong(COUNTER)).isEmpty();
			}));
		}
		futures.forEach(CompletableFuture::join);
	}

	private void failoverGracePeriod() {
		this.configuration.getFailoverGracePeriod().ifPresent(duration -> {
			// Grace time between fail-over requests
			try {
				Thread.sleep(duration.toMillis());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	private static CompletableFuture<HttpResponse<Void>> request(HttpClient client, URI uri, HttpMethod method) {
		return client.sendAsync(HttpRequest.newBuilder(uri).method(method.name(), BodyPublishers.noBody()).build(), BodyHandlers.discarding());
	}
}
