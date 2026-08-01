/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class AnySerializationContextInitializer extends AbstractSerializationContextInitializer {
	private static final System.Logger LOGGER = System.getLogger(AnySerializationContextInitializer.class.getCanonicalName());

	/**
	 * Creates a new serialization context initializer.
	 */
	public AnySerializationContextInitializer() {
		// Do nothing
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		Module module = this.getClass().getModule();
		Set<AnyField> fields = EnumSet.allOf(AnyField.class);
		Iterator<AnyField> iterator = fields.iterator();
		while (iterator.hasNext()) {
			AnyField field = iterator.next();
			FieldMarshaller<?> marshaller = field.getMarshaller();
			Class<?> openClass = marshaller.getOpenClass().orElse(null);
			if (openClass != null) {
				Module openModule = openClass.getModule();
				String openPackageName = openClass.getPackageName();
				if (!openModule.isOpen(openPackageName, module)) {
					iterator.remove();
					LOGGER.log(System.Logger.Level.DEBUG, "ProtoStream marshaller for {0} is disabled. To enable, add --add-opens={1}/{2}={3} to JVM parameters", marshaller.getJavaClass().getCanonicalName(), openModule.getName(), openPackageName, module.isNamed() ? module.getName() : "ALL-UNNAMED");
				}
			}
		}
		context.registerMarshaller(new AnyMarshaller(fields));
	}
}
