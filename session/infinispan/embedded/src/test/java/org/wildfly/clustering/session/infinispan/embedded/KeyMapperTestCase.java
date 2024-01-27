/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.KeyMapperTester;
import org.wildfly.clustering.session.infinispan.embedded.attributes.SessionAttributesKey;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;
import org.wildfly.clustering.session.infinispan.embedded.user.UserContextKey;
import org.wildfly.clustering.session.infinispan.embedded.user.UserSessionsKey;

/**
 * @author Paul Ferraro
 */
public class KeyMapperTestCase {
	@Test
	public void test() {
		KeyMapperTester<Key<String>> tester = new KeyMapperTester<>(new KeyMapper());

		String id = "ABC123";

		tester.test(new SessionMetaDataKey(id));
		tester.test(new SessionAttributesKey(id));

		tester.test(new UserContextKey(id));
		tester.test(new UserSessionsKey(id));
	}
}
