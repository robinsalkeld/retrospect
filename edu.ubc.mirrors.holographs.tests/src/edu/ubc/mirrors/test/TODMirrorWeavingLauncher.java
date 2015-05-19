package edu.ubc.mirrors.test;

import java.io.File;
import java.net.URL;
import java.util.Collections;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.tod.TODVirtualMachineMirror;
import edu.ubc.retrospect.MirrorWorld;

public class TODMirrorWeavingLauncher {
    public static void launch(String clientName, File aspectPath, File hologramCachePath) throws Exception {
        final TODVirtualMachineMirror todVMM = TODVirtualMachineMirror.connect(clientName);
        ThreadMirror thread = null;
        for (ThreadMirror t : todVMM.getThreads()) {
            if (Reflection.getThreadName(t).equals("main")) {
                thread = t;
                break;
            }
        }
        
        URL urlPath = aspectPath.toURI().toURL();
        
        System.out.println("Booting up holographic VM...");
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(todVMM, hologramCachePath,
                Collections.singletonMap("/", "/"));
        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
        
        vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
        vmh.addBootstrapPathURL(EvalConstants.GuardAspectsBin.toURI().toURL());
        vmh.addBootstrapPathURL(urlPath);
        
        MirrorWorld world = new MirrorWorld(thread, null);
        world.weave();
        
        vmh.resume();
        vmh.dispatch().run();
        
//        ClassMirror guardAspect = vmh.findBootstrapClassMirror("edu.ubc.aspects.JDKAroundFieldSets");
//        ObjectMirror newOut = guardAspect.get(guardAspect.getDeclaredField("newStdoutBaos"));
//        String output = Reflection.toString(newOut, thread);
//        System.out.print(output);
    }
}
