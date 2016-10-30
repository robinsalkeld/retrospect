package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Assert;

import edu.ubc.mirrors.raw.NativeClassMirror;

public class EvaluationCase {

    private static final String expectedTracingOutput;
    static {
        try {
            expectedTracingOutput = new String(NativeClassMirror.readFully(
                    EvaluationCase.class.getResourceAsStream("expected-tracing-test-combined-output.txt")), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static final EvaluationCase TRACING = new EvaluationCase("TracingAspectTest",
            "tracing.ExampleMain",
            EvalConstants.TracingExampleBin.toString(), 
            EvalConstants.TracingAspectsBin.toString(),
            expectedTracingOutput);
    
    public static final EvaluationCase HEAP = new EvaluationCase("HeapAspectTest",
          "tracing.ExampleMain",
          EvalConstants.TracingExampleBin.toString(),
          EvalConstants.DJProfClasses + ":" + EvalConstants.DJProfClassesHeap,
          Pattern.compile(Pattern.quote("Bytes Allocated | Bytes Allocated | overall | name")));
    
    public static final EvaluationCase CONTRACT = new EvaluationCase("ContractValidationAspectTest",
          "edu.ubc.mirrors.test.MyCloseable",
          EvalConstants.EvalTestsBin.toString(), 
          EvalConstants.ContractValidationAspectBin.toString(),
          null);

    public static final EvaluationCase LEAK_DETECTION = new EvaluationCase("LeakDetectorAspectTest",
          "edu.ubc.mirrors.test.LeakSample",
          EvalConstants.TestsRoot.toString(), 
          EvalConstants.LeakDetectorAspectBin.toString(),
          null);

    public static final EvaluationCase RACER = new EvaluationCase("RacerTest",
          "Task",
          EvalConstants.RacerExampleBin.toString(), 
          EvalConstants.RacerBin.toString(),
          null);
    
    private final String name;
    private final String mainClass;
    private final String programPath;
    private final String aspectPath;
    private final Object expectedOutput;
    
    public EvaluationCase(String name, String mainClass, String programPath, String aspectPath, Object expectedOutput) {
        this.name = name;
        this.mainClass = mainClass;
        this.programPath = programPath;
        this.aspectPath = aspectPath;
        this.expectedOutput = expectedOutput;
    }
    
    public String getName() {
        return name;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public String getProgramPath() {
        return programPath;
    }
    
    public String getAspectPath() {
        return aspectPath;
    }
    
    public Object getExpectedOutput() {
        return expectedOutput;
    }
    
    public void verifyOutput(String output) {
        if (expectedOutput instanceof String) {
            Assert.assertEquals((String)expectedOutput, output);
        } else if (expectedOutput instanceof Pattern) {
            Assert.assertTrue(((Pattern)expectedOutput).matcher(output).matches());
        }
    }
}
