package edu.ubc.mirrors.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EvalConstants {

    public static File Root = new File("/Users/robinsalkeld/Documents/UBC/Code/Retrospect");
    public static File EvalRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectEval");
    public static File DataRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData");
    
    public static File SpecJVMRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/SPECjvm2008");
    public static File SpecJVMJar = new File(SpecJVMRoot, "SPECjvm2008.jar");
    public static File SpecJVMLib = new File(SpecJVMRoot, "lib");
    
    public static File TestsRoot = new File(Root, "edu.ubc.mirrors.holographs.tests/bin");
    
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
    
}
