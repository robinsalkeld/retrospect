package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.ContextProvider;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResultTable;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.IHeapObjectArgument;
import org.eclipse.mat.util.IProgressListener;

import com.sun.jdi.ObjectReference;

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
    
    @Argument(isMandatory = false)
    public String threadID = "main";
    
    @Argument(isMandatory = true)
    public String projectName = "Tracing Example";
    
    private static final ILaunchConfigurationType remoteJavaLaunchType;
    static {
        ILaunchConfigurationType result = null;
        for (ILaunchConfigurationType type : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes()) {
            if (type.getIdentifier().equals("org.eclipse.jdt.launching.remoteJavaApplication")) {
                result = type;
                break;
            }
        }
        if (result == null) {
            throw new RuntimeException("Can't find remoteJavaApplication launch type");
        }
        remoteJavaLaunchType = result;
    }
    
    public static class Result implements IResultTable
    {
            private List<IObject> objects;
            private List<IObject> results;
            private final Semaphore semaphore;
            
            private Result(ISnapshot snapshot, List<IObject> objects)
            {
                    this.objects = objects;
                    this.results = new ArrayList<IObject>(Collections.<IObject>nCopies(objects.size(), null));
                    semaphore = new Semaphore(0);
            }

            public ResultMetaData getResultMetaData()
            {
                    return new ResultMetaData.Builder() //

                                    .addContext(new ContextProvider("Object") {
                                            public IContextObject getContext(Object row)
                                            {
                                                    return getObject(row);
                                            }
                                    }) //

                                    .addContext(new ContextProvider("Result") {
                                            public IContextObject getContext(Object row)
                                            {
                                                    return getResult(row);
                                            }
                                    }) //

                                    .build();
            }

            public Column[] getColumns()
            {
                    return new Column[] { new Column("Object").sorting(Column.SortDirection.ASC), //
                                    new Column("Result") };
            }

            public Object getColumnValue(Object row, int columnIndex)
            {
                    int rowIndex = (Integer)row;

                    switch (columnIndex)
                    {
                    case 0:
                            return Reflection.toString(HolographVMRegistry.getMirror(objects.get(rowIndex)));
                    case 1:
                            return Reflection.toString(HolographVMRegistry.getMirror(results.get(rowIndex)));
                    }

                    return null;
            }

            public int getRowCount()
            {
                    return objects.size();
            }

            public Object getRow(int rowId)
            {
                    return rowId;
            }

            @Override
            public IContextObject getContext(Object row) {
                return getResult(row);
            }
            
            private IContextObject getObject(final Object row)
            {
                            return new IContextObject() {
                                    public int getObjectId()
                                    {
                                        return objects.get((Integer)row).getObjectId();
                                    }
                            };
            }

            private IContextObject getResult(final Object row)
            {
                            return new IContextObject() {
                                    public int getObjectId()
                                    {
                                            return results.get((Integer)row).getObjectId();
                                    }
                            };
            }
            
            public void setResult(int index, IObject result) {
                results.set(index, result);
                semaphore.release();
            }
            
            public void waitForAllResults() throws InterruptedException {
                semaphore.acquire(objects.size());
            }
    }

    public Result execute(IProgressListener listener) throws Exception
    {
//          InspectionAssert.heapFormatIsNot(snapshot, "DTFJ-PHD"); //$NON-NLS-1$
//          listener.subTask(Messages.HashEntriesQuery_Msg_Extracting);

            List<IObject> input = new ArrayList<IObject>();
            
            VirtualMachineMirror vm = HolographVMRegistry.getHolographVM(snapshot);
            ThreadMirror thread = vm.getThreads().get(0);
            MirrorsVirtualMachine jdiVM = new MirrorsVirtualMachine(vm);
            
            ILaunchConfiguration launchConfig = remoteJavaLaunchType.newInstance(null, "dummy");
            ILaunch launch = new Launch(launchConfig, ILaunchManager.DEBUG_MODE, null);
            
            JDIDebugTarget debugTarget = MirrorsVirtualMachine.makeDebugTarget(thread, jdiVM, launch);
            
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            IJavaProject javaProject = JavaCore.create(project);
            
            IJavaThread evalThread = null;
            for (IThread t : debugTarget.getThreads()) {
                if (t.getName().equals(threadID)) {
                    evalThread = (IJavaThread)t;
                    break;
                }
            }
            if (evalThread == null) {
                throw new IllegalStateException("Thread nameds '" + threadID + "' not found.");
            }
            
            IEvaluationEngine engine = debugTarget.getEvaluationEngine(javaProject);
            
            for (int[] ids : objects)
            {
                    for (int id : ids)
                    {
                            IObject obj = snapshot.getObject(id);
                            input.add(obj);
                            
                            if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
                    }
            }

            final Result evalResults = new Result(snapshot, input);
            
            int count = 0;
            for (IObject obj : input) {
                ObjectMirror mirror = HolographVMRegistry.getMirror(obj);
                ObjectReference jdiObject = jdiVM.wrapMirror(mirror);
                IJavaObject thisContext = (IJavaObject)JDIValue.createValue(debugTarget, jdiObject);
                
                final int index = count;
                IEvaluationListener thisListener = new IEvaluationListener() {
                    public void evaluationComplete(IEvaluationResult result) {
                        if (!result.isTerminated()) {
                            if (result.hasErrors()) {
                                if (result.getException() != null) {
                                    result.getException().printStackTrace();
                                }
                            } else {
                                IValue resultValue = result.getValue();
                                
                                ObjectReference jdiValue = ((JDIObjectValue)resultValue).getUnderlyingObject();
                                ObjectMirror resultMirror = (ObjectMirror)((MirrorsObjectReference)jdiValue).getWrapped();
                                IObject heapDumpObject = HolographVMRegistry.fromMirror(resultMirror);
                                
                                evalResults.setResult(index, heapDumpObject);
                            }
                        }
                    }
                };
                
                engine.evaluate(expression, thisContext, evalThread, thisListener, DebugEvent.EVALUATION, false);
                
                if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
                
                count++;
            }
            
            evalResults.waitForAllResults();
            
            listener.done();

            return evalResults;
    }
}
