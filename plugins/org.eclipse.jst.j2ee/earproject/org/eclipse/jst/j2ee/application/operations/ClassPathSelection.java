/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.jst.j2ee.application.operations;



import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
import org.eclipse.jst.j2ee.commonarchivecore.internal.Container;
import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ManifestException;
import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
import org.eclipse.jst.j2ee.internal.archive.operations.EARProjectLoadStrategyImpl;
import org.eclipse.jst.j2ee.internal.archive.operations.J2EELoadStrategyImpl;
import org.eclipse.jst.j2ee.internal.earcreation.EARNatureRuntime;
import org.eclipse.jst.j2ee.internal.earcreation.ModuleMapHelper;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;

import com.ibm.wtp.common.logger.proxy.Logger;


public class ClassPathSelection {
	protected Archive archive;
	protected IProject earProject;
	protected List classpathElements;
	protected Map urisToElements;
	protected boolean modified;
	private String targetProjectName;
	protected Map ejbToClientJARs = null;
	protected Map clientToEJBJARs = null;
	public static final int FILTER_EJB_SERVER_JARS = 0;
	public static final int FILTER_EJB_CLIENT_JARS = 1;
	public static final int FILTER_NONE = 2;

	protected int filterLevel = 2;

	protected static Comparator comparator = new Comparator() {
		/**
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Object o1, Object o2) {
			Archive a1 = (Archive) o1;
			Archive a2 = (Archive) o2;
			return a1.getURI().compareTo(a2.getURI());
		}
	};

	public ClassPathSelection(Archive anArchive, String targetProjectName, EARFile earFile) {
		super();
		archive = anArchive;
		this.targetProjectName = targetProjectName;
		initializeEARProject(earFile);
		initializeElements();
	}

	/**
	 * ClassPathSelection constructor comment.
	 */
	public ClassPathSelection(Archive anArchive, EARFile earFile) {
		super();
		archive = anArchive;
		initializeEARProject(earFile);
		initializeElements();
	}

	protected ClasspathElement createElement(Archive referencingArchive, Archive referencedArchive, String cpEntry) {
		ClasspathElement element = new ClasspathElement(referencingArchive);
		element.setValid(true);
		element.setText(referencedArchive.getURI());
		element.setTargetArchive(referencedArchive);
		element.setEarProject(earProject);
		setProjectValues(element, referencedArchive);
		if (cpEntry != null)
			element.setValuesSelected(cpEntry);
		setType(element, referencedArchive);
		return element;
	}

	protected ClasspathElement createInvalidElement(String cpEntry) {
		ClasspathElement element = new ClasspathElement(archive);
		element.setValid(false);
		element.setSelected(true);
		element.setRelativeText(cpEntry);
		element.setText(cpEntry);
		element.setEarProject(earProject);
		setInvalidProject(element);
		return element;
	}

	/**
	 * @param element
	 */
	private void setInvalidProject(ClasspathElement element) {
		IProject earProj = element.getEarProject();
		EARNatureRuntime nature = EARNatureRuntime.getRuntime(earProj);
		if (nature != null) {
			IProject mappedProject = nature.getMappedProject(element.getRelativeText());
			element.setProject(mappedProject);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (8/22/2001 1:17:21 PM)
	 * 
	 * @return java.util.List
	 */
	public java.util.List getClasspathElements() {
		return classpathElements;
	}

	/**
	 * Adapter method to convert the manifest class path entries which map to a project to a list of
	 * classpath entries for a java build path
	 */
	protected IClasspathEntry[] getClasspathEntries(boolean filterSelected) {
		List result = new ArrayList();
		IClasspathEntry[] array = null;
		;
		ClasspathElement element = null;
		for (int i = 0; i < classpathElements.size(); i++) {
			element = (ClasspathElement) classpathElements.get(i);
			if (filterSelected && !element.isSelected())
				continue;
			array = ((ClasspathElement) classpathElements.get(i)).newClasspathEntries();
			if (array == null)
				continue;
			for (int j = 0; j < array.length; j++) {
				if (!result.contains(array[j]))
					result.add(array[j]);
			}
		}
		return (IClasspathEntry[]) result.toArray(new IClasspathEntry[result.size()]);
	}

	/**
	 * Adapter method to convert the manifest class path entries which map to a project to a list of
	 * classpath entries for a java build path
	 */
	public IClasspathEntry[] getClasspathEntriesForAll() {
		return getClasspathEntries(false);
	}

	/**
	 * Adapter method to convert the manifest class path entries which map to a project to a list of
	 * classpath entries for a java build path
	 */
	public IClasspathEntry[] getClasspathEntriesForSelected() {
		return getClasspathEntries(true);
	}

	protected EARFile getEARFile() {
		if (archive == null)
			return null;

		Container parent = archive.getContainer();
		if (parent != null && parent.isEARFile())
			return (EARFile) parent;
		return null;
	}

	protected static IProject getEARProject(Archive anArchive) {
		Container c = anArchive.getContainer();
		if (!c.isEARFile())
			return null;
		EARFile ear = (EARFile) c;
		LoadStrategy loader = ear.getLoadStrategy();
		if (!(loader instanceof EARProjectLoadStrategyImpl))
			return null;

		return ((EARProjectLoadStrategyImpl) loader).getProject();
	}

	public Archive getArchive() {
		return archive;
	}

	protected IProject getProject(Archive anArchive) {
		LoadStrategy loader = anArchive.getLoadStrategy();
		if (!(loader instanceof J2EELoadStrategyImpl))
			return ModuleMapHelper.getProject(anArchive.getURI(), getEARFile());

		return ((J2EELoadStrategyImpl) loader).getProject();
	}

	public String getText() {
		return archive.getURI();
	}

	protected Archive getArchive(String uri, List archives) {
		for (int i = 0; i < archives.size(); i++) {
			Archive anArchive = (Archive) archives.get(i);
			if (anArchive.getURI().equals(uri))
				return anArchive;
		}
		return null;
	}

	protected void initializeElements() {
		ejbToClientJARs = J2EEProjectUtilities.collectEJBClientJARs(getEARFile());
		clientToEJBJARs = reverse(ejbToClientJARs);
		classpathElements = new ArrayList();
		urisToElements = new HashMap();
		String[] cp = new String[0];
		try {
			cp = archive.getManifest().getClassPathTokenized();
		} catch (ManifestException ex) {
			Logger.getLogger().logError(ex);
		}
		List archives = getEARFile().getArchiveFiles();
		Archive other = null;
		ClasspathElement element = null;
		for (int i = 0; i < cp.length; i++) {
			String cpEntry = cp[i];
			String uri = ArchiveUtil.deriveEARRelativeURI(cpEntry, archive);
			other = getArchive(uri, archives);
			if (other != null && ArchiveUtil.isValidDependency(other, archive)) {
				element = createElement(archive, other, cpEntry);
				archives.remove(other);
			} else {
				element = createInvalidElement(cpEntry);
				if (element.representsImportedJar()) {
					element.setValid(true);
					element.setProject(getProject(archive));
				}
				if (other != null)
					element.setProject(getProject(other));
			}
			addClasspathElement(element, uri);
		}
		Collections.sort(archives, comparator);
		//Anything that remains in the list of available archives that is valid should be
		//available for selection
		for (int i = 0; i < archives.size(); i++) {
			other = (Archive) archives.get(i);
			if (other != archive && ArchiveUtil.isValidDependency(other, archive)) {
				IProject project = getProject(other);
				if (null == targetProjectName || null == project || !project.getName().equals(targetProjectName)) {
					element = createElement(archive, other, null);
					element.setProject(getProject(other));
					addClasspathElement(element, other.getURI());
				}
			}
		}
	}

	private void initializeEARProject(EARFile earFile) {
		LoadStrategy loadStrat = earFile.getLoadStrategy();
		if (loadStrat instanceof EARProjectLoadStrategyImpl)
			earProject = ((EARProjectLoadStrategyImpl) loadStrat).getProject();
	}

	private void setType(ClasspathElement element, Archive other) {
		if (other == null)
			return;
		else if (clientToEJBJARs.containsKey(other))
			element.setJarType(ClasspathElement.EJB_CLIENT_JAR);
		else if (other.isEJBJarFile())
			element.setJarType(ClasspathElement.EJB_JAR);
	}

	/**
	 * @param localejbToClientJARs
	 * @return
	 */
	private Map reverse(Map localejbToClientJARs) {
		if (localejbToClientJARs == null || localejbToClientJARs.isEmpty())
			return Collections.EMPTY_MAP;
		Map result = new HashMap();
		Iterator iter = localejbToClientJARs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			result.put(entry.getValue(), entry.getKey());
		}
		return result;
	}

	protected void addClasspathElement(ClasspathElement element, String uri) {
		classpathElements.add(element);
		urisToElements.put(uri, element);
		element.setParentSelection(this);
	}

	/**
	 * Insert the method's description here. Creation date: (8/22/2001 6:05:11 PM)
	 * 
	 * @return boolean
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Insert the method's description here. Creation date: (8/22/2001 6:05:11 PM)
	 * 
	 * @param newModified
	 *            boolean
	 */
	public void setModified(boolean newModified) {
		modified = newModified;
	}

	protected void setProjectValues(ClasspathElement element, Archive referencedArchive) {
		IProject p = getProject(referencedArchive);
		if (p == null)
			return;

		element.setProject(p);
		//Handle the imported jars in the project
		String[] cp = null;
		try {
			cp = referencedArchive.getManifest().getClassPathTokenized();
		} catch (ManifestException mfEx) {
			Logger.getLogger().logError(mfEx);
			cp = new String[]{};
		}
		List paths = new ArrayList(cp.length);
		for (int i = 0; i < cp.length; i++) {

			IFile file = null;
			try {
				file = p.getFile(cp[i]);
			} catch (IllegalArgumentException invalidPath) {
				continue;
			}
			if (file.exists())
				paths.add(file.getFullPath());
		}
		if (!paths.isEmpty())
			element.setImportedJarPaths(paths);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < classpathElements.size(); i++) {
			ClasspathElement element = (ClasspathElement) classpathElements.get(i);
			if (element.isSelected()) {
				sb.append(element.getRelativeText());
				sb.append(" "); //$NON-NLS-1$
			}
		}
		//Remove the trailing space
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public void setAllSelected(boolean selected) {
		setAllSelected(classpathElements, selected);
	}

	public void setAllSelected(List elements, boolean selected) {
		for (int i = 0; i < elements.size(); i++) {
			ClasspathElement elmt = (ClasspathElement) elements.get(i);
			elmt.setSelected(selected);
		}
	}

	/* borrowed code from jdt */
	protected List moveUp(List elements, List move) {
		int nElements = elements.size();
		List res = new ArrayList(nElements);
		Object floating = null;
		for (int i = 0; i < nElements; i++) {
			Object curr = elements.get(i);
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null) {
					res.add(floating);
				}
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		return res;
	}

	/* borrowed code from jdt */
	public void moveUp(List toMoveUp) {
		setModifiedIfAnySelected(toMoveUp);
		if (toMoveUp.size() > 0)
			classpathElements = moveUp(classpathElements, toMoveUp);
	}

	/* borrowed code from jdt */
	public void moveDown(List toMoveDown) {
		setModifiedIfAnySelected(toMoveDown);
		if (toMoveDown.size() > 0)
			classpathElements = reverse(moveUp(reverse(classpathElements), toMoveDown));

	}

	/* borrowed code from jdt */
	protected List reverse(List p) {
		List reverse = new ArrayList(p.size());
		for (int i = p.size() - 1; i >= 0; i--) {
			reverse.add(p.get(i));
		}
		return reverse;
	}

	public ClasspathElement getClasspathElement(String uri) {
		if (urisToElements == null)
			return null;
		return (ClasspathElement) urisToElements.get(uri);
	}

	public ClasspathElement getClasspathElement(IProject archiveProject) {
		if (archiveProject != null) {
			for (int i = 0; i < classpathElements.size(); i++) {
				ClasspathElement elmnt = (ClasspathElement) classpathElements.get(i);
				if (archiveProject.equals(elmnt.getProject()))
					return elmnt;
			}
		}
		return null;
	}

	public boolean hasDirectOrIndirectDependencyTo(IProject archiveProject) {
		ClasspathElement element = getClasspathElement(archiveProject);
		if (element == null)
			return false;
		Archive anArchive = null;
		if (element.isValid()) {
			try {
				anArchive = (Archive) getEARFile().getFile(element.getText());
			} catch (FileNotFoundException e) {
			}
		}
		return anArchive != null && archive.hasClasspathVisibilityTo(anArchive);
	}

	public boolean hasDirectOrIndirectDependencyTo(String jarName) {
		ClasspathElement element = getClasspathElement(jarName);
		if (element == null)
			return false;
		Archive anArchive = null;
		if (element.isValid()) {
			try {
				anArchive = (Archive) getEARFile().getFile(element.getText());
			} catch (FileNotFoundException e) {
			}
		}
		return anArchive != null && archive.hasClasspathVisibilityTo(anArchive);
	}


	public boolean isAnyJarSelected(int type) {
		if (classpathElements != null) {
			for (int i = 0; i < classpathElements.size(); i++) {
				ClasspathElement element = (ClasspathElement) classpathElements.get(i);
				if (element.getJarType() == type && element.isSelected())
					return true;
			}
		}
		return false;
	}


	public boolean isAnyEJBJarSelected() {
		return isAnyJarSelected(ClasspathElement.EJB_JAR);
	}



	public boolean isAnyEJBClientJARSelected() {
		return isAnyJarSelected(ClasspathElement.EJB_CLIENT_JAR);

	}

	/**
	 * @return
	 */
	public int getFilterLevel() {
		return filterLevel;
	}

	/**
	 * @param i
	 */
	public void setFilterLevel(int i) {
		filterLevel = i;
	}

	/**
	 * This method selects or deselects indivual elements based on the filter level, and
	 * additionally sets the filter level.
	 * 
	 * @param i
	 */
	public void selectFilterLevel(int level) {
		setFilterLevel(level);
		switch (level) {
			case FILTER_EJB_CLIENT_JARS :
				invertClientJARSelections(ClasspathElement.EJB_CLIENT_JAR);
				break;
			case FILTER_EJB_SERVER_JARS :
				invertClientJARSelections(ClasspathElement.EJB_JAR);
				break;
			default :
				break;
		}
	}

	public void invertClientJARSelection(IProject aProject, IProject opposite) {
		ClasspathElement element = getClasspathElement(aProject);
		ClasspathElement oppositeElement = (opposite == null ? null : getClasspathElement(opposite));
		if (element.isSelected())
			invertSelectionIfPossible(element, oppositeElement);
	}

	private void invertClientJARSelections(int elementType) {
		if (classpathElements == null)
			return;

		for (int i = 0; i < classpathElements.size(); i++) {
			ClasspathElement element = (ClasspathElement) classpathElements.get(i);
			if (element.getJarType() == elementType && element.isSelected()) {
				invertSelectionIfPossible(element, null);
			}
		}
	}

	/**
	 * @param element
	 * @param elementType
	 */
	private void invertSelectionIfPossible(ClasspathElement element, ClasspathElement opposite) {
		if (element == null)
			return;
		if (opposite == null)
			opposite = getOppositeElement(element);
		if (opposite != null) {
			opposite.setSelected(true);
			element.setSelected(false);
		}
	}

	/**
	 * If the element represents an EJB client JAR, returns the corresponding server JAR. If the
	 * element represents an EJB server JAR, returns the corresponding client JAR.
	 */
	public ClasspathElement getOppositeElement(ClasspathElement element) {
		String uri = element.getText();
		Archive target = element.getTargetArchive();
		if (uri == null || target == null)
			return null;
		Archive oppositeJAR = null;
		switch (element.getJarType()) {
			case (ClasspathElement.EJB_CLIENT_JAR) :
				oppositeJAR = (Archive) clientToEJBJARs.get(target);
				break;
			case (ClasspathElement.EJB_JAR) :
				oppositeJAR = (Archive) ejbToClientJARs.get(target);
				break;
			default :
				break;
		}
		if (oppositeJAR != null)
			return getClasspathElement(oppositeJAR.getURI());

		return null;
	}

	private void setModifiedIfAnySelected(List elements) {
		for (int i = 0; i < elements.size(); i++) {
			ClasspathElement element = (ClasspathElement) elements.get(i);
			if (element.isSelected())
				setModified(true);
		}
	}

	public boolean isMyClientJAR(ClasspathElement element) {
		if (element == null)
			return false;

		Archive myClientJar = (Archive) ejbToClientJARs.get(archive);
		return myClientJar != null && myClientJar == element.getTargetArchive();
	}

}