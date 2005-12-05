/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jst.j2ee.model.internal.validation;

import java.util.List;

import org.eclipse.jem.java.JavaClass;
import org.eclipse.jem.java.JavaHelpers;
import org.eclipse.jem.java.Method;
import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
import org.eclipse.jst.j2ee.ejb.Entity;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;


/**
 * This class checks entity key classes for errors or potential errors.
 * If any problems are found, an error, warning, or info marker is added to the task list.
 *
 * The following paragraph is taken from
 * Enterprise JavaBeans Specification ("Specification")
 * Version: 1.1
 * Status: Final Release
 * Release: 12/17/99
 * Copyright 1999 Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, U.S.A.
 * All rights reserved.
 *
 * 9.2.9 Entity bean's primary key class
 *  - The Bean Provider must specify a primary key class in the deployment descriptor.
 *  - The primary key type must be a legal Value Type in RMI-IIOP.
 *  - The class must provide suitable implementation of the hashCode() and 
 *    equals(Object other) methods to simplify the management of the primary keys 
 *    by client code.
*/
public abstract class AValidateKeyClass extends AValidateEJB {
	public Object getTarget(Object parent, Object clazz) {
		if (parent == null) {
			return null;
		}

		return ((Entity) parent).getPrimaryKey();
	}

	public final List[] getMethodsExtended(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) {
		// Never check that a key class' method is defined on another class 
		// of the bean.
		return null;
	}
	
	public final List[] getFieldsExtended(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) {
		// Never check that a key class' field is defined on another class
		// of the bean.
		return null;
	}
	
	/**
	 * Return true if the method can, and should, be validated.
	 * Filter out faulty methods (i.e., null), and methods which
	 * belong to the base type, whatever that is. (e.g. java.lang.Object)
	 */
	protected boolean isValid(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz, Method method, List[] methodsExtendedList) throws InvalidInputException {
		if (super.isValid(vc, bean, clazz, method, methodsExtendedList)) {
			// exclude root object methods
			if (!ValidationRuleUtility.isJavaLangObjectMethod(bean, method)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 9.2.9 Entity bean's primary key class
	 *  - The Bean Provider must specify a primary key class in the deployment descriptor. (checked by the MOF model)
	 *  - The primary key type must be a legal Value Type in RMI-IIOP. (for key fields, checked in the ValidateEntityBean class)
	 *  - The class must provide suitable implementation of the hashCode() and 
	 *    equals(Object other) methods to simplify the management of the primary keys 
	 *    by client code.
	 */
	public void validateClass(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) throws InvalidInputException {
		vc.terminateIfCancelled();

		validateLegalRMIType(vc, bean, clazz);

		if (ValidationRuleUtility.isUnnamedPackage(clazz.getJavaPackage())) {
			IMessage message = MessageUtility.getUtility().getMessage(vc, IEJBValidatorMessageConstants.CHKJ2041, IEJBValidationContext.INFO, bean, clazz, this);
			vc.addMessage(message);
		}
	}
	
	/**
	 * Verifies that a given class is a legal Value Type in RMI-IIOP.
	 *
	 * Java Remote MethodInvocation
	 * Specification
	 *
	 * Revision 1.7, Java 2 SDK, Standard Edition, v1.3.0, December 1999
	 *
	 * 2.6 Parameter Passing in Remote Method Invocation
	 *   An argument to, or a return value from, a remote object can be any object that
	 *   is serializable. This includes primitive types, remote objects, and non-remote
	 *   objects that implement the java.io.Serializable interface. For more
	 *   details on how to make classes serializable, see the Java Object Serialization
	 *   Specification. 
	 */
	public final void validateLegalRMIType(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) throws InvalidInputException {
		vc.terminateIfCancelled();

		ValidationRuleUtility.isValidType(clazz);

		if (!ValidationRuleUtility.isLegalRMI_IIOPType(bean, clazz)) {
			IMessage message = MessageUtility.getUtility().getMessage(vc, IEJBValidatorMessageConstants.CHKJ2019, IEJBValidationContext.INFO, bean, clazz, new String[] { clazz.getQualifiedName()}, this);
			vc.addMessage(message);
		}
	}
	
	/**
	 * 9.2.9 Entity bean's primary key class
	 *  - The class must provide suitable implementation of the hashCode() and 
	 *    equals(Object other) methods to simplify the management of the primary keys 
	 *    by client code.
	 */
	public void validateMethodExists(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) throws InvalidInputException {
		// The class must provide suitable implementation of the hashCode() and 
		// equals(Object other) methods to simplify the management of the primary keys 
		// by client code.
		Method hashCodeMethod = ValidationRuleUtility.getMethodExtended(clazz, IMethodAndFieldConstants.METHODNAME_HASHCODE, new JavaHelpers[0]);
		if ((hashCodeMethod == null) || ValidationRuleUtility.isJavaLangObjectMethod(bean, hashCodeMethod)) {
			// EJB 1.0 did not require this method, so this is a warning instead of an error.
			String[] msgParm = { clazz.getQualifiedName(), IMethodAndFieldConstants.METHODSIGNATURE_HASHCODE};
			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb11Constants.CHKJ2001, IEJBValidationContext.WARNING, bean, clazz, msgParm, this);
			vc.addMessage(message);
		}

		Method equalsMethod = ValidationRuleUtility.getMethodExtended(clazz, IMethodAndFieldConstants.METHODNAME_EQUALS, new JavaHelpers[]{ValidationRuleUtility.getType(ITypeConstants.CLASSNAME_JAVA_LANG_OBJECT, bean)});
		if ((equalsMethod == null) || (ValidationRuleUtility.isJavaLangObjectMethod(bean, equalsMethod))) {
			// EJB 1.0 did not require this method, so this is a warning instead of an error.
			String[] msgParm = { clazz.getQualifiedName(), IMethodAndFieldConstants.METHODSIGNATURE_EQUALS };
			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb11Constants.CHKJ2001, IEJBValidationContext.WARNING, bean, clazz, msgParm, this);
			vc.addMessage(message);
		}
	}

}
