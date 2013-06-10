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
package org.eclipse.mat.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.mat.ui.MemoryAnalyserPlugin;
import org.eclipse.mat.ui.Messages;
import org.eclipse.ui.PlatformUI;

public class OpenHelpPageAction extends Action
{
    private String helpUrl;

    public OpenHelpPageAction(String helpUrl)
    {
        super(Messages.OpenHelpPageAction_Help, MemoryAnalyserPlugin
                        .getImageDescriptor(MemoryAnalyserPlugin.ISharedImages.HELP));
        this.helpUrl = helpUrl;
    }

    @Override
    public void run()
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
    }
}
