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
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.retrospect.MirrorWeaver;

public class TracingExampleTest {

    public static void main(String[] args) throws Exception {
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(
        	"tracing.ExampleMain", 
        	"-cp \"/Users/robinsalkeld/Documents/UBC/Code/Tracing Example/bin\"",
        	true, true);
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
        
//        MonitorContendedEnteredRequest mwr = jdiVM.eventRequestManager().createMonitorContendedEnteredRequest();
//        mwr.addClassFilter("tracing.Square");
//        mwr.enable();
        
        final JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM, threadRef);
        ThreadMirror thread = (ThreadMirror)jdiVMM.makeMirror(threadRef);
        
//        new EventDispatch(jdiVMM).start();
//        if (true) return;
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        URL urlPath = binDir.toURI().toURL();
        URL aspectRuntimeJar = new URL("jar:file:///Users/robinsalkeld/Documents/workspace/org.aspectj.runtime/aspectjrt.jar!/");
        
        
        // TODO-RS: Cheating for now...
        File cachePath = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/eclipse_for_osgi_dump/java_pid2675.0001.subeclipseonjava7_hologram_classes");
        VirtualMachineHolograph vmh = new VirtualMachineHolograph(jdiVMM, cachePath, Collections.singletonMap("/", "/"));
	thread = (ThreadMirror)vmh.getWrappedMirror(thread);
	
	final VirtualMachineMirror vm = vmh;
        final ThreadMirror finalThread = thread;
	
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath, aspectRuntimeJar});
	ClassMirror traceClass = Reflection.classMirrorForName(vm, thread, "tracing.version3.Trace", true, loader);
	traceClass.getStaticFieldValues().setInt(traceClass.getDeclaredField("TRACELEVEL"), 2);
	
	ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
        InstanceMirror stream = (InstanceMirror)systemClass.getStaticFieldValues().get(systemClass.getDeclaredField("err"));
	MethodMirror method = traceClass.getDeclaredMethod("initStream", PrintStream.class.getName());
        method.invoke(thread, null, stream);
	
        Reflection.withThread(thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ClassMirror aspect = Reflection.classMirrorForName(vm, finalThread, "tracing.version3.TraceMyClasses", true, loader);
                MirrorWeaver weaver = new MirrorWeaver(vm, loader, finalThread);
                weaver.weave(aspect);
                
                vm.dispatch().start();
                return null;
            }
        });
	
    }
    
    
}
