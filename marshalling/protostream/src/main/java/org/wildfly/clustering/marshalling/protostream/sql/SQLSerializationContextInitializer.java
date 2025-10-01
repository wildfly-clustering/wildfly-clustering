/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.sql;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for the {@link java.sql} package.
 * @author Paul Ferraro
 */
public class SQLSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a new serialization context initializer.
	 */
	public SQLSerializationContextInitializer() {
		super(Timestamp.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(context.getMarshaller(LocalDate.class).wrap(Date.class, Date::toLocalDate, Date::valueOf));
		context.registerMarshaller(context.getMarshaller(LocalTime.class).wrap(Time.class, Time::toLocalTime, Time::valueOf));
		context.registerMarshaller(context.getMarshaller(LocalDateTime.class).wrap(Timestamp.class, Timestamp::toLocalDateTime, Timestamp::valueOf));
	}
}
