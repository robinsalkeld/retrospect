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

import org.eclipse.core.runtime.IPath;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.ui.IEditorInput;

public interface ISnapshotEditorInput extends IEditorInput
{
    interface IChangeListener
    {
        void onSnapshotLoaded(ISnapshot snapshot);

        void onBaselineLoaded(ISnapshot snapshot);
    }

    IPath getPath();

    ISnapshot getSnapshot();

    boolean hasSnapshot();

    ISnapshot getBaseline();

    void setBaseline(ISnapshot snapshot);

    boolean hasBaseline();

    void addChangeListener(IChangeListener listener);

    void removeChangeListener(IChangeListener listener);
}
