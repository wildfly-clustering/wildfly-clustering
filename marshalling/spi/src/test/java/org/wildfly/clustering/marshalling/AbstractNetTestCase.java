/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Generic tests for java.net.* classes.
 * @author Paul Ferraro
 */
public abstract class AbstractNetTestCase {

	private final MarshallingTesterFactory factory;

	public AbstractNetTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testURI() {
		Consumer<URI> tester = this.factory.createTester();
		tester.accept(URI.create("http://wildfly.org/news/"));
	}

	@Test
	public void testURL() throws MalformedURLException {
		Consumer<URL> tester = this.factory.createTester();
		tester.accept(URI.create("http://wildfly.org/news/").toURL());
	}

	@Test
	public void testInetAddress() throws UnknownHostException {
		Consumer<InetAddress> tester = this.factory.createTester();
		tester.accept(InetAddress.getLoopbackAddress());
		tester.accept(InetAddress.getLocalHost());
		tester.accept(InetAddress.getByName("127.0.0.1"));
		tester.accept(InetAddress.getByName("::1"));
		tester.accept(InetAddress.getByName("0.0.0.0"));
		tester.accept(InetAddress.getByName("::"));
	}

	@Test
	public void testInetSocketAddress() throws UnknownHostException {
		Consumer<InetSocketAddress> tester = this.factory.createTester();
		tester.accept(InetSocketAddress.createUnresolved("foo.bar", 0));
		tester.accept(InetSocketAddress.createUnresolved("foo.bar", Short.MAX_VALUE));
		tester.accept(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
		tester.accept(new InetSocketAddress(InetAddress.getLoopbackAddress(), Short.MAX_VALUE));
		tester.accept(new InetSocketAddress(InetAddress.getLocalHost(), 0));
		tester.accept(new InetSocketAddress(InetAddress.getLocalHost(), Short.MAX_VALUE));
		tester.accept(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0));
		tester.accept(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), Short.MAX_VALUE));
		tester.accept(new InetSocketAddress(InetAddress.getByName("::"), 0));
		tester.accept(new InetSocketAddress(InetAddress.getByName("::"), Short.MAX_VALUE));
	}
}
