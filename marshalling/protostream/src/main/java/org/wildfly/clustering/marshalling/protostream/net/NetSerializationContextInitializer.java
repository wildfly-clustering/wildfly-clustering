/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.net;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * @author Paul Ferraro
 */
public class NetSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public NetSerializationContextInitializer() {
		super("java.net.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(InetAddressMarshaller.INSTANCE.asMarshaller(InetAddress.class));
		context.registerMarshaller(new InetSocketAddressMarshaller());
		ProtoStreamMarshaller<URI> uriMarshaller = Scalar.STRING.cast(String.class).toMarshaller(java.net.URI.class, java.net.URI::toString, java.net.URI::create);
		context.registerMarshaller(uriMarshaller);
		context.registerMarshaller(uriMarshaller.map(URL.class, new Function<>() {
			@Override
			public URI apply(URL url) {
				try {
					return url.toURI();
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}, new Function<>() {
			@Override
			public URL apply(URI uri) {
				try {
					return uri.toURL();
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}));
	}
}
