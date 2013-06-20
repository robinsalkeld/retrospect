package edu.ubc.mirrors.eclipse.mat.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import edu.ubc.mirrors.MirrorInvocationTargetException;
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
    
    public static VirtualMachineHolograph getHolographVM(ISnapshot snapshot) {
        VirtualMachineHolograph vm = vms.get(snapshot);
        if (vm == null) {
            HeapDumpVirtualMachineMirror hdvm = new HeapDumpVirtualMachineMirror(snapshot);
            
            String snapshotPath = snapshot.getSnapshotInfo().getPath();
            int lastDot = snapshotPath.lastIndexOf('.');
            File holographicFSConfigPath = new File(snapshotPath.substring(0, lastDot) + "_hfs.ini");
            Map<String, String> mappedFiles = new HashMap<String, String>();
            try {
                if (holographicFSConfigPath.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(holographicFSConfigPath));
                    String line;
                    while ((line = br.readLine()) != null) {
                        int equalsIndex = line.indexOf('=');
                        String key, value;
                        if (equalsIndex < 0) {
                            key = line;
                            value = line;
                        } else {
                            key = line.substring(0, equalsIndex);
                            value = line.substring(equalsIndex + 1);
                        }
                        mappedFiles.put(key, value);
                    }
                    br.close();
                } else {
                    holographicFSConfigPath.createNewFile();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                
            vm = new VirtualMachineHolograph(hdvm, mappedFiles);
            
            vms.put(snapshot, vm);
            
            ThreadMirror match = null;
            for (ThreadMirror t : vm.getThreads()) {
                if (Reflection.getThreadName(t).equals("main")) {
                    match = t;
                    break;
                }
            }
            if (match == null) {
                throw new IllegalStateException("Thread named 'main' not found.");
            }
            setThreadForEval(vm, match);
        }
        
        return vm;
    }
    
    public static IEvaluationEngine getEvaluationEngine(VirtualMachineMirror vm) throws CoreException {
        IEvaluationEngine result = evalEngines.get(vm);
        if (result != null) {
            return result;
        }
        
        MirrorsVirtualMachine jdiVM = new MirrorsVirtualMachine(vm);
        
        ILaunchConfiguration launchConfig = remoteJavaLaunchType.newInstance(null, "dummy");
        ILaunch launch = new Launch(launchConfig, ILaunchManager.DEBUG_MODE, null);
        
        ThreadMirror thread = vm.getThreads().get(0);
        JDIDebugTarget debugTarget = MirrorsVirtualMachine.makeDebugTarget(thread, jdiVM, launch);
        
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];
        IJavaProject javaProject = JavaCore.create(project);
        
        result = debugTarget.getEvaluationEngine(javaProject);
        evalEngines.put(vm, result);
        
        return result;
    }
    
    public static ObjectMirror getMirror(IObject object) {
        if (object == null) {
            return null;
        }
        
        ObjectMirror mirror = mirrors.get(object);
        if (mirror == null) {
            VirtualMachineHolograph vm = getHolographVM(object.getSnapshot());
            HeapDumpVirtualMachineMirror hdvm = (HeapDumpVirtualMachineMirror)vm.getWrappedVM();
            mirror = vm.getWrappedMirror(hdvm.makeMirror(object));
            mirrors.put(object, mirror);
        }
        return mirror;
    }

    public static IObject fromMirror(ObjectMirror element) {
        return ((HeapDumpObjectMirror)((WrappingMirror)element).getWrapped()).getHeapDumpObject();
    }
    
    public static void setThreadForEval(VirtualMachineMirror vm, ThreadMirror thread) {
        threadsForEval.put(vm, thread);
    }
    
    public static ThreadMirror getThreadForEval(VirtualMachineMirror vm) {
        return threadsForEval.get(vm);
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

