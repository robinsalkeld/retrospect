package edu.ubc.retrospect;

import java.io.File;
import java.util.zip.ZipFile;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.retrospect.AspectJMirrors.AspectMirror;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, InterruptedException {
        VirtualMachineHolograph vm = (VirtualMachineHolograph)aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
        threadHolograph.enterHologramExecution();
        
        ClassMirrorLoader loader = aspect.getLoader();
        AspectJMirrors mirrors = new AspectJMirrors(loader, thread);
        AspectMirror aspectMirror = mirrors.getAspectMirror(aspect);
        mirrors.resolve();
        
        aspectMirror.installRequests();
        
        vm.dispatch().start();
        
        threadHolograph.exitHologramExecution();
    }
}

