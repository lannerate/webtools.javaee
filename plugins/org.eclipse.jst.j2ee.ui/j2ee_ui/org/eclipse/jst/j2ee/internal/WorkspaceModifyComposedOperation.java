/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.jst.j2ee.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author jialin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkspaceModifyComposedOperation extends org.eclipse.ui.actions.WorkspaceModifyOperation {
	
	protected List fRunnables;

	public WorkspaceModifyComposedOperation(ISchedulingRule rule) {
		super(rule);
	}
	
	/**
	 * Creates a new operation which will delegate its work to the given runnable.
	 */
	public WorkspaceModifyComposedOperation() {
		super();
	}

	public WorkspaceModifyComposedOperation(ISchedulingRule rule, List nestedRunnablesWithProgress) {
		super(rule);
		fRunnables = nestedRunnablesWithProgress;
	}

	public WorkspaceModifyComposedOperation(List nestedRunnablesWithProgress) {
		super();
		fRunnables = nestedRunnablesWithProgress;
	}

	/**
	 * Creates a new operation which will delegate its work to the given runnable.
	 * 
	 * @param content
	 *            the runnable to delegate to when this operation is executed
	 */
	public WorkspaceModifyComposedOperation(IRunnableWithProgress nestedOp) {
		super();
		getRunnables().add(nestedOp);
	}

	public boolean addRunnable(IRunnableWithProgress nestedOp) {
		return getRunnables().add(nestedOp);
	}

	protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		int size = fRunnables.size();
		monitor.beginTask("", size);//$NON-NLS-1$
		for (int i = 0; i < fRunnables.size(); i++) {
			IRunnableWithProgress op = (IRunnableWithProgress) fRunnables.get(i);
			op.run(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
		}
	}

	protected List getRunnables() {
		if (fRunnables == null)
			fRunnables = new ArrayList(3);
		return fRunnables;
	}
}