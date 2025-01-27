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

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class TimeSerializationContextInitializer extends AbstractSerializationContextInitializer {

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
		context.registerMarshaller(new LocalDateTimeMarshaller());
		context.registerMarshaller(LocalTimeMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(ProtoStreamMarshaller.of(Month.class));
		context.registerMarshaller(new MonthDayMarshaller());
		context.registerMarshaller(new OffsetDateTimeMarshaller());
		context.registerMarshaller(new OffsetTimeMarshaller());
		context.registerMarshaller(new PeriodMarshaller());
		context.registerMarshaller(YearMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(new YearMonthMarshaller());
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(ZoneId.class, ZoneId::getId, Functions.constantSupplier(ZoneOffset.UTC), ZoneId::of));
		context.registerMarshaller(ZoneOffsetMarshaller.INSTANCE.asMarshaller());
		context.registerMarshaller(new ZonedDateTimeMarshaller());
	}
}
