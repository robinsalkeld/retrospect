package edu.ubc.retrospect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.retrospect.AspectJMirrors.AspectMirror;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, InterruptedException {
        VirtualMachineHolograph vm = (VirtualMachineHolograph)aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
        threadHolograph.enterHologramExecution();
        try {
            ThreadHolograph.raiseMetalevel();
            ClassMirrorLoader loader = aspect.getLoader();
            AspectJMirrors mirrors = new AspectJMirrors(vm, loader, thread);
            AspectMirror aspectMirror = mirrors.getAspectMirror(aspect);
            mirrors.resolve();
            
            aspectMirror.installRequests();
            ThreadHolograph.lowerMetalevel();
            vm.dispatch().start();
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
}

