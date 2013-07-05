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
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;

@Name("Load and Run Code")
public class LoadAndRunAnalysisCodeQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED, advice = Advice.HEAP_OBJECT)
    public int[] objectIds;

    @Argument(isMandatory = true)
    public File classFile;
    
    @Argument(isMandatory = true)
    public String methodName;
    
    @Argument
    public boolean aggregate = false;
    
    private VirtualMachineMirror vm;
    private ThreadMirror threadMirror;
    private MethodMirror method;
    
    public IResult execute(IProgressListener listener) throws Exception
    {
            vm = HolographVMRegistry.getHolographVM(snapshot, listener);
            threadMirror = HolographVMRegistry.getThreadForEval(vm);
            
            method = defineClassAndGetMethod(listener);
            
            List<Object> results = new ArrayList<Object>();
            
            if (aggregate) {
                listener.beginTask("Running analysis code", 1);
                ObjectMirror allObjectsList = createCalculatedObjectList(objectIds);
                listener.worked(1);
                results.add(evaluate(allObjectsList, listener));
            } else {
                int count = objectIds.length;
                listener.beginTask("Running analysis code", count);
                int i = 0;
                for (int id : objectIds) {
                    listener.subTask("Running analysis code (" + ++i + " of " + count + ")");
                    ObjectMirror mirror = HolographVMRegistry.getObjectMirror(snapshot, id, listener);
                    results.add(evaluate(mirror, listener));
                    
                    listener.worked(1);
                    if (listener.isCanceled()) {
                        throw new IProgressListener.OperationCanceledException();
                    }
                }
            }
            
            listener.done();

            return new ObjectMirrorListResult.Outbound(snapshot, results);
    }
    
    private ObjectMirror createCalculatedObjectList(final int[] objectIDs) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchMethodException, MirrorInvocationTargetException {
        final IProgressListener voidListener = new VoidProgressListener();
        ClassMirror objectArrayClass = vm.getArrayClass(1, vm.findBootstrapClassMirror(Object.class.getName()));
        ObjectArrayMirror array = new CalculatedObjectArrayMirror(objectArrayClass, objectIDs.length) {
            @Override
            public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
                try {
                    return HolographVMRegistry.getObjectMirror(snapshot, objectIDs[index], voidListener);
                } catch (SnapshotException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return (ObjectMirror)vm.findBootstrapClassMirror(Arrays.class.getName()).getMethod("asList", objectArrayClass).invoke(threadMirror, null, array);
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
