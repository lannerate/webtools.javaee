/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 6, 2003
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.jst.j2ee.internal.jca.operations;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jst.j2ee.application.operations.J2EEModuleCreationDataModel;
import org.eclipse.jst.j2ee.common.XMLResource;
import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.CommonarchiveFactoryImpl;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
import org.eclipse.jst.j2ee.internal.project.IConnectorNatureConstants;
import org.eclipse.jst.j2ee.internal.servertarget.ServerTargetDataModel;
import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModel;
import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
import org.eclipse.wst.common.frameworks.internal.operations.WTPPropertyDescriptor;
import org.eclipse.wst.common.jdt.internal.integration.JavaProjectCreationDataModel;

/**
 * @author jsholl
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ConnectorProjectCreationDataModel extends J2EEModuleCreationDataModel {
	public WTPOperation getDefaultOperation() {
		return new ConnectorProjectCreationOperation(this);
	}

	protected void init() {
		j2eeNatureID = IConnectorNatureConstants.NATURE_ID;
		super.init();
		setProperty(EDIT_MODEL_ID, IConnectorNatureConstants.EDIT_MODEL_ID);
		getJavaProjectCreationDataModel().setProperty(ProjectCreationDataModel.PROJECT_NATURES, new String[]{j2eeNatureID});
		if (getDefaultJ2EEVersion() != null)
			setProperty(J2EE_MODULE_VERSION, getDefaultProperty(J2EE_MODULE_VERSION));
		else
			setProperty(J2EE_MODULE_VERSION, null);
		serverTargetDataModel.setIntProperty(ServerTargetDataModel.DEPLOYMENT_TYPE_ID, XMLResource.RAR_TYPE);
		getJavaProjectCreationDataModel().setProperty(JavaProjectCreationDataModel.OUTPUT_LOCATION, IConnectorNatureConstants.DEFAULT_SOURCE_PATH);
		getJavaProjectCreationDataModel().setProperty(JavaProjectCreationDataModel.SOURCE_FOLDERS, new String[]{IConnectorNatureConstants.DEFAULT_SOURCE_PATH});
	}

	protected Object getDefaultProperty(String propertyName) {
		if (propertyName.equals(J2EE_MODULE_VERSION)) {
			return getDefaultJ2EEVersion();
		}
		return super.getDefaultProperty(propertyName);
	}

	/**
	 * @return Returns the default J2EE spec level based on the Global J2EE Preference
	 */
	private Object getDefaultJ2EEVersion() {
		int highestJ2EEPref = J2EEPlugin.getDefault().getJ2EEPreferences().getHighestJ2EEVersionID();
		switch (highestJ2EEPref) {
			case (J2EEVersionConstants.J2EE_1_4_ID) :
				return new Integer(J2EEVersionConstants.JCA_1_5_ID);
			case (J2EEVersionConstants.J2EE_1_3_ID) :
				return new Integer(J2EEConstants.JCA_1_0_ID);
			case (J2EEVersionConstants.J2EE_1_2_ID) :
				return null;
			default :
				return new Integer(J2EEVersionConstants.JCA_1_5_ID);
		}
	}

	protected WTPPropertyDescriptor doGetPropertyDescriptor(String propertyName) {
		if (propertyName.equals(J2EE_MODULE_VERSION)) {
			Integer propertyValue = (Integer) getProperty(propertyName);
			String description = null;
			switch (propertyValue.intValue()) {
				case J2EEVersionConstants.JCA_1_0_ID :
					description = J2EEVersionConstants.VERSION_1_0_TEXT;
					break;
				case J2EEVersionConstants.JCA_1_5_ID :
				default :
					description = J2EEVersionConstants.VERSION_1_5_TEXT;
					break;
			}
			return new WTPPropertyDescriptor(propertyValue, description);
		}
		return super.doGetPropertyDescriptor(propertyName);
	}

	protected WTPPropertyDescriptor[] getValidJ2EEModuleVersionDescriptors() {
		int highestJ2EEPref = J2EEPlugin.getDefault().getJ2EEPreferences().getHighestJ2EEVersionID();
		WTPPropertyDescriptor[] descriptors = null;
		switch (highestJ2EEPref) {
			case J2EEVersionConstants.J2EE_1_3_ID :
				descriptors = new WTPPropertyDescriptor[1];
				descriptors[0] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.JCA_1_0_ID), J2EEVersionConstants.VERSION_1_0_TEXT);
				break;
			case J2EEVersionConstants.J2EE_1_4_ID :
			default :
				descriptors = new WTPPropertyDescriptor[2];
				descriptors[0] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.JCA_1_0_ID), J2EEVersionConstants.VERSION_1_0_TEXT);
				descriptors[1] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.JCA_1_5_ID), J2EEVersionConstants.VERSION_1_5_TEXT);
				break;
		}
		return descriptors;
	}

	protected int convertModuleVersionToJ2EEVersion(int moduleVersion) {
		switch (moduleVersion) {
			case J2EEVersionConstants.JCA_1_0_ID :
				return J2EEVersionConstants.J2EE_1_3_ID;
			case J2EEVersionConstants.JCA_1_5_ID :
				return J2EEVersionConstants.J2EE_1_4_ID;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jst.j2ee.internal.internal.application.operations.J2EEModuleCreationDataModel#convertJ2EEVersionToModuleVersion(java.lang.Integer)
	 */
	protected Integer convertJ2EEVersionToModuleVersion(Integer j2eeVersion) {
		switch (j2eeVersion.intValue()) {
			case J2EEVersionConstants.J2EE_1_3_ID :
				return new Integer(J2EEVersionConstants.JCA_1_0_ID);
			case J2EEVersionConstants.J2EE_1_4_ID :
				return new Integer(J2EEVersionConstants.JCA_1_5_ID);
		}
		return super.convertJ2EEVersionToModuleVersion(j2eeVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jst.j2ee.internal.internal.application.operations.J2EEModuleCreationDataModel#getModuleType()
	 */
	protected EClass getModuleType() {
		return CommonarchiveFactoryImpl.getPackage().getRARFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jst.j2ee.internal.internal.application.operations.J2EEModuleCreationDataModel#getModuleExtension()
	 */
	protected String getModuleExtension() {
		return ".rar"; //$NON-NLS-1$
	}
}