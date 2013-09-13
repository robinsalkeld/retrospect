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

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.retrospect.RetroactiveWeaver;

public class DebuggingTest {

    public static void main(String[] args) throws Exception {
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(
        	"tracing.ExampleMain", 
        	"-cp \"/Users/robinsalkeld/Documents/UBC/Code/Tracing Example/bin\"",
        	true);
//        VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ClassPrepareRequest r = jdiVM.eventRequestManager().createClassPrepareRequest();
        r.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        r.addClassFilter("tracing.ExampleMain");
        r.enable();
        jdiVM.resume();
        EventQueue q = jdiVM.eventQueue();
        // Ignore the VMStartEvent
        q.remove();
        EventSet es = q.remove();
        ClassPrepareEvent cpe = (ClassPrepareEvent)es.eventIterator().next();
        final ThreadReference threadRef = cpe.thread();
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        URL urlPath = binDir.toURI().toURL();
        
        JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM);
        ClassMirror c = jdiVMM.findBootstrapClassMirror(ClassLoader.class.getName());
        List<ObjectMirror> i = c.getInstances();
        
	final VirtualMachineHolograph vm = new VirtualMachineHolograph(jdiVMM, null,
                Collections.singletonMap("/", "/"));
        final ThreadMirror thread = (ThreadMirror)vm.getWrappedMirror(jdiVMM.makeMirror(threadRef));
        
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath});
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
        	ClassMirror traceClass = Reflection.classMirrorForName(vm, thread, "tracing.version1.Trace", true, loader);
        	traceClass.getStaticFieldValues().setInt(traceClass.getDeclaredField("TRACELEVEL"), 2);
        	
        	ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
                InstanceMirror stream = (InstanceMirror)systemClass.getStaticFieldValues().get(systemClass.getDeclaredField("out"));
        	MethodMirror method = traceClass.getDeclaredMethod("initStream", vm.findBootstrapClassMirror(PrintStream.class.getName()));
                method.invoke(thread, null, stream);
        	
        	ClassMirror aspect = Reflection.classMirrorForName(vm, thread, "tracing.version1.TraceMyClasses", true, loader);
                RetroactiveWeaver.weave(aspect, thread);
                
                return null;
            }
        });
    }
    
    
}
