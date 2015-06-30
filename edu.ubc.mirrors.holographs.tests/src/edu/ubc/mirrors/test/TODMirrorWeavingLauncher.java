package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.tod.TODVirtualMachineMirror;

public class TODMirrorWeavingLauncher {
    
    public static String recordAndWeave(String mainClassName, List<String> programArgs, List<String> vmArgs, String aspectPath, File hologramClassPath) throws Exception {
//        Process dbgridProcess = ProcessUtils.launchJava("tod.impl.dbgrid.GridMaster", "0", "-cp " + EvalConstants.TODdbgridBin);
//        ProcessUtils.handleStreams(dbgridProcess, System.out, System.err);
        
        String clientName = "tod-" + mainClassName;
        List<String> vmArgsWithTOD = new ArrayList<String>(vmArgs);
        vmArgsWithTOD.addAll(EvalConstants.todVMArgs(clientName));
        List<String> env = Arrays.asList("DYLD_LIBRARY_PATH=" + EvalConstants.BoostDynLibPath);
        Process baseProgram = ProcessUtils.launchJava(mainClassName, programArgs, vmArgsWithTOD, env);
        ProcessUtils.handleStreams(baseProgram, System.out, System.err);
        int result = baseProgram.waitFor();
        if (result != 0) {
            throw new RuntimeException("Base program returned non-zero exit code: " + result);
        }
        
        return launch(clientName, aspectPath, hologramClassPath);
    }
    
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
