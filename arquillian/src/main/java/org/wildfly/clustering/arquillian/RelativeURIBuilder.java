/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Builds a URI relative to some other URI.
 * @author Paul Ferraro
 */
public interface RelativeURIBuilder {

	/**
	 * Appends the specified path to the URI created by this builder.
	 * @param path a URI path
	 * @return a reference to this builder
	 */
	default RelativeURIBuilder addPath(String path) {
		return this.addPath(path, Map.of());
	}

	/**
	 * Appends the specified path and path parameters to the URI created by this builder.
	 * @param path a URI path
	 * @param parameters the path parameters for the specified path
	 * @return a reference to this builder
	 */
	RelativeURIBuilder addPath(String path, Map<String, String> parameters);

	/**
	 * Appends the specified query parameter to the URI created by this builder.
	 * @param parameterName a query parameter name
	 * @param parameterValue a query parameter name
	 * @return a reference to this builder
	 */
	RelativeURIBuilder addParameter(String parameterName, String parameterValue);

	/**
	 * Appends the specified query parameters to the URI created by this builder.
	 * @param parameters a map of query parameters
	 * @return a reference to this builder
	 */
	RelativeURIBuilder addParameters(Map<String, String> parameters);

	/**
	 * Specifies the fragment (e.g. anchor) of the URI created by this builder.
	 * @param fragment a URI fragment
	 * @return a reference to this builder
	 */
	RelativeURIBuilder setFragment(String fragment);

	URI build();

	static RelativeURIBuilder relative(URI base) throws URISyntaxException {
		return URIBuilder.of().setScheme(base.getScheme()).setUserInfo(base.getUserInfo()).setHost(base.getHost()).setPort(base.getPort()).addPath(base.getPath());
	}
}
