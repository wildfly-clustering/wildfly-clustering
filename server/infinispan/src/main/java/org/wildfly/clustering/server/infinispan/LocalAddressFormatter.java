/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(Formatter.class)
public class LocalAddressFormatter extends Formatter.Provided<Address> {

	public LocalAddressFormatter() {
		super(Formatter.of(LocalModeAddress.INSTANCE));
	}
}
