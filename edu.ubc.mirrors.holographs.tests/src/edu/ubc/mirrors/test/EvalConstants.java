package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class EvalConstants {

    // Equinox-specific logic to get the root of a class' bundle
    public static File getBundleRoot(Class<?> classInBundle) {
        Bundle bundle = FrameworkUtil.getBundle(classInBundle);
        try {
            return FileLocator.getBundleFile(bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File TestsRoot = getBundleRoot(EvalConstants.class);
    public static File TestsBin = new File(TestsRoot, "bin");
    
    public static File Root = TestsRoot.getParentFile();
    public static File EvalRoot = new File(Root, "../RetrospectEval");
    public static File DataRoot = new File(Root, "../RetrospectData");
    
    public static File SpecJVMRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/SPECjvm2008");
    public static File SpecJVMJar = new File(SpecJVMRoot, "SPECjvm2008.jar");
    public static File SpecJVMLib = new File(SpecJVMRoot, "lib");
    
    public static File LeapRoot = new File(EvalRoot, "Leap");
    public static File LeapTransformerJar = new File(LeapRoot, "leap-transformer-0.2.jar");
    public static File LeapRecorderJar = new File(LeapRoot, "leap-recorder-0.2.jar");
    public static File LeapReplayerJar = new File(LeapRoot, "leap-replayer-0.2.jar");
    
    public static File LeapExampleRoot = new File(EvalRoot, "Leap Example");
    public static File LeapExampleBin = new File(LeapExampleRoot, "bin");
    
    public static File EvalTestsRoot = new File(EvalRoot, "Test");
    public static File EvalTestsBin = new File(EvalTestsRoot, "bin");
    
    public static File TracingExampleRoot = new File(EvalRoot, "Tracing Example");
    public static File TracingExampleBin = new File(TracingExampleRoot, "bin");
    
    public static File TracingAspectsRoot = new File(EvalRoot, "Tracing Example Aspects");
    public static File TracingAspectsBin = new File(TracingAspectsRoot, "bin");
    
    public static File TracingSpecJVMAspectsRoot = new File(EvalRoot, "SPECjvm2008 Tracing Aspects");
    public static File TracingSpecJVMAspectsBin = new File(TracingAspectsRoot, "bin");
    
    public static File RacerExampleRoot = new File(EvalRoot, "Racer Test");
    public static File RacerExampleBin = new File(RacerExampleRoot, "bin");
    
    public static File RacerRoot = new File(EvalRoot, "RacerAJ");
    public static File RacerBin = new File(RacerRoot, "bin");
    
    public static File LeakDetectorAspectBin = new File(new File(EvalRoot, "Leak Detection Aspect"), "bin");
    
    public static File ContractValidationAspectBin = new File(new File(EvalRoot, "Contract Validation"), "bin");
    
    public static File DJProf = new File(EvalRoot, "djprof-v1.0.2");
    public static File DJProfLib = new File(DJProf, "lib");
    public static File DJProfMainJar = new File(DJProfLib, "djprof.jar");
    public static File DJProfHeapAspectJar = new File(DJProfLib, "heap.jar");
    public static File DJProfClasses = new File(DJProf, "classes");
    public static File DJProfClassesHeap = new File(DJProfClasses, "profilers/heap");
    
    public static File GuardAspectsBin = new File(Root, "Retroactive Aspect Guards/bin");
    
    public static Map<String, String> casestudyAspectPaths = new HashMap<String, String>();
    static {
        casestudyAspectPaths.put("tracing", TracingExampleBin + ":" + TracingSpecJVMAspectsBin);
    }
    
    public static File TODGitRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/tod/git");
    
    public static File TODRoot = new File(TODGitRoot, "tod");
    public static File TODdbgrid = new File(TODRoot, "dbgrid");
    public static File TODdbgridBin = new File(TODdbgrid, "bin");
    public static File TODagent = new File(TODRoot, "agent");
    public static File TODagentBin = new File(TODagent, "bin");
    public static File TODcore = new File(TODRoot, "core");
    public static File TODcoreBin = new File(TODcore, "bin");
    public static File TODcoreLib = new File(TODcore, "lib");
    public static File TODevdbng = new File(TODRoot, "evdbng");
    public static File TODevdbngBin = new File(TODevdbng, "bin");
    
    public static File zzutils = new File(TODGitRoot, "zz.utils");
    public static File zzutilsBin = new File(zzutils, "bin");
    
    public static File BoostDynLibPath = new File("/Users/robinsalkeld/Documents/UBC/Code/boost_1_58_0_libs/lib");
    
    public static List<String> todVMArgs(String clientName) {
        return Arrays.asList("-Xbootclasspath/p:" + TODagentBin,
                             "-noverify",
                             "-agentpath:" + TODagent + "/libtod-agent15.dylib",
                             "-Dcollector-host=localhost",
                             "-Dcollector-port=8058",
                             "-Dclient-name=" + clientName,
                             "-Dagent-cache-path=/Users/robinsalkeld/tmp/tod",
                             "-Dagent-verbose=1",
                             "-Dcapture-at-start=true");
    }
    
    public static List<File> TODGridMasterClasspathEntries = 
            Arrays.asList(TODdbgridBin,
                          TODagentBin,
                          TODcoreBin,
                          TODevdbngBin,
                          zzutilsBin,
                          new File(TODcoreLib, "asm-all-3.2-svn.jar"),
                          ProcessUtils.aspectRuntimeJar);
}
