/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.NodeVersion;
import org.jboss.marshalling.Externalizer;
import org.jgroups.util.UUID;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;

/**
 * Serializer for an Infinispan JGroups-based address.
 * @author Paul Ferraro
 */
public enum AddressSerializer implements Serializer<Address> {
	/** Singleton instance */
	INSTANCE;

	@Override
	public void write(DataOutput output, Address address) throws IOException {
		boolean local = address == Address.LOCAL;
		output.writeBoolean(local);
		if (!local) {
			output.writeLong(address.getMostSignificantBits());
			output.writeLong(address.getLeastSignificantBits());
			NodeVersion version = address.getVersion();
			if (version != null) {
				output.writeByte(version.getMajor());
				output.writeByte(version.getMinor());
				output.writeByte(version.getPatch());
			} else {
				output.writeByte(0);
			}
			writeString(output, address.getSiteId());
			writeString(output, address.getRackId());
			writeString(output, address.getMachineId());
		}
	}

	private static void writeString(DataOutput output, String value) throws IOException {
		output.writeUTF((value != null) ? value : "");
	}

	@Override
	public Address read(DataInput input) throws IOException {
		if (input.readBoolean()) {
			return Address.LOCAL;
		}
		long mostSignificant = input.readLong();
		long leastSignificant = input.readLong();
		byte major = input.readByte();
		NodeVersion version = (major != 0) ? NodeVersion.from(major, input.readByte(), input.readByte()) : null;
		String site = readString(input);
		String rack = readString(input);
		String machine = readString(input);
		return Address.protoFactory(mostSignificant, leastSignificant, version, site, rack, machine);
	}

	private static String readString(DataInput input) throws IOException {
		String value = input.readUTF();
		return value.isEmpty() ? null : value;
	}

	/**
	 * Provides an externalizer of a JGroups address.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class JGroupsAddressExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		/**
		 * Creates a new externalizer provider.
		 */
		public JGroupsAddressExternalizerProvider() {
		}

		@Override
		public Class<?> getType() {
			return Address.class;
		}

		@Override
		public Externalizer getExternalizer() {
			return this.externalizer;
		}
	}

	/**
	 * A formatter of a JGroups address.
	 */
	@MetaInfServices(Formatter.class)
	public static class JGroupsAddressFormatter implements Formatter<Address> {
		private static final String DELIMITER = "\t";

		/**
		 * Creates a formatter.
		 */
		public JGroupsAddressFormatter() {
		}

		@Override
		public Class<? extends Address> getType() {
			return Address.class;
		}

		@Override
		public Address parse(String value) {
			if (value.isEmpty()) return Address.LOCAL;
			String[] parts = value.split(Pattern.quote(DELIMITER));
			UUID uuid = UUID.fromString(parts[0]);
			NodeVersion version = (parts.length > 1) ? NodeVersion.from(parts[1]) : null;
			String site = (parts.length > 2) ? parts[2] : null;
			String rack = (parts.length > 3) ? parts[3] : null;
			String machine = (parts.length > 4) ? parts[4] : null;
			return Address.protoFactory(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), version, site, rack, machine);
		}

		@Override
		public String format(Address address) {
			List<String> parts = new ArrayList<>(4);
			if (address != Address.LOCAL) {
				parts.add(new UUID(address.getMostSignificantBits(), address.getLeastSignificantBits()).toStringLong());
				NodeVersion version = address.getVersion();
				if (version != null) {
					parts.add(version.toString());
				}
				String site = address.getSiteId();
				if (site != null) {
					parts.add(site);
				}
				String rack = address.getRackId();
				if (rack != null) {
					parts.add(rack);
				}
				String machine = address.getMachineId();
				if (machine != null) {
					parts.add(machine);
				}
			}
			return parts.stream().collect(Collectors.joining(DELIMITER));
		}
	}
}
