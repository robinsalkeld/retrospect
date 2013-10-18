/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.eclipse.mat.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.VoidProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

@Name("Load and Run Code")
public class LoadAndRunAnalysisCodeQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED, advice = Advice.HEAP_OBJECT)
    public int[] objectIds;

    @Argument(isMandatory = true)
    public File classFile = new File("/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/org/eclipse/cdt/internal/core/dom/parser/cpp/CPPASTNameDuplicateAnalysis.class");
    
    @Argument(isMandatory = true)
    public String methodName = "filterByLocations";
    
    @Argument
    public boolean aggregate = true;
    
    @Argument
    public Integer maxObjects = null;
    
    @Argument
    public boolean inbound = true;
    
    private VirtualMachineMirror vm;
    private ThreadMirror threadMirror;
    private MethodMirror method;
    
    public IResult execute(IProgressListener listener) throws Exception
    {
            vm = HolographVMRegistry.getHolographVM(snapshot, listener);
            threadMirror = HolographVMRegistry.getThreadForEval(vm);
            
            method = defineClassAndGetMethod(listener);
            
            List<Object> results = new ArrayList<Object>();
            int count = objectIds.length;
            if (maxObjects != null) {
                count = Math.min(maxObjects, count);
            }
            
            if (aggregate) {
                listener.beginTask("Running analysis code", 1);
                ObjectMirror allObjectsList = createCalculatedObjectList(objectIds, count);
                results.add(evaluate(allObjectsList, listener));
                listener.worked(1);
            } else {
                listener.beginTask("Running analysis code", count);
                int i = 0;
                for (int index = 0; index < count; index++) {
                    listener.subTask("Running analysis code (" + ++i + " of " + count + ")");
                    ObjectMirror mirror = HolographVMRegistry.getObjectMirror(snapshot, objectIds[index], listener);
                    results.add(evaluate(mirror, listener));
                    
                    listener.worked(1);
                    if (listener.isCanceled()) {
                        throw new IProgressListener.OperationCanceledException();
                    }
                }
            }
            
            listener.done();

            return ObjectMirrorListResult.make(snapshot, results, inbound);
    }
    
    private ObjectMirror createCalculatedObjectList(final int[] objectIDs, int count) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchMethodException, MirrorInvocationTargetException {
        final IProgressListener voidListener = new VoidProgressListener();
        ClassMirror objectArrayClass = vm.getArrayClass(1, vm.findBootstrapClassMirror(Object.class.getName()));
        ObjectArrayMirror array = new CalculatedObjectArrayMirror(objectArrayClass, count) {
            @Override
            public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
                try {
                    int objectID = objectIDs[index];
                    System.out.println(index + ": " + objectID);
                    return HolographVMRegistry.getObjectMirror(snapshot, objectID, voidListener);
                } catch (SnapshotException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return (ObjectMirror)vm.findBootstrapClassMirror(Arrays.class.getName()).getDeclaredMethod("asList", "java.lang.Object[]").invoke(threadMirror, null, array);
    }
    
    private MethodMirror defineClassAndGetMethod(final IProgressListener listener) throws IOException, SnapshotException, SecurityException, NoSuchMethodException {
        return Reflection.withThread(threadMirror, new Callable<MethodMirror>() {
            @Override
            public MethodMirror call() throws Exception {
                FileInputStream fis = new FileInputStream(classFile);
                byte[] analyzerClassBytecode = NativeClassMirror.readFully(fis);
                fis.close();
                String className = Reflection.getClassNameFromBytecode(analyzerClassBytecode);
                
                // TODO-RS: Find a way to avoid the assumption that these are all the same kinds of objects 
                // (and that there's at least one).
                ClassMirror firstObjectClass = HolographVMRegistry.getObjectMirror(snapshot, objectIds[0], listener).getClassMirror();
                ClassMirrorLoader loader = firstObjectClass.getLoader();
                ClassMirror analyzerClass = Reflection.injectBytecode(vm, threadMirror, loader, className, analyzerClassBytecode);
                
                MethodMirror result = null;
                for (MethodMirror method : analyzerClass.getDeclaredMethods(true)) {
                    if (method.getName().equals(methodName)) {
                        if (result != null) {
                            throw new IllegalArgumentException("More than one method found: " + methodName);
                        }
                        result = method; 
                    }
                }
                if (result == null) {
                    throw new IllegalArgumentException("No method found: " + methodName);
                }
                
                return result;
            }
        });
    }
    
    private Object evaluate(ObjectMirror mirror, IProgressListener listener) throws IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException {
        return method.invoke(threadMirror, null, mirror);
    }
}
