/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;


/**
 * <p>
 * UndoActionHandler provides common behavior for performing an undo, as
 * well as enabling and labelling the undo menu item.  
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public class UndoActionHandler extends OperationHistoryActionHandler {

	/**
	 * Construct an action handler that handles the labelling and enabling of
	 * the undo action for a specified operation context.
	 * 
	 * @param site -
	 *            the workbench part site that created the action.
	 * @param context -
	 *            the context to be used for the undo
	 */
	public UndoActionHandler(IWorkbenchPartSite site, IUndoContext context) {
		super(site, context);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
	}

	protected void flush() {
		getHistory().dispose(undoContext, true, false, false);
	}

	protected String getCommandString() {
		return WorkbenchMessages.Workbench_undo;
	}

	protected IUndoableOperation getOperation() {
		return getHistory().getUndoOperation(undoContext);

	}

	public void run() {
		try {
			getHistory().undo(undoContext, getProgressMonitor(), this);
		} catch (ExecutionException e) {
			reportException(e);
		}
	}

	protected boolean shouldBeEnabled() {
		// if a context was not supplied, do not enable.
		if (undoContext == null)
			return false;
		return getHistory().canUndo(undoContext);
	}
}
