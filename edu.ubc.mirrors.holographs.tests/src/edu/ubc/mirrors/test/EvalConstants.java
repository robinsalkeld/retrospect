package edu.ubc.mirrors.test;

import java.io.File;

public class EvalConstants {

    public static File EvalRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectEval");
    public static File DataRoot = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData");
    
    public static File TracingExampleRoot = new File(EvalRoot, "Tracing Example");
    public static File TracingExampleBin = new File(TracingExampleRoot, "bin");
    
    public static File TracingAspectsRoot = new File(EvalRoot, "Tracing Example Aspects");
    public static File TracingAspectsBin = new File(TracingAspectsRoot, "bin");
    
    public static File RacerExampleRoot = new File(EvalRoot, "Racer Test");
    public static File RacerExampleBin = new File(RacerExampleRoot, "bin");
    
    public static File RacerRoot = new File(EvalRoot, "RacerAJ");
    public static File RacerBin = new File(RacerRoot, "bin");
    
    public static File LeakDetectorAspectBin = new File(new File(EvalRoot, "Leak Detection Aspect"), "bin");
    
}
