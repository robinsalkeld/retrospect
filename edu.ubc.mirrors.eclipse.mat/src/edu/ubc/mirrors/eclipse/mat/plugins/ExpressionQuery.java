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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
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
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.IProgressListener.Severity;
import org.eclipse.mat.util.VoidProgressListener;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;

@CommandName("evaluate_expression")
@Name("Evaluate Expression")
public class ExpressionQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED, advice = Advice.HEAP_OBJECT)
    public int[] objects;

    @Argument(isMandatory = true)
    public String expression = "toString()";
    
    @Argument
    public boolean aggregate = false;
    
    @Argument(isMandatory = false)
    public Integer maxObjects = null;
    
    @Argument
    public boolean inbound = false;
    
    private VirtualMachineMirror vm;
    private MirrorsVirtualMachine jdiVM;
    private IAstEvaluationEngine engine;
    private ThreadMirror threadMirror;
    private IJavaThread evalThread;
    private JDIDebugTarget debugTarget;

    private ICompiledExpression compiledExpr;
    
    public static class Value {
        
        private IEvaluationResult result;
        private final Semaphore semaphore = new Semaphore(0);
        
        public void setResult(IEvaluationResult result) {
            this.result = result;
            semaphore.release();
        }
        
        public void waitForResult() throws InterruptedException {
            semaphore.acquire();
        }
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
    
    public IResult execute(IProgressListener listener) throws Exception
    {
            vm = HolographVMRegistry.getHolographVM(snapshot, listener);
            jdiVM = new MirrorsVirtualMachine(vm);
            
            engine = HolographVMRegistry.getEvaluationEngine(vm);
            debugTarget = (JDIDebugTarget)engine.getDebugTarget();
            
            threadMirror = HolographVMRegistry.getThreadForEval(vm);
            ThreadReference jdiThread = (ThreadReference)jdiVM.wrapMirror(threadMirror);
            evalThread = debugTarget.findThread(jdiThread);
            
            int count = objects.length;
            if (maxObjects != null) {
                count = Math.min(maxObjects, count);
            }
            
            List<ObjectMirror> input;
            if (aggregate) {
                input = Collections.singletonList(createCalculatedObjectList(objects, count));
            } else {
                input = new ArrayList<ObjectMirror>(objects.length);
                for (int index = 0; index < count; index++) {
                    input.add(HolographVMRegistry.getObjectMirror(snapshot, objects[index], listener));
                }
            }
            
            listener.beginTask("Compiling expression", 1);
            ObjectReference jdiObject = jdiVM.wrapMirror(input.get(0));
            final IJavaObject thisContext = (IJavaObject)JDIValue.createValue(debugTarget, jdiObject);
            Reflection.withThread(HolographVMRegistry.getSecondaryThreadForEval(vm), new Callable<Object>() {
                public Object call() throws Exception {
                    compiledExpr = engine.getCompiledExpression(expression, thisContext);
                    return null;
                }
            });
            listener.worked(1);
            if (compiledExpr.hasErrors()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Compilation errors in expression text \"" + expression + "\": \n");
                for (String error : compiledExpr.getErrorMessages()) {
                    builder.append(error);
                    builder.append('\n');
                }
                throw new IllegalArgumentException(builder.toString());
            }
            
            List<Object> results = new ArrayList<Object>();
            listener.beginTask("Evaluating expression", input.size());
            for (ObjectMirror mirror : input) {
                results.add(evaluate(mirror, listener));
                listener.worked(1);
                
                if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
            }
            
            listener.done();

            return ObjectMirrorListResult.make(snapshot, results, inbound);
    }
    
    private Object evaluate(ObjectMirror mirror, IProgressListener listener) throws DebugException, InterruptedException {
        ObjectReference jdiObject = jdiVM.wrapMirror(mirror);
        final IJavaObject thisContext = (IJavaObject)JDIValue.createValue(debugTarget, jdiObject);
        
        final Value value = new Value();
        final IEvaluationListener evaluationListener = new IEvaluationListener() {
            public void evaluationComplete(IEvaluationResult result) {
                value.setResult(result);
            }
        };
        
        // Create a new thread to watch for cancellation and interrupt the evaluation.
        final CancellingThread cancellingThread = new CancellingThread(listener, threadMirror);
        
        // TODO-RS: The whole "secondary thread for eval" thing is to workaround a race condition here if we
        // use the same thread: the actual evaluation can start on the worker thread before this block finishes,
        // making the holographic thread complain about being used by two native threads at once.
        // This needs fixing (and by this I mean the fact that a lot of the mirrors API as I've designed it
        // requires executing code and hence a thread to evaluate with).
        Reflection.withThread(HolographVMRegistry.getSecondaryThreadForEval(vm), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                engine.evaluateExpression(compiledExpr, thisContext, evalThread, evaluationListener, DebugEvent.EVALUATION, false);
                cancellingThread.finished();
                return null;
            }
        });
        
        cancellingThread.start();
        
        // Need to synchronize since you can't evaluate multiple expressions against
        // a single ThreadMirror.
        value.waitForResult();
        IEvaluationResult result = value.result;
        
        Object object = null;
        if (result.isTerminated()) {
            throw new IProgressListener.OperationCanceledException();
        } else {
            if (result.hasErrors()) {
                if (result.getException() != null) {
                    listener.sendUserMessage(Severity.ERROR, "Exception thrown", result.getException());
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Compilation errors in expression text: \n");
                    for (String error : result.getErrorMessages()) {
                        builder.append(error);
                        builder.append('\n');
                    }
                    listener.sendUserMessage(Severity.ERROR, builder.toString(), null);
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
        return object;
    }
    
    private class CancellingThread extends Thread {
        
        public CancellingThread(IProgressListener thisListener, ThreadMirror threadMirror) {
            this.thisListener = thisListener;
            this.threadMirror = threadMirror;
        }

        private boolean stopped = false;
        private final IProgressListener thisListener;
        private final ThreadMirror threadMirror;
        
        @Override
        public void run() {
            while (!stopped) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                
                if (thisListener.isCanceled()) {
                    threadMirror.interrupt();
                    break;
                }
            }
        }
        
        public void finished() {
            stopped = true;
            try {
                join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
