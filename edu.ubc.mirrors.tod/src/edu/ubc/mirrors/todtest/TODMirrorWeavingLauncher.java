package edu.ubc.mirrors.todtest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.test.EvalConstants;
import edu.ubc.mirrors.test.ProcessUtils;
import edu.ubc.mirrors.test.RetroactiveWeaving;
import edu.ubc.mirrors.tod.TODVirtualMachineMirror;
import edu.ubc.util.Stopwatch;

public class TODMirrorWeavingLauncher {
    
    public static String recordAndWeave(String mainClassName, List<String> programArgs, List<String> vmArgs, 
            String aspectPath, File hologramClassPath) throws Exception {
        System.out.println("Starting GridMaster process");
        Process dbgridProcess = launchGridMasterProcess();
        try {
            ProcessUtils.handleStreams(dbgridProcess, System.out, System.err);
            
            // Wait for the DB process to become ready
            // TODO: Scan for 
            Thread.sleep(10000);
            try {
                int dbgridResult = dbgridProcess.exitValue();
                throw new RuntimeException("TOD database process returned non-zero exit code: " + dbgridResult);
            } catch (IllegalThreadStateException e) {
                // Expected
            }
            
            System.out.println("Recording base program");
            Stopwatch s = new Stopwatch();
            
            String clientName = "tod-" + mainClassName;
            List<String> vmArgsWithTOD = new ArrayList<String>(vmArgs);
            vmArgsWithTOD.addAll(EvalConstants.todVMArgs(clientName));
            List<String> env = Arrays.asList("DYLD_LIBRARY_PATH=" + EvalConstants.BoostDynLibPath);
            
            s.start();
            Process baseProgram = ProcessUtils.launchJava(mainClassName, programArgs, vmArgsWithTOD, env);
            ProcessUtils.handleStreams(baseProgram, System.out, System.err);
            
            int baseResult = baseProgram.waitFor();
            if (baseResult != 0) {
                throw new RuntimeException("Base program returned non-zero exit code: " + baseResult);
            }
            long recordTime = s.lap();
            
            System.out.println("Weaving aspects");
            s.lap();
            String result = launch(clientName, aspectPath, hologramClassPath);
            long weaveTime = s.lap();
            System.out.println("Record: " + recordTime / 1000.0 + "s, weave: " + weaveTime / 1000.0 + "s");
            return result;
        } finally {
            System.out.println("Destroying GridMaster process");
            dbgridProcess.destroy();
        }
    }
    
    public static Process launchGridMasterProcess() throws IOException {
        return ProcessUtils.launchJava("tod.impl.dbgrid.GridMaster", Arrays.asList("0"), 
                Arrays.asList("-cp", Reflection.join(EvalConstants.TODGridMasterClasspathEntries, ":")),
                Collections.<String>emptyList());
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
        
        return new RetroactiveWeaving().weave(todVMM, thread, aspectPath, hologramClassPath, null);
    }
}
