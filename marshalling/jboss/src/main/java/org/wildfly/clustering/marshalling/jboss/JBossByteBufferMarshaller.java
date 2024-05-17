/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.OutputStream;

import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.SimpleDataInput;
import org.jboss.marshalling.SimpleDataOutput;
import org.jboss.marshalling.Unmarshaller;
import org.wildfly.clustering.marshalling.AbstractByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.IndexSerializer;

/**
 * A {@link ByteBufferMarshaller} based on JBoss Marshalling.
 * @author Paul Ferraro
 */
public class JBossByteBufferMarshaller extends AbstractByteBufferMarshaller {

	private final MarshallerFactory factory = Marshalling.getMarshallerFactory("river", Marshalling.class.getClassLoader());
	private final MarshallingConfigurationRepository repository;
	private final MarshallingConfiguration configuration;

	/**
	 * Creates a versioned marshaller supporting multiple marshalling configurations.
	 * @param repository a repository of marshalling configurations
	 * @param loader a class loader
	 */
	public JBossByteBufferMarshaller(MarshallingConfigurationRepository repository, ClassLoader loader) {
		this(repository, repository.getCurrentMarshallingConfiguration(), loader);
	}

	/**
	 * Creates an unversioned marshaller using a single marshalling configuration.
	 * @param configuration a marshalling configuration
	 * @param loader a loader
	 */
	public JBossByteBufferMarshaller(MarshallingConfiguration configuration, ClassLoader loader) {
		this(null, configuration, loader);
	}

	private JBossByteBufferMarshaller(MarshallingConfigurationRepository repository, MarshallingConfiguration configuration, ClassLoader loader) {
		super(loader);
		this.repository = repository;
		this.configuration = configuration;
	}

	@Override
	public Object readFrom(InputStream input) throws IOException {
		try (SimpleDataInput data = new SimpleDataInput(Marshalling.createByteInput(input))) {
			MarshallingConfiguration configuration = this.configuration;
			if (this.repository != null) {
				int version = IndexSerializer.UNSIGNED_BYTE.readInt(data);
				configuration = this.repository.getMarshallingConfiguration(version);
			}
			try (Unmarshaller unmarshaller = this.factory.createUnmarshaller(configuration)) {
				unmarshaller.start(data);
				Object result = unmarshaller.readObject();
				unmarshaller.finish();
				return result;
			}
		} catch (ClassNotFoundException e) {
			InvalidClassException exception = new InvalidClassException(e.getMessage());
			exception.initCause(e);
			throw exception;
		} catch (RuntimeException e) {
			// Issues such as invalid lambda deserialization throw runtime exceptions
			InvalidObjectException exception = new InvalidObjectException(e.getMessage());
			exception.initCause(e);
			throw exception;
		}
	}

	@Override
	public void writeTo(OutputStream output, Object value) throws IOException {
		try (SimpleDataOutput data = new SimpleDataOutput(Marshalling.createByteOutput(output))) {
			if (this.repository != null) {
				IndexSerializer.UNSIGNED_BYTE.writeInt(data, this.repository.getCurrentVersion());
			}
			try (Marshaller marshaller = this.factory.createMarshaller(this.configuration)) {
				marshaller.start(data);
				marshaller.writeObject(value);
				marshaller.finish();
			}
		}
	}

	@Override
	public boolean isMarshallable(Object object) {
		if (object == null) return true;
		Class<?> objectClass = object.getClass();
		try {
			if (this.configuration.getObjectTable().getObjectWriter(object) != null) return true;
			if (this.configuration.getClassExternalizerFactory().getExternalizer(objectClass) != null) return true;
			return this.configuration.getSerializabilityChecker().isSerializable(objectClass);
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "JBossMarshalling";
	}
}
