package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.IHeapObjectArgument;
import org.eclipse.mat.util.IProgressListener;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.mirages.Reflection;

public class ExpressionQuery implements IQuery {
    @Argument
    public ISnapshot snapshot;

    @Argument(flag = Argument.UNFLAGGED)
    public IHeapObjectArgument objects;

    @Argument(isMandatory = true)
    public String expression = "toString()";
    
    private List<Value> results;
            
    public static class Value {
        
        private ObjectMirror mirror;
        private final Semaphore semaphore = new Semaphore(0);
        
        public void setMirror(ObjectMirror mirror) {
            this.mirror = mirror;
            semaphore.release();
        }
        
        public void waitForResult() throws InterruptedException {
            semaphore.acquire();
        }
        
        public String getString() {
            return HolographVMRegistry.toString(mirror);
        }
    }
    
    public IResult execute(IProgressListener listener) throws Exception
    {
//          InspectionAssert.heapFormatIsNot(snapshot, "DTFJ-PHD"); //$NON-NLS-1$
//          listener.subTask(Messages.HashEntriesQuery_Msg_Extracting);

            listener.subTask("Creating holographic VM");
        
            VirtualMachineMirror vm = HolographVMRegistry.getHolographVM(snapshot);
            listener.subTask("Creating evaluation engine");
            MirrorsVirtualMachine jdiVM = new MirrorsVirtualMachine(vm);
            
            final IEvaluationEngine engine = HolographVMRegistry.getEvaluationEngine(vm);
            JDIDebugTarget debugTarget = (JDIDebugTarget)engine.getDebugTarget();
            
            ThreadMirror threadMirror = HolographVMRegistry.getThreadForEval(vm);
            ThreadReference jdiThread = (ThreadReference)jdiVM.wrapMirror(threadMirror);
            final IJavaThread evalThread = debugTarget.findThread(jdiThread);
            
            listener.subTask("Collecting objects");
            List<IObject> input = new ArrayList<IObject>();
            for (int[] ids : objects)
            {
                    for (int id : ids)
                    {
                            IObject obj = snapshot.getObject(id);
                            input.add(obj);
                            
                            if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
                    }
            }

            results = new ArrayList<Value>(input.size());
            
            listener.subTask("Evaluating expression");
            int count = 0;
            for (IObject obj : input) {
                ObjectMirror mirror = HolographVMRegistry.getMirror(obj);
                ObjectReference jdiObject = jdiVM.wrapMirror(mirror);
                final IJavaObject thisContext = (IJavaObject)JDIValue.createValue(debugTarget, jdiObject);
                
                final Value value = new Value();
                results.add(value);
                
                final IEvaluationListener thisListener = new IEvaluationListener() {
                    public void evaluationComplete(IEvaluationResult result) {
                        ObjectMirror resultMirror = null;
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
                                        ObjectReference jdiValue = ((JDIObjectValue)resultValue).getUnderlyingObject();
                                        if (jdiValue != null) {
                                            resultMirror = (ObjectMirror)((MirrorsObjectReference)jdiValue).getWrapped();
                                        }
                                    }

                                    
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        value.setMirror(resultMirror);
                    }

                };
                
                engine.evaluate(expression, thisContext, evalThread, thisListener, DebugEvent.EVALUATION, false);
                
                // Need to synchronize since you can't evaluate multiple expressions against
                // a single ThreadMirror.
                value.waitForResult();
                
                if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
                
                count++;
            }
            
            listener.done();

            return new org.eclipse.mat.query.results.ListResult(Value.class, results, "string");
    }
}
