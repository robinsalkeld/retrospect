/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Johnson - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.snapshot.actions;

import java.util.List;

import org.eclipse.mat.query.IContextObjectSet;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Icon;
import org.eclipse.mat.snapshot.OQL;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * Combines all the OQL queries associated with the IContextObjectSets into one big query.
 */
@Icon("/icons/copy.gif")
public class CopyOQL implements IQuery
{
    @Argument
    public List<IContextObjectSet> elements;

    @Argument
    public Display display;

    public IResult execute(IProgressListener listener) throws Exception
    {
        final StringBuilder buf = new StringBuilder(128);
        String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

        for (IContextObjectSet element : elements)
        {
            String buf1 = element.getOQL();
            if (buf1 != null)
                OQL.union(buf, buf1);
        }

        buf.append(lineSeparator);

        display.asyncExec(new Runnable()
        {
            public void run()
            {
                Clipboard clipboard = new Clipboard(display);
                clipboard.setContents(new Object[] { buf.toString() }, new Transfer[] { TextTransfer
                                .getInstance() });
                clipboard.dispose();
            }
        });

        // let the UI ignore this query
        throw new IProgressListener.OperationCanceledException();

    }

}
