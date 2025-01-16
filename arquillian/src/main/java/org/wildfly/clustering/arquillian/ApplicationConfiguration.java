/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import org.jboss.shrinkwrap.api.Archive;

/**
 * Encapsulates the configuration of a test application.
 * @author Paul Ferraro
 * @param <C> the tester configuration type
 * @param <A> the archive type
 */
public interface ApplicationConfiguration<C, A extends Archive<A>> {

	A createArchive(C configuration);
}
