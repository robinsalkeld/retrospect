/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.snapshot.editor;

import org.eclipse.mat.ui.Messages;
import org.eclipse.mat.ui.editor.AbstractEditorPane;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

public abstract class HeapEditorPane extends AbstractEditorPane
{
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        if (!(input instanceof ISnapshotEditorInput))
            throw new PartInitException(Messages.HeapEditorPane_EditorInputMustBeOfType + getClass().getName());

        super.init(site, input);
    }

    public ISnapshotEditorInput getSnapshotInput()
    {
        return (ISnapshotEditorInput) getEditorInput();
    }

}
