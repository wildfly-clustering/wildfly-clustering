/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.Transcoder;
import org.infinispan.commons.marshall.Marshaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;

/**
 * @author Paul Ferraro
 */
@ExtendWith(MockitoExtension.class)
public class MarshalledValueTranscoderTestCase {

	@Mock
	private MarshalledValueFactory<Object> factory;
	@Mock
	private Marshaller marshaller;
	@Mock
	private MarshalledValue<Object, Object> value;

	@Test
	public void test() throws IOException, InterruptedException, ClassNotFoundException {
		MediaType type = new MediaType("application", "test");
		Transcoder transcoder = new MarshalledValueTranscoder<>(type, this.factory, this.marshaller);

		assertThat(transcoder.supports(type)).isTrue();

		Iterable<MediaType> supportedMediaTypes = List.of(MediaType.APPLICATION_OBJECT, MediaType.APPLICATION_OCTET_STREAM);
		assertThat(transcoder.getSupportedMediaTypes()).hasSize(3).contains(type).containsAll(supportedMediaTypes);

		for (MediaType supportedMediaType : supportedMediaTypes) {
			assertThat(transcoder.supports(supportedMediaType)).isTrue();
			assertThat(transcoder.supportsConversion(type, supportedMediaType)).isTrue();
			assertThat(transcoder.supportsConversion(supportedMediaType, type)).isTrue();
			for (MediaType mediaType : supportedMediaTypes) {
				assertThat(transcoder.supportsConversion(mediaType, supportedMediaType)).isFalse();
				assertThat(transcoder.supportsConversion(supportedMediaType, mediaType)).isFalse();
			}
		}
		Iterable<MediaType> unsupportedMediaTypes = List.of(MediaType.APPLICATION_JBOSS_MARSHALLING, MediaType.APPLICATION_PROTOSTREAM, MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_SERIALIZED_OBJECT);
		for (MediaType unsupportedMediaType : unsupportedMediaTypes) {
			assertThat(transcoder.supports(unsupportedMediaType)).isFalse();
			for (MediaType mediaType : supportedMediaTypes) {
				assertThat(transcoder.supportsConversion(mediaType, unsupportedMediaType)).isFalse();
				assertThat(transcoder.supportsConversion(unsupportedMediaType, mediaType)).isFalse();
			}
			for (MediaType mediaType : unsupportedMediaTypes) {
				assertThat(transcoder.supportsConversion(mediaType, unsupportedMediaType)).isFalse();
				assertThat(transcoder.supportsConversion(unsupportedMediaType, mediaType)).isFalse();
			}
		}

		Object content = new Object();

		assertThat(transcoder.transcode(content, type, type)).isSameAs(content);

		verifyNoInteractions(this.factory, this.marshaller);

		doReturn(this.value).when(this.factory).createMarshalledValue(content);

		assertThat(transcoder.transcode(content, type, MediaType.APPLICATION_OBJECT)).isSameAs(this.value);

		verifyNoInteractions(this.marshaller);

		byte[] marshalled = new byte[0];

		doReturn(marshalled).when(this.marshaller).objectToByteBuffer(this.value);

		assertThat(transcoder.transcode(content, type, MediaType.APPLICATION_OCTET_STREAM)).isSameAs(marshalled);

		Object context = new Object();

		doReturn(context).when(this.factory).getMarshallingContext();
		doReturn(content).when(this.value).get(context);

		assertThat(transcoder.transcode(this.value, MediaType.APPLICATION_OBJECT, type));

		doReturn(this.value).when(this.marshaller).objectFromByteBuffer(marshalled);

		assertThat(transcoder.transcode(marshalled, MediaType.APPLICATION_OCTET_STREAM, type));
	}
}
