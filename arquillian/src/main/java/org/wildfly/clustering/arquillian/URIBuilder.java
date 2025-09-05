/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;

/**
 * Builder for a URI.
 * @author Paul Ferraro
 */
public interface URIBuilder extends RelativeURIBuilder {

	/**
	 * Specifies the scheme of URI created by this builder.
	 * If unspecified, "http" is used.
	 * @param scheme a URI schema
	 * @return a reference to this builder
	 */
	URIBuilder setScheme(String scheme);

	/**
	 * Specifies the user info of URI created by this builder.
	 * @param userInfo user info
	 * @return a reference to this builder
	 */
	URIBuilder setUserInfo(String userInfo);

	/**
	 * Specifies the host of the URI created by this builder.
	 * If unspecified, the loopback address of the host machine is used.
	 * @param host a URI host
	 * @return a reference to this builder
	 */
	URIBuilder setHost(String host);

	/**
	 * Specifies the port of the URI created by this builder.
	 * If unspecified, the URI will use the default port for the specified scheme.
	 * @param port a URI port
	 * @return a reference to this builder
	 */
	URIBuilder setPort(int port);

	@Override
	default URIBuilder addPath(String path) {
		return this.addPath(path, Map.of());
	}

	@Override
	URIBuilder addPath(String path, Map<String, String> parameters);

	@Override
	default URIBuilder addParameter(String parameterName, String parameterValue) {
		return this.addParameter(parameterName, parameterValue);
	}

	@Override
	default URIBuilder addParameters(Map<String, String> parameters) {
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			this.addParameter(parameter.getKey(), parameter.getValue());
		}
		return this;
	}

	@Override
	URIBuilder setFragment(String fragment);

	static URIBuilder of() {
		return of(new ToIntFunction<>() {
			@Override
			public int applyAsInt(String scheme) {
				return switch (scheme) {
					case "ftp" -> 21;
					case "sftp" -> 22;
					case "scp" -> 22;
					case "ssh" -> 22;
					case "dns" -> 53;
					case "http" -> 80;
					case "https" -> 443;
					default -> throw new IllegalArgumentException(scheme);
				};
			}
		});
	}

	static URIBuilder of(ToIntFunction<String> defaultPort) {
		AtomicReference<String> schemeRef = new AtomicReference<>("http");
		AtomicReference<String> userInfoRef = new AtomicReference<>();
		AtomicReference<String> hostRef = new AtomicReference<>(InetAddress.getLoopbackAddress().getHostName());
		AtomicReference<OptionalInt> portRef = new AtomicReference<>(OptionalInt.empty());
		List<String> paths = new LinkedList<>();
		Map<String, List<String>> parameters = new TreeMap<>();
		AtomicReference<String> fragmentRef = new AtomicReference<>();
		return new URIBuilder() {
			@Override
			public URIBuilder setScheme(String value) {
				schemeRef.setPlain(value);
				return this;
			}

			@Override
			public URIBuilder setUserInfo(String userInfo) {
				userInfoRef.setPlain(userInfo);
				return this;
			}

			@Override
			public URIBuilder setHost(String host) {
				hostRef.setPlain(host);
				return this;
			}

			@Override
			public URIBuilder setPort(int port) {
				portRef.setPlain(OptionalInt.of(port));
				return this;
			}

			@Override
			public URIBuilder addPath(String path, Map<String, String> parameters) {
				StringBuilder builder = new StringBuilder(path);
				if (!parameters.isEmpty()) {
					for (Map.Entry<String, String> parameter : parameters.entrySet()) {
						builder.append(";").append(parameter.getKey()).append("=").append(parameter.getValue());
					}
				}
				paths.add(builder.toString());
				return this;
			}

			@Override
			public URIBuilder addParameter(String name, String value) {
				parameters.computeIfAbsent(name, key -> new LinkedList<>()).add(value);
				return this;
			}

			@Override
			public URIBuilder setFragment(String fragment) {
				fragmentRef.setPlain(fragment);
				return this;
			}

			@Override
			public URI build() {
				String scheme = schemeRef.getPlain();
				String userInfo = userInfoRef.getPlain();
				String host = hostRef.getPlain();
				int port = portRef.getPlain().orElseGet(() -> defaultPort.applyAsInt(scheme));
				StringBuilder builder = new StringBuilder();
				for (String path : paths) {
					if (!path.startsWith("/")) {
						builder.append("/");
					}
					builder.append(path);
				}
				String path = !builder.isEmpty() ? builder.toString() : null;
				List<String> queryParameters = !parameters.isEmpty() ? new LinkedList<>() : List.of();
				if (!parameters.isEmpty()) {
					for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
						for (String value : entry.getValue()) {
							queryParameters.add(entry.getKey() + "=" + Optional.ofNullable(value).orElse(""));
						}
					}
				}
				String query = !parameters.isEmpty() ? String.join("&", queryParameters) : null;
				String fragment = fragmentRef.getPlain();
				try {
					return new URI(scheme, userInfo, host, port, path, query, fragment);
				} catch (URISyntaxException e) {
					throw new IllegalStateException(e);
				}
			}
		};
	}
}
