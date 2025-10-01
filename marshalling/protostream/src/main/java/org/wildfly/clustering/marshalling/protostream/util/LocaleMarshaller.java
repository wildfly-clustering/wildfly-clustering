/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.IllformedLocaleException;
import java.util.Locale;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for an {@link Locale}.
 * @author Paul Ferraro
 */
public enum LocaleMarshaller implements ProtoStreamMarshaller<Locale> {
	/** Singleton instance */
	INSTANCE;

	private static final int LANGUAGE_INDEX = 1;
	private static final int REGION_INDEX = 2;
	private static final int VARIANT_INDEX = 3;
	private static final int SCRIPT_INDEX = 4;
	private static final int EXTENSION_INDEX = 5;

	@Override
	public Class<? extends Locale> getJavaClass() {
		return Locale.class;
	}

	@Override
	public Locale readFrom(ProtoStreamReader reader) throws IOException {
		Locale.Builder builder = new Locale.Builder().setLocale(Locale.ROOT);
		String language = Locale.ROOT.getLanguage();
		String region = Locale.ROOT.getCountry();
		String variant = Locale.ROOT.getVariant();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case LANGUAGE_INDEX -> {
					language = reader.readString();
				}
				case REGION_INDEX -> {
					region = reader.readString();
				}
				case VARIANT_INDEX -> {
					variant = reader.readString();
				}
				case SCRIPT_INDEX -> {
					builder.setScript(reader.readString());
				}
				case EXTENSION_INDEX -> {
					String value = reader.readString();
					builder.setExtension(value.charAt(0), value.substring(1));
				}
				default -> reader.skipField(tag);
			}
		}
		try {
			return builder.setLanguage(language).setRegion(region).setVariant(variant).build();
		} catch (IllformedLocaleException e) {
			// Original Locale was not IETF BCP 47 compliant, use legacy constructor
			return new Locale(language, region, variant);
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Locale locale) throws IOException {
		String language = locale.getLanguage();
		if (!Locale.ROOT.getLanguage().equals(language)) {
			writer.writeString(LANGUAGE_INDEX, language);
		}
		String region = locale.getCountry();
		if (!Locale.ROOT.getCountry().equals(region)) {
			writer.writeString(REGION_INDEX, region);
		}
		String variant = locale.getVariant();
		if (!Locale.ROOT.getVariant().equals(variant)) {
			writer.writeString(VARIANT_INDEX, variant);
		}
		String script = locale.getScript();
		if (!Locale.ROOT.getScript().equals(script)) {
			writer.writeString(SCRIPT_INDEX, script);
		}
		for (Character key : locale.getExtensionKeys()) {
			String extension = locale.getExtension(key.charValue());
			writer.writeString(EXTENSION_INDEX, new StringBuilder(extension.length() + 1).append(key.charValue()).append(extension).toString());
		}
	}
}
