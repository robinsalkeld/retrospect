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
    public static void launch(String clientName, String aspectPath) throws Exception {
        final TODVirtualMachineMirror todVMM = TODVirtualMachineMirror.connect(clientName);
        ThreadMirror thread = null;
        for (ThreadMirror t : todVMM.getThreads()) {
            if (Reflection.getThreadName(t).equals("main")) {
                thread = t;
            }
        }
        VirtualMachineMirror vm = todVMM; 
        
        File binDir = new File(aspectPath);
        URL urlPath = binDir.toURI().toURL();
        URL aspectRuntimeJar = new URL("jar:file:///Users/robinsalkeld/Documents/workspace/org.aspectj.runtime/aspectjrt.jar!/");
        
        System.out.println("Booting up holographic VM...");
//        // TODO-RS: Cheating for now...
        File cachePath = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/jdi/RacerTest/hologram_classes");
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(todVMM, cachePath,
                Collections.singletonMap("/", "/"));
        vm = vmh;
        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
        
        final VirtualMachineMirror finalVM = vm;
        final ThreadMirror finalThread = thread;
        
        System.out.println("Creating class loader for aspects...");
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, finalThread, null, new URL[] {urlPath, aspectRuntimeJar});
        
        MirrorWorld world = new MirrorWorld(finalVM, loader, finalThread);
        world.weave();
        
        vm.dispatch().start();
    }
}
