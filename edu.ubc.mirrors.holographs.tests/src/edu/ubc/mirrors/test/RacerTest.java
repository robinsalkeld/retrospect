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
import com.sun.jdi.event.MonitorContendedEnterEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.retrospect.RetroactiveWeaver;

public class RacerTest {

    public static void main(String[] args) throws Exception {
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(
        	"Task", 
        	"-cp \"/Users/robinsalkeld/Documents/UBC/Code/Racer Test/bin\"",
        	true);
//        VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ClassPrepareRequest r = jdiVM.eventRequestManager().createClassPrepareRequest();
        r.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        r.addClassFilter("Task");
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
        
        final JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM);
//        new EventDispatch(jdiVMM).start();
//        if (true) return;
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/RacerAJ/bin");
        URL urlPath = binDir.toURI().toURL();
        URL aspectRuntimeJar = new URL("jar:file:///Users/robinsalkeld/Documents/workspace/org.aspectj.runtime/aspectjrt.jar!/");
        
        // TODO-RS: Cheating for now...
        File cachePath = new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/eclipse_for_osgi_dump/java_pid2675.0001.subeclipseonjava7_hologram_classes");
	final VirtualMachineHolograph vm = new VirtualMachineHolograph(jdiVMM, cachePath,
                Collections.singletonMap("/", "/"));
        final ThreadMirror thread = (ThreadMirror)vm.getWrappedMirror(jdiVMM.makeMirror(threadRef));
        
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath, aspectRuntimeJar});
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
        	ClassMirror locking = Reflection.classMirrorForName(vm, thread, "ca.mcgill.sable.racer.Locking", true, loader);
                RetroactiveWeaver.weave(locking, thread);
                
                ClassMirror racer = Reflection.classMirrorForName(vm, thread, "ca.mcgill.sable.racer.Racer", true, loader);
                RetroactiveWeaver.weave(racer, thread);
                
                vm.dispatch().start();
                
                return null;
            }
        });
    }
    
    
}
