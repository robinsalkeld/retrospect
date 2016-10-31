package edu.ubc.mirrors.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.allOf;

import java.io.IOException;

import org.hamcrest.Matcher;
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
            equalTo(expectedTracingOutput));
    
    public static final EvaluationCase HEAP = new EvaluationCase("HeapAspectTest",
            "tracing.ExampleMain",
            EvalConstants.TracingExampleBin.toString(),
            EvalConstants.DJProfClasses + ":" + EvalConstants.DJProfClassesHeap,
            containsString("Bytes Allocated | Bytes Allocated | overall | name"));
    
    public static final EvaluationCase CONTRACT = new EvaluationCase("ContractValidationAspectTest",
            "edu.ubc.mirrors.test.MyCloseable",
            EvalConstants.EvalTestsBin.toString(), 
            EvalConstants.ContractValidationAspectBin.toString(),
            containsString("Unclosed closables: [edu.ubc.mirrors.test.MyCloseable"));

    public static final EvaluationCase LEAK_DETECTION = new EvaluationCase("LeakDetectorAspectTest",
            "edu.ubc.mirrors.test.LeakSample",
            EvalConstants.TestsBin.toString(), 
            EvalConstants.LeakDetectorAspectBin.toString(),
            allOf(containsString("   =>java.lang.String.<init>(String.java:602)"),
                  // If the holographic GC is not working correctly all 100 string instances
                  // in LeakSample.myVector will show up as leaks.
                  not(containsString("Number of occurrences: 100"))));
    
    public static final EvaluationCase RACER = new EvaluationCase("RacerTest",
            "Task",
            EvalConstants.RacerExampleBin.toString(), 
            EvalConstants.RacerBin.toString(),
            allOf(containsString("Field 'static int Task.shared' is accessed unprotected."),
                  not(containsString("Field 'static int Task.shared_protected' is accessed unprotected."))));
    
    private final String name;
    private final String mainClass;
    private final String programPath;
    private final String aspectPath;
    private final Matcher<String> expectedOutput;
    
    public EvaluationCase(String name, String mainClass, String programPath, String aspectPath, Matcher<String> expectedOutput) {
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
        Assert.assertThat(output, expectedOutput);
    }
}
