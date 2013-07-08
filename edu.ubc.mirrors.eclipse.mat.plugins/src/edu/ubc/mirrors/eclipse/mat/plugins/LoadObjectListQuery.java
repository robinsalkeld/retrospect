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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.query.ObjectListResult;
import org.eclipse.mat.util.IProgressListener;

@CommandName("load_object_list")
@Name("Load Object List")
public class LoadObjectListQuery implements IQuery
{
    @Argument
    public ISnapshot snapshot;

    @Argument(isMandatory = true)
    public File path;
    
    public IResult execute(IProgressListener listener) throws Exception
    {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        int[] objectIDs = new int[lines.size()];
        for (int i = 0; i < objectIDs.length; i++) {
            objectIDs[i] = Integer.valueOf(lines.get(i));
        }
        
        return new ObjectListResult.Outbound(snapshot, objectIDs);
    }
}
