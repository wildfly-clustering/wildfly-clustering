/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Unit test for {@link ImmutableHttpSession}.
 * @author Paul Ferraro
 */
public class ImmutableHttpSessionTestCase extends AbstractHttpSessionTestCase<ImmutableSession, ImmutableSessionMetaData> {

	public ImmutableHttpSessionTestCase() {
		super(ImmutableSession.class, ImmutableSessionMetaData.class, ImmutableHttpSession::new);
	}

	@Test
	public void setMaxInactiveInterval() {
		assertThatIllegalStateException().isThrownBy(() -> this.subject.setMaxInactiveInterval(this.random.nextInt(Short.MAX_VALUE)));

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void setAttribute() {
		assertThatIllegalStateException().isThrownBy(() -> this.subject.setAttribute("foo", UUID.randomUUID()));

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void removeAttribute() {
		assertThatIllegalStateException().isThrownBy(() -> this.subject.removeAttribute("foo"));

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void invalidate() {
		assertThatIllegalStateException().isThrownBy(() -> this.subject.invalidate());

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}
}
