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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;

@CommandName("collection_values")
@Name("Collection Values")
@Category("Java Collections")
public class CollectionValuesQuery implements IQuery
{
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED, advice = Advice.HEAP_OBJECT)
    public int collection;

    public IResult execute(IProgressListener listener) throws Exception
    {
        List<Object> entries = new ArrayList<Object>();

        ObjectMirror collectionMirror = HolographVMRegistry.getObjectMirror(snapshot, collection, listener);
        for (ObjectMirror mirror : Reflection.collectionValues(collectionMirror)) {
            entries.add(mirror);
        }
        
        return new ObjectMirrorListResult.Outbound(snapshot, entries);
    }
}
