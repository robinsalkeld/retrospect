package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.IHeapObjectArgument;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.VoidProgressListener;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.mirages.Reflection;

@Name("Evaluate Expression")
public class ExpressionQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED)
    public IHeapObjectArgument objects;

    @Argument(isMandatory = true)
    public String expression = "toString()";
    
    private VirtualMachineMirror vm;
    private MirrorsVirtualMachine jdiVM;
    private IEvaluationEngine engine;
    private ThreadMirror threadMirror;
    private IJavaThread evalThread;
    private JDIDebugTarget debugTarget;
    private List<Value> results;
            
    public static class Value {
        
        private Object object;
        private final Semaphore semaphore = new Semaphore(0);
        
        public void setObject(Object object) {
            this.object = object;
            semaphore.release();
        }
        
        public void waitForResult() throws InterruptedException {
            semaphore.acquire();
        }
        
        public String getResult() {
            if (object instanceof ObjectMirror) {
                return HolographVMRegistry.toString((ObjectMirror)object);
            } else {
                return String.valueOf(object);
            }
        }
    }
    
    public IResult execute(IProgressListener listener) throws Exception
    {
            vm = HolographVMRegistry.getHolographVM(snapshot, listener);
            jdiVM = new MirrorsVirtualMachine(vm);
            
            engine = HolographVMRegistry.getEvaluationEngine(vm);
            debugTarget = (JDIDebugTarget)engine.getDebugTarget();
            
            threadMirror = HolographVMRegistry.getThreadForEval(vm);
            ThreadReference jdiThread = (ThreadReference)jdiVM.wrapMirror(threadMirror);
            evalThread = debugTarget.findThread(jdiThread);
            
            results = new ArrayList<Value>();
            
//            if (aggregate) {
//                listener.beginTask("Evaluating expression", 1);
//                ObjectMirror allObjectsList = createCalculatedObjectList(objects.getIds(listener));
//                results.add(evaluate(allObjectsList));
//            } else {
                int[] ids = objects.getIds(listener);
                listener.beginTask("Evaluating expression", ids.length);
                for (int id : ids) {
                    IObject obj = snapshot.getObject(id);
                    ObjectMirror mirror = HolographVMRegistry.getMirror(obj, listener);
                    results.add(evaluate(mirror));
                    
                    listener.worked(1);
                    if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
                }
//            }
            
            listener.done();

            return new org.eclipse.mat.query.results.ListResult(Value.class, results, "result");
    }
    
    private ObjectMirror createCalculatedObjectList(final int[] objectIDs) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchMethodException, MirrorInvocationTargetException {
        final IProgressListener voidListener = new VoidProgressListener();
        ClassMirror objectArrayClass = vm.getArrayClass(1, vm.findBootstrapClassMirror(Object.class.getName()));
        ObjectArrayMirror array = new CalculatedObjectArrayMirror(objectArrayClass, objectIDs.length) {
            @Override
            public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
                IObject obj;
                try {
                    obj = snapshot.getObject(objectIDs[index]);
                } catch (SnapshotException e) {
                    throw new RuntimeException(e);
                }
                return HolographVMRegistry.getMirror(obj, voidListener);
            }
        };
        return (ObjectMirror)vm.findBootstrapClassMirror(Arrays.class.getName()).getMethod("asList", objectArrayClass).invoke(threadMirror, null, array);
    }
    
    private Value evaluate(ObjectMirror mirror) throws DebugException, InterruptedException {
        ObjectReference jdiObject = jdiVM.wrapMirror(mirror);
        final IJavaObject thisContext = (IJavaObject)JDIValue.createValue(debugTarget, jdiObject);
        
        final Value value = new Value();
        final IEvaluationListener thisListener = new IEvaluationListener() {
            public void evaluationComplete(IEvaluationResult result) {
                Object object = null;
                try {
                    if (!result.isTerminated()) {
                        if (result.hasErrors()) {
                            if (result.getException() != null) {
                                result.getException().printStackTrace();
                            } else {
                                for (String error : result.getErrorMessages()) {
                                    System.out.println(error);
                                }
                            }
                        } else {
                            IValue resultValue = result.getValue();
                            
                            if (resultValue != null) {
                                if (resultValue instanceof JDIObjectValue) {
                                    ObjectReference jdiValue = ((JDIObjectValue)resultValue).getUnderlyingObject();
                                    if (jdiValue != null) {
                                        object = ((MirrorsObjectReference)jdiValue).getWrapped();
                                    }
                                } else if (resultValue instanceof JDIPrimitiveValue) {
                                    // TODO-RS: Complete
                                    object = ((JDIPrimitiveValue)resultValue).getIntValue();
                                }
                            }

                            
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                value.setObject(object);
            }

        };
        
        // TODO-RS: The whole "secondary thread for eval" thing is to workaround a race condition here if we
        // use the same thread: the actual evaluation can start on the worker thread before this block finishes,
        // making the holographic thread complain about being used by two native threads at once.
        // This needs fixing (and by this I mean the fact that a lot of the mirrors API as I've designed it
        // requires executing code and hence a thread to evaluate with).
        Reflection.withThread(HolographVMRegistry.getSecondaryThreadForEval(vm), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                engine.evaluate(expression, thisContext, evalThread, thisListener, DebugEvent.EVALUATION, false);
                return null;
            }
        });
        
        // Need to synchronize since you can't evaluate multiple expressions against
        // a single ThreadMirror.
        value.waitForResult();
        
        return value;
    }
}
