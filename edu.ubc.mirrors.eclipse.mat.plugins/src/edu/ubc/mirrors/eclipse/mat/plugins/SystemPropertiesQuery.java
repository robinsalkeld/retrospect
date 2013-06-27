/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package edu.ubc.mirrors.eclipse.mat.plugins;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.SnapshotQuery;
import org.eclipse.mat.util.IProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.Reflection;

@CommandName("system_properties")
@Name("System Properties")
@Category("Java Basics")
public class SystemPropertiesQuery implements IQuery
{
    @Argument
    public ISnapshot snapshot;

    public MapEntriesQuery.Result execute(IProgressListener listener) throws Exception
    {
        VirtualMachineMirror vm = HolographVMRegistry.getHolographVM(snapshot, listener);
        ClassMirror systemClass = vm.findAllClasses(System.class.getName(), false).get(0);
        ThreadMirror thread = vm.getThreads().get(0);
        ObjectMirror propertiesMirror = (ObjectMirror)Reflection.invokeStaticMethodHandle(thread, systemClass, new MethodHandle() {
            protected void methodCall() throws Throwable {
                System.getProperties();
            }
        });
        IObject properties = HolographVMRegistry.fromMirror(propertiesMirror); 
        
        return (MapEntriesQuery.Result) SnapshotQuery.lookup("map_entries", snapshot) //$NON-NLS-1$
                        .setArgument("objects", properties) //$NON-NLS-1$
                        .execute(listener);
    }

}
