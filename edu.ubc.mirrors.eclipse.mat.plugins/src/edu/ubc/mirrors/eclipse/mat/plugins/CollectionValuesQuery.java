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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.ObjectListResult;
import org.eclipse.mat.util.IProgressListener;

import edu.ubc.mirrors.MethodHandle;
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
    public int collectionID;

    public IResult execute(IProgressListener listener) throws Exception
    {
        List<Object> entries = new ArrayList<Object>();

        ObjectMirror collectionMirror = HolographVMRegistry.getObjectMirror(snapshot, collectionID, listener);
        for (ObjectMirror mirror : getValues(collectionMirror)) {
            entries.add(mirror);
        }
        
        return new ObjectMirrorListResult.Outbound(snapshot, entries);
    }
    
    public static List<ObjectMirror> getValues(ObjectMirror collection) {
        ObjectMirror iterator = (ObjectMirror)Reflection.invokeMethodHandle(collection, new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Collection<?>)null).iterator();
            }
        });
        MethodHandle nextMethod = new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Iterator<?>)null).next();
            }  
        };
        MethodHandle hasNextMethod = new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Iterator<?>)null).hasNext();
            }  
        };
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        while (((Boolean)Reflection.invokeMethodHandle(iterator, hasNextMethod)).booleanValue()) {
            ObjectMirror element = (ObjectMirror)Reflection.invokeMethodHandle(iterator, nextMethod);
            result.add(element);
        }
        return result;
    }
}
