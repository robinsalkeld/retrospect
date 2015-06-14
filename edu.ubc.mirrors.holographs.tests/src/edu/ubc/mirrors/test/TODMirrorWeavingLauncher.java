package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
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
    public static String launch(String clientName, File aspectPath, File hologramCachePath) throws Exception {
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
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        OutputStream teedOut = new TeeOutputStream(mergedOutput, System.out);
        OutputStream teedErr = new TeeOutputStream(mergedOutput, System.err);
        
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(todVMM, hologramCachePath,
                Collections.singletonMap("/", "/"));
        vmh.setSystemOut(teedOut);
        vmh.setSystemErr(teedErr);
        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
        
        vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
        vmh.addBootstrapPathURL(EvalConstants.GuardAspectsBin.toURI().toURL());
        vmh.addBootstrapPathURL(urlPath);
        
        MirrorWorld world = new MirrorWorld(thread, null);
        world.weave();
        
//        todVMM.eventRequestManager().dumpEvents();
        
        vmh.resume();
        vmh.dispatch().run();
        
        return mergedOutput.toString();
//        ClassMirror guardAspect = vmh.findBootstrapClassMirror("edu.ubc.aspects.JDKAroundFieldSets");
//        ObjectMirror newOut = guardAspect.get(guardAspect.getDeclaredField("newStderrBaos"));
//        String output = Reflection.toString(newOut, thread);
//        System.out.print(output);
    }
}
