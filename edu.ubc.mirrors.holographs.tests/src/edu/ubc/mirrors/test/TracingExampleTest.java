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
package edu.ubc.mirrors.test;

import junit.framework.TestCase;

import edu.ubc.mirrors.raw.NativeClassMirror;


public class TracingExampleTest extends TestCase {

    public void testTracingAspect() throws Exception {
        String combinedOutput = JDIMirrorWeavingLauncher.launch("tracing.ExampleMain", 
                "-cp \"/Users/robinsalkeld/Documents/UBC/Code/Tracing Example/bin\"", 
                "/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin", 
                "/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/jdi/TracingExampleTest/hologram_classes");
        
        String expectedOutput = new String(NativeClassMirror.readFully(getClass().getResourceAsStream("expected-tracing-test-output.txt")), "UTF-8");
        assertEquals(expectedOutput, combinedOutput);
    
//	traceClass.getStaticFieldValues().setInt(traceClass.getDeclaredField("TRACELEVEL"), 2);
//	
//	ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
//        InstanceMirror stream = (InstanceMirror)systemClass.getStaticFieldValues().get(systemClass.getDeclaredField("err"));
//	MethodMirror method = traceClass.getDeclaredMethod("initStream", PrintStream.class.getName());
//        method.invoke(thread, null, stream);
    }
}
