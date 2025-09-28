/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;

/**
 * Adds privileged actions to the default global marshaller.
 * @author Paul Ferraro
 */
@SuppressWarnings("removal")
@Scope(Scopes.GLOBAL)
public class GlobalMarshaller extends org.infinispan.marshall.core.GlobalMarshaller {

	/**
	 * Creates a global marshaller.
	 */
	public GlobalMarshaller() {
	}

	@Override
	public Object objectFromByteBuffer(byte[] buf) throws IOException, ClassNotFoundException {
		if (System.getSecurityManager() == null) {
			return super.objectFromByteBuffer(buf);
		}
		PrivilegedExceptionAction<Object> action = () -> super.objectFromByteBuffer(buf);
		try {
			return AccessController.doPrivileged(action);
		} catch (PrivilegedActionException e) {
			Exception exception = e.getException();
			if (exception instanceof IOException ioe) {
				throw ioe;
			}
			if (exception instanceof ClassNotFoundException cnfe) {
				throw cnfe;
			}
			throw new IllegalStateException(exception);
		}
	}

	@Override
	public Object objectFromByteBuffer(byte[] bytes, int offset, int len) throws IOException, ClassNotFoundException {
		if (System.getSecurityManager() == null) {
			return super.objectFromByteBuffer(bytes, offset, len);
		}
		PrivilegedExceptionAction<Object> action = () -> super.objectFromByteBuffer(bytes, offset, len);
		try {
			return AccessController.doPrivileged(action);
		} catch (PrivilegedActionException e) {
			Exception exception = e.getException();
			if (exception instanceof IOException ioe) {
				throw ioe;
			}
			if (exception instanceof ClassNotFoundException cnfe) {
				throw cnfe;
			}
			throw new IllegalStateException(exception);
		}
	}

	@Override
	public Object objectFromInputStream(InputStream input) throws IOException, ClassNotFoundException {
		if (System.getSecurityManager() == null) {
			return super.objectFromInputStream(input);
		}
		PrivilegedExceptionAction<Object> action = () -> super.objectFromInputStream(input);
		try {
			return AccessController.doPrivileged(action);
		} catch (PrivilegedActionException e) {
			Exception exception = e.getException();
			if (exception instanceof IOException ioe) {
				throw ioe;
			}
			if (exception instanceof ClassNotFoundException cnfe) {
				throw cnfe;
			}
			throw new IllegalStateException(exception);
		}
	}

	@Override
	public Object objectFromObjectStream(ObjectInput input) {
		if (System.getSecurityManager() == null) {
			return super.objectFromObjectStream(input);
		}
		PrivilegedAction<Object> action = () -> super.objectFromObjectStream(input);
		return AccessController.doPrivileged(action);
	}
}
