/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.internal.ui.refactoring.Assert;

/**
 * Instance of this class represent the input for a {@link IChangePreviewViewer}.
 * The input manages the Change object the viewer is associated with
 * <p>
 * This class is not intended to be extended outside the refactoring
 * framework.
 * </p>
 * 
 * @since 3.0
 */
public class ChangePreviewViewerInput {
	private Change fChange;
	
	/**
	 * Creates a new input object for the given change.
	 * 
	 * @param change the change object
	 */
	public ChangePreviewViewerInput(Change change) {
		Assert.isNotNull(change);
		fChange= change;
	}
	
	/**
	 * Returns the change of this input object.
	 * 
	 * @return the change of this input object
	 */
	public Change getChange() {
		return fChange;
	}
}
