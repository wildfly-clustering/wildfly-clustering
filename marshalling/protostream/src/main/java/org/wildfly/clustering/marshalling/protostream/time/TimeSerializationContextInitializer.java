/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.time;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for the {@link java.time} package.
 * @author Paul Ferraro
 */
public class TimeSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a new serialization context initializer.
	 */
	public TimeSerializationContextInitializer() {
		super(Instant.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ProtoStreamMarshaller.of(DayOfWeek.class));
		ProtoStreamMarshaller<Duration> durationMarshaller = DurationMarshaller.INSTANCE.asMarshaller();
		context.registerMarshaller(durationMarshaller);
		// Marshall as duration since epoch
		context.registerMarshaller(durationMarshaller.wrap(Instant.class, instant -> Duration.ofSeconds(instant.getEpochSecond(), instant.getNano()), duration -> Instant.ofEpochSecond(duration.getSeconds(), duration.getNano())));
		context.registerMarshaller(LocalDateMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(LocalDateTimeMarshaller.INSTANCE);
		context.registerMarshaller(LocalTimeMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(ProtoStreamMarshaller.of(Month.class));
		context.registerMarshaller(MonthDayMarshaller.INSTANCE);
		context.registerMarshaller(OffsetDateTimeMarshaller.INSTANCE);
		context.registerMarshaller(OffsetTimeMarshaller.INSTANCE);
		context.registerMarshaller(PeriodMarshaller.INSTANCE);
		context.registerMarshaller(YearMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(YearMonthMarshaller.INSTANCE);
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(ZoneId.class, ZoneId::getId, Supplier.of(ZoneOffset.UTC), ZoneId::of));
		context.registerMarshaller(ZoneOffsetMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(ZonedDateTimeMarshaller.INSTANCE);
	}
}
