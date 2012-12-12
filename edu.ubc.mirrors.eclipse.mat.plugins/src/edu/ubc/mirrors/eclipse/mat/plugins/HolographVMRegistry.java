package edu.ubc.mirrors.eclipse.mat.plugins;

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

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.wrapping.WrappingMirror;

public class HolographVMRegistry {

    private static final Map<ISnapshot, VirtualMachineHolograph> vms = new HashMap<ISnapshot, VirtualMachineHolograph>();
    private static final Map<VirtualMachineMirror, IEvaluationEngine> evalEngines = new HashMap<VirtualMachineMirror, IEvaluationEngine>();
    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
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
            
            // TODO-RS: proper configuration
            Map<String, String> mappedFiles = new HashMap<String, String>(Collections.singletonMap("/", "/"));
            
            // TODO-RS: Haxxors to avoid handling "." correctly.
            String jrubyJar = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/lib/jruby.jar";
            mappedFiles.put("./jruby-1.6.4/lib/jruby.jar", jrubyJar);
            
            vm = new VirtualMachineHolograph(hdvm, mappedFiles);
            
            vms.put(snapshot, vm);
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
    
}
