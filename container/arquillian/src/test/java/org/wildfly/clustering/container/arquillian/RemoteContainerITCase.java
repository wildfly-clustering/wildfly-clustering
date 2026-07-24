/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;

/**
 * Validates the lifecycle manipulation of a remote arquillian container.
 * @author Paul Ferraro
 */
public class RemoteContainerITCase {
	@RegisterExtension
	static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	@RunAsClient
	@Test
	public void test() {
		assertThat(this.registry.getContainerNames()).containsExactly("tomcat");
		DeploymentContainer container = this.registry.getContainer("tomcat");
		assertThat(container.isStarted()).isFalse();
		container.start();
		assertThat(container.isStarted()).isTrue();

		WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war").add(EmptyAsset.INSTANCE, "index.html");

		Deployment deployment = container.deploy(archive);
		assertThat(deployment.isStarted()).isTrue();

		deployment.stop();
		assertThat(deployment.isStarted()).isFalse();

		container.stop();
		assertThat(container.isStarted()).isFalse();
	}
}
