/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.todtest;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import edu.ubc.mirrors.test.EvalConstants;
import edu.ubc.mirrors.test.EvaluationCase;
import edu.ubc.mirrors.test.ProcessUtils;

public class TODTest extends TestCase implements IApplication {

    public static void main(String[] args) throws Exception {
        try {
            TODMirrorWeavingLauncher.launch("tod-ExampleMain", EvalConstants.TracingAspectsBin.toString(),
                    new File(EvalConstants.DataRoot, "/tod/TracingTest"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
    
    public void testCaseStudyEvaluation(EvaluationCase evalCase) throws Exception {
        List<String> vmArgs = Arrays.asList("-cp", evalCase.getProgramPath());
        List<String> programArgs = Collections.emptyList();
        List<String> env = Collections.emptyList();
        
        ProcessUtils.timeJava(evalCase.getMainClass(), programArgs, vmArgs, env);
        ProcessUtils.timeLoadTimeWeaving(evalCase.getMainClass(), evalCase.getProgramPath(), evalCase.getAspectPath());
        
        File todHologramClassesPath = new File(EvalConstants.DataRoot, "tod/" + evalCase.getName() + "/hologram_classes");
        String output = TODMirrorWeavingLauncher.recordAndWeave(evalCase.getMainClass(), programArgs, vmArgs, evalCase.getAspectPath(), todHologramClassesPath);
        evalCase.verifyOutput(output);
    }

    public void testTracingAspectTOD() throws Exception {
        testCaseStudyEvaluation(EvaluationCase.TRACING);
    }
    
    public void testHeapAspectTOD() throws Exception {
        testCaseStudyEvaluation(EvaluationCase.HEAP);
    }
}
