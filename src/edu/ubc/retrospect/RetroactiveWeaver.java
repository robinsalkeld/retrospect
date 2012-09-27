package edu.ubc.retrospect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect) throws ClassNotFoundException {
        VirtualMachineMirror vm = aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)vm.getThreads().get(0);
        threadHolograph.enterHologramExecution();
        
        ClassMirrorLoader loader = aspect.getLoader();
        AspectJMirrors mirrors = new AspectJMirrors(loader);
        mirrors.getAspectMirror(aspect);
        mirrors.resolve();
        
        threadHolograph.exitHologramExecution();
    }
}
