/***********************************************************************
 * Copyright (c) 2008 by SAP AG, Walldorf. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 ***********************************************************************/
package org.eclipse.jst.jee.ui.internal.navigator.web;

import java.net.URL;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
import org.eclipse.jst.javaee.web.WebApp;
import org.eclipse.jst.jee.ui.internal.Messages;
import org.eclipse.jst.jee.ui.internal.navigator.AbstractGroupProvider;
import org.eclipse.jst.jee.ui.plugin.JEEUIPluginIcons;
import org.eclipse.swt.graphics.Image;

/**
 * Servlet Mappings group in Deployment Descriptor node. 
 * 
 * @author Dimitar Giormov
 *
 */
public class GroupServletMappingItemProvider extends AbstractGroupProvider{

	private static Image SERVLETMAPPING;

	public GroupServletMappingItemProvider(WebApp webApp) {
		super(webApp);
		text = Messages.SERVLET_MAPPING_ITEM_PROVIDER;
	}

	@Override
	public List getChildren() {
		if (javaee != null){
			return ((WebApp) javaee).getServletMappings();
		}
		return null;
	}

	@Override
	public Image getImage() {
		return getServletMappingImage();
	}

	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	public static Image getServletMappingImage() {
		if (SERVLETMAPPING == null) {
			URL url = (URL) J2EEPlugin.getPlugin().getImage(JEEUIPluginIcons.SERVLET_MAPPING);
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url); 
			SERVLETMAPPING = imageDescriptor.createImage();
		}
		return SERVLETMAPPING;
	}

}