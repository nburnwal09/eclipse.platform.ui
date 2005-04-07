/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String FORMS_RESOURCE_BUNDLE = "org.eclipse.ui.internal.forms.FormsMessages";

    static {
        initializeMessages(FORMS_RESOURCE_BUNDLE, Messages.class);
    }

    public static String FormText_copy;
    // not used so far.
    // public static String FormText_copyShortcut;


}
