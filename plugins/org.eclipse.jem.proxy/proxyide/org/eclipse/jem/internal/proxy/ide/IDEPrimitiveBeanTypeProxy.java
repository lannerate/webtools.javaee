/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jem.internal.proxy.ide;
/*


 */


public abstract class IDEPrimitiveBeanTypeProxy extends IDEBeanTypeProxy {
	
	static final int BOOLEAN = 1;
	static final int BYTE = 2;
	static final int CHAR = 3;
	static final int DOUBLE = 4;
	static final int FLOAT = 5;
	static final int INTEGER = 6;
	static final int LONG = 7;
	static final int SHORT = 8;
	
public IDEPrimitiveBeanTypeProxy(IDEProxyFactoryRegistry aRegistry,Class aClass){
	super(aRegistry,aClass);
}
public boolean isPrimitive() {
	return true;
}
abstract int getPrimitiveType();
}
