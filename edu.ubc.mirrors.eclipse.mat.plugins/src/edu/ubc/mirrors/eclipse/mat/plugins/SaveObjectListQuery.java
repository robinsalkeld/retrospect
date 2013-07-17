/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - enhancements and fixes
 *******************************************************************************/
package edu.ubc.mirrors.eclipse.mat.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.query.IHeapObjectArgument;
import org.eclipse.mat.util.IProgressListener;

@CommandName("save_object_list")
@Name("Save Object List")
public class SaveObjectListQuery implements IQuery
{
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED)
    public IHeapObjectArgument objects;

    @Argument(isMandatory = true, advice = Advice.SAVE)
    public File path;
    
    public IResult execute(IProgressListener listener) throws Exception
    {
        PrintStream out = new PrintStream(new FileOutputStream(path));
        for (int[] objIDs : objects) {
            for (int objID : objIDs) {
                out.println(objID);
            }
        }
        out.flush();
        out.close();

        // let the UI ignore this query
        throw new IProgressListener.OperationCanceledException();
    }
}
