package edu.ubc.retrospect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.retrospect.AspectJMirrors.AspectMirror;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, InterruptedException {
        VirtualMachineMirror vm = aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
        threadHolograph.enterHologramExecution();
        
        ClassMirrorLoader loader = aspect.getLoader();
        AspectJMirrors mirrors = new AspectJMirrors(loader, thread);
        AspectMirror aspectMirror = mirrors.getAspectMirror(aspect);
        mirrors.resolve();
        
        MethodMirrorEntryRequest entryRequest = vm.eventRequestManager().createMethodMirrorEntryRequest();
        entryRequest.enable();
        MethodMirrorExitRequest exitRequest = vm.eventRequestManager().createMethodMirrorExitRequest();
        exitRequest.enable();
        
        vm.resume();
        
        // TODO: Put this logic on separate threads and set it up before resuming instead.
        MirrorEventQueue q = vm.eventQueue();
        MirrorEventSet eventSet = q.remove();
        while (eventSet != null) {
            aspectMirror.executeAdvice(eventSet);
            eventSet.resume();
            
            eventSet = q.remove();
        }
        
        threadHolograph.exitHologramExecution();
    }
    
    
    
}

