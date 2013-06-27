package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingMirror;

public class HolographVMRegistry {

    private static final Map<ISnapshot, VirtualMachineHolograph> vms = new HashMap<ISnapshot, VirtualMachineHolograph>();
    private static final Map<VirtualMachineMirror, IEvaluationEngine> evalEngines = new HashMap<VirtualMachineMirror, IEvaluationEngine>();
    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
    private static final Map<VirtualMachineMirror, ThreadMirror> threadsForEval = new HashMap<VirtualMachineMirror, ThreadMirror>();
    private static final Map<VirtualMachineMirror, ThreadMirror> secondaryThreadsForEval = new HashMap<VirtualMachineMirror, ThreadMirror>();
    
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
    
    public static void prepareHolographVM(final VirtualMachineHolograph vm, final IProgressListener listener) {
        ThreadMirror thread = vm.getThreads().get(0);
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
                List<ClassMirror> allClasses = vm.findAllClasses();
                listener.beginTask("Preparing Holographic VM", allClasses.size());
                for (ClassMirror klass : allClasses) {
                    listener.subTask("Preparing " + klass.getClassName());
                    vm.prepareClass(klass);
                    listener.worked(1);
                }
                listener.done();
                return null;
            }
        });
    }
    
    public static VirtualMachineHolograph getHolographVM(ISnapshot snapshot, final IProgressListener listener) {
        VirtualMachineHolograph vm = vms.get(snapshot);
        if (vm == null) {
            listener.beginTask("Creating Holographic VM", IProgressListener.UNKNOWN_TOTAL_WORK);
            vm = VirtualMachineHolograph.fromSnapshotWithIniFile(snapshot);
            vms.put(snapshot, vm);
            listener.done();
            
//            prepareHolographVM(vm, listener);
            
            ThreadMirror match = null;
            ThreadMirror secondary = null;
            for (ThreadMirror t : vm.getThreads()) {
                if (Reflection.getThreadName(t).equals("main")) {
                    match = t;
                } else {
                    secondary = t;
                }
                
                if (match != null && secondary != null) {
                    break;
                }
            }
            if (match == null) {
                throw new IllegalStateException("Thread named 'main' not found.");
            }
            setThreadsForEval(vm, match, secondary);
        }
        
        return vm;
    }
    
    public static void dispose(ISnapshot snapshot) {
        vms.remove(snapshot);
        evalEngines.remove(snapshot);
        threadsForEval.remove(snapshot);
        
        // TODO-RS: Be more precise
        mirrors.clear();
    }
    
    public static IEvaluationEngine getEvaluationEngine(VirtualMachineMirror vm) throws CoreException {
        IEvaluationEngine result = evalEngines.get(vm);
        if (result != null) {
            return result;
        }
        
        MirrorsVirtualMachine jdiVM = new MirrorsVirtualMachine(vm);
        
        ILaunchConfiguration launchConfig = remoteJavaLaunchType.newInstance(null, "dummy");
        ILaunch launch = new Launch(launchConfig, ILaunchManager.DEBUG_MODE, null);
        
        JDIDebugTarget debugTarget = MirrorsVirtualMachine.makeDebugTarget(getThreadForEval(vm), jdiVM, launch);
        
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];
        IJavaProject javaProject = JavaCore.create(project);
        
        result = debugTarget.getEvaluationEngine(javaProject);
        evalEngines.put(vm, result);
        
        return result;
    }
    
    public static ObjectMirror getMirror(IObject object, IProgressListener listener) {
        if (object == null) {
            return null;
        }
        
        ObjectMirror mirror = mirrors.get(object);
        if (mirror == null) {
            VirtualMachineHolograph vm = getHolographVM(object.getSnapshot(), listener);
            HeapDumpVirtualMachineMirror hdvm = (HeapDumpVirtualMachineMirror)vm.getWrappedVM();
            mirror = vm.getWrappedMirror(hdvm.makeMirror(object));
            mirrors.put(object, mirror);
        }
        return mirror;
    }

    public static IObject fromMirror(ObjectMirror element) {
        return ((HeapDumpObjectMirror)((WrappingMirror)element).getWrapped()).getHeapDumpObject();
    }
    
    public static void setThreadsForEval(VirtualMachineMirror vm, ThreadMirror thread, ThreadMirror secondary) {
        threadsForEval.put(vm, thread);
        secondaryThreadsForEval.put(vm, secondary);
    }
    
    public static ThreadMirror getThreadForEval(VirtualMachineMirror vm) {
        return threadsForEval.get(vm);
    }
    
    public static ThreadMirror getSecondaryThreadForEval(VirtualMachineMirror vm) {
        return secondaryThreadsForEval.get(vm);
    }
    
    public static String toString(ObjectMirror mirror) {
        if (mirror == null) {
            return "null";
        }
        try {
            return Reflection.toString(mirror, getThreadForEval(mirror.getClassMirror().getVM()));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
}

