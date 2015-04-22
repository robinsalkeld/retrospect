package edu.ubc.mirrors.test;

import java.io.File;
import java.net.URL;
import java.util.Collections;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.tod.TODVirtualMachineMirror;
import edu.ubc.retrospect.MirrorWorld;

public class TODMirrorWeavingLauncher {
    public static void launch(String clientName, String aspectPath, String hologramCachePath) throws Exception {
        final TODVirtualMachineMirror todVMM = TODVirtualMachineMirror.connect(clientName);
        ThreadMirror thread = null;
        for (ThreadMirror t : todVMM.getThreads()) {
            if (Reflection.getThreadName(t).equals("main")) {
                thread = t;
                break;
            }
        }
        VirtualMachineMirror vm = todVMM; 
        
        File binDir = new File(aspectPath);
        URL urlPath = binDir.toURI().toURL();
        
        System.out.println("Booting up holographic VM...");
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(todVMM, new File(hologramCachePath),
                Collections.singletonMap("/", "/"));
        vm = vmh;
        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
        
        vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
        vmh.addBootstrapPathURL(urlPath);
        
        MirrorWorld world = new MirrorWorld(thread, null);
        world.weave();
    }
}
