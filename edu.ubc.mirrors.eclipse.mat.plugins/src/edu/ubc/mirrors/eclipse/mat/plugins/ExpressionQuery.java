package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.ArrayList;
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
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.IProgressListener.Severity;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.mirages.Reflection;

@Name("Evaluate Expression")
public class ExpressionQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED, advice = Advice.HEAP_OBJECT)
    public int[] objectIds;

    @Argument(isMandatory = true)
    public String expression = "toString()";
    
    @Argument
    public boolean inbound = false;
    
    private VirtualMachineMirror vm;
    private MirrorsVirtualMachine jdiVM;
    private IEvaluationEngine engine;
    private ThreadMirror threadMirror;
    private IJavaThread evalThread;
    private JDIDebugTarget debugTarget;
            
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
    
    public IResult execute(IProgressListener listener) throws Exception
    {
            vm = HolographVMRegistry.getHolographVM(snapshot, listener);
            jdiVM = new MirrorsVirtualMachine(vm);
            
            engine = HolographVMRegistry.getEvaluationEngine(vm);
            debugTarget = (JDIDebugTarget)engine.getDebugTarget();
            
            threadMirror = HolographVMRegistry.getThreadForEval(vm);
            ThreadReference jdiThread = (ThreadReference)jdiVM.wrapMirror(threadMirror);
            evalThread = debugTarget.findThread(jdiThread);
            
            List<Object> results = new ArrayList<Object>();
            
            listener.beginTask("Evaluating expression", objectIds.length);
            for (int id : objectIds) {
                ObjectMirror mirror = HolographVMRegistry.getObjectMirror(snapshot, id, listener);
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
                engine.evaluate(expression, thisContext, evalThread, evaluationListener, DebugEvent.EVALUATION, false);
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
