/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.jst.j2ee.internal.web.deployables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jem.util.logger.proxy.Logger;
import org.eclipse.jst.j2ee.internal.deployables.J2EEDeployableFactory;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;

public class WebDeployableFactory extends J2EEDeployableFactory {
	public static final String ID = "org.eclipse.jst.j2ee.server.web"; //$NON-NLS-1$

	protected static final IPath[] PATHS = new IPath[]{new Path(".j2ee") //$NON-NLS-1$
	};

	public String getFactoryId() {
		return ID;
	}

	public String getNatureID() {
		return IModuleConstants.MODULE_NATURE_ID;
	}

	protected IPath[] getListenerPaths() {
		return PATHS;
	}


	protected List createModuleDelegates(IVirtualComponent component) throws CoreException {
		J2EEFlexProjWebDeployable moduleDelegate = null;
		IModule module = null;
		List moduleList = new ArrayList();
		try {
			if(IModuleConstants.JST_WEB_MODULE.equals(component.getComponentTypeId())) {
				moduleDelegate = new J2EEFlexProjWebDeployable(component.getProject(), ID, component);
				module = createModule(component.getName(), component.getName(), moduleDelegate.getType(), moduleDelegate.getVersion(), moduleDelegate.getProject());
				moduleList.add(module);
				moduleDelegate.initialize(module);
			}
			// adapt(moduleDelegate, (WorkbenchComponent) workBenchModules.get(i));
		} catch (Exception e) {
			Logger.getLogger().write(e);
		} finally {
			if (module != null) {
				if (getModuleDelegate(module) == null)
					moduleDelegates.add(moduleDelegate);
			}
		}
		return moduleList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.server.core.model.ModuleFactoryDelegate#getModules()
	 */
	public IModule[] getModules() {
		cacheModules(false);
		ArrayList moduleList = new ArrayList();
		for (Iterator iter = projects.values().iterator(); iter.hasNext();) {
			IModule[] element = (IModule[]) iter.next();
			for (int j = 0; j < element.length; j++) {
				moduleList.add(element[j]);
			}
		}
		IModule[] modules = new IModule[moduleList.size()];
		moduleList.toArray(modules);
		return modules;

	}
	
	protected boolean isValidModule(IProject project) {
		if (isFlexibleProject(project)) {
	        IVirtualComponent comp = ComponentCore.createComponent(project);
            if(comp.getComponentTypeId().equals(IModuleConstants.JST_WEB_MODULE))
                return true;
	        }     
        return false;
    }
}