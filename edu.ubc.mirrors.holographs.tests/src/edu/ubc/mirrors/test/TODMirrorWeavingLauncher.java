package edu.ubc.mirrors.test;

import java.io.File;
import java.net.URL;

import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.tod.TODVirtualMachineMirror;

public class TODMirrorWeavingLauncher {
    public static String launch(String clientName, String aspectPath, File hologramClassPath) throws Exception {
        final TODVirtualMachineMirror todVMM = TODVirtualMachineMirror.connect(clientName);
        ThreadMirror thread = null;
        for (ThreadMirror t : todVMM.getThreads()) {
            if (Reflection.getThreadName(t).equals("main")) {
                thread = t;
                break;
            }
        }
        
        return RetroactiveWeaving.weave(todVMM, thread, aspectPath, hologramClassPath, null);
    }
}
