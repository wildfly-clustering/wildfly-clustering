/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.time;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.protostream.FieldSetMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for {@link Duration} instances, using the following strategy:
 * <ol>
 * <li>Marshal {@link Duration#ZERO} as zero bytes</li>
 * <li>Marshal number of seconds of duration as unsigned long</li>
 * <li>Marshal sub-second value of duration as unsigned integer, using millisecond precision, if possible</li>
 * </ol>
 * @author Paul Ferraro
 */
public enum DurationMarshaller implements FieldSetMarshaller.Simple<Duration> {
	/** Singleton instance */
	INSTANCE;

	private static final int NANOS_PER_MILLI = ChronoUnit.MILLIS.getDuration().getNano();
	private static final Set<ChronoUnit> SUPER_SECOND_UNITS = EnumSet.of(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
	private static final Set<ChronoUnit> SUB_MILLSECOND_UNITS = EnumSet.of(ChronoUnit.NANOS, ChronoUnit.MICROS);

	private static final int POSITIVE_SECONDS_INDEX = 0;
	private static final int NEGATIVE_SECONDS_INDEX = 1;
	private static final int MILLIS_INDEX = 2;
	private static final int NANOS_INDEX = 3;
	private static final int FIELDS = 4;

	@Override
	public Duration createInitialValue() {
		return Duration.ZERO;
	}

	@Override
	public int getFields() {
		return FIELDS;
	}

	@Override
	public Duration readFrom(ProtoStreamReader reader, int index, WireType type, Duration duration) throws IOException {
		return switch (index) {
			case POSITIVE_SECONDS_INDEX -> withSeconds(duration, reader.readUInt64());
			case NEGATIVE_SECONDS_INDEX -> duration.withSeconds(0 - reader.readUInt64());
			case MILLIS_INDEX -> withMillis(duration, reader.readUInt32());
			case NANOS_INDEX -> withNanos(duration, reader.readUInt32());
			default -> Supplier.call(() -> reader.skipField(type), null).thenApply(Function.of(duration)).get();
		};
	}

	private static Duration withSeconds(Duration duration, long seconds) {
		if (duration.isZero()) {
			// Use standard Duration, if possible
			for (ChronoUnit unit : SUPER_SECOND_UNITS) {
				Duration unitDuration = unit.getDuration();
				if (unitDuration.getSeconds() == seconds) {
					return unitDuration;
				}
			}
		}
		return duration.withSeconds(seconds);
	}

	private static Duration withMillis(Duration duration, int millis) {
		// Use standard Duration, if possible
		return (duration.isZero() && (millis == 1)) ? ChronoUnit.MILLIS.getDuration() : duration.withNanos(millis * NANOS_PER_MILLI);
	}

	private static Duration withNanos(Duration duration, int nanos) {
		if (duration.isZero()) {
			// Use standard Duration, if possible
			for (ChronoUnit unit : SUB_MILLSECOND_UNITS) {
				Duration unitDuration = unit.getDuration();
				if (unitDuration.getNano() == nanos) {
					return unitDuration;
				}
			}
		}
		return duration.withNanos(nanos);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Duration duration) throws IOException {
		long seconds = duration.getSeconds();
		// Optimize for positive values
		if (seconds > 0) {
			writer.writeUInt64(POSITIVE_SECONDS_INDEX, seconds);
		} else if (seconds < 0) {
			writer.writeUInt64(NEGATIVE_SECONDS_INDEX, 0 - seconds);
		}
		int nanos = duration.getNano();
		if (nanos > 0) {
			// Optimize for ms precision, if possible
			if (nanos % NANOS_PER_MILLI == 0) {
				writer.writeUInt32(MILLIS_INDEX, nanos / NANOS_PER_MILLI);
			} else {
				writer.writeUInt32(NANOS_INDEX, nanos);
			}
		}
	}
}
