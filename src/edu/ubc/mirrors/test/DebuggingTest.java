package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadStartRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.retrospect.RetroactiveWeaver;

public class DebuggingTest {

    public static void main(String[] args) throws Exception {
        VirtualMachine jdiVM = JDIVirtualMachineMirror.commandLineLaunch(
        	"tracing.ExampleMain", 
        	"-cp \"/Users/robinsalkeld/Documents/UBC/Code/Tracing Example/bin\"");
//        VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ThreadStartRequest r = jdiVM.eventRequestManager().createThreadStartRequest();
        r.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        r.enable();
        jdiVM.resume();
        EventQueue q = jdiVM.eventQueue();
        // Ignore the VMStartEvent
        q.remove();
        EventSet es = q.remove();
        ThreadStartEvent tse = (ThreadStartEvent)es.eventIterator().next();
        final ThreadReference threadRef = tse.thread();
        
        JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM);
	final VirtualMachineHolograph vm = new VirtualMachineHolograph(jdiVMM,
                Reflection.getBootstrapPath(),
                Collections.singletonMap("/", "/"));
        final ThreadMirror thread = (ThreadMirror)vm.getWrappedMirror(jdiVMM.makeMirror(threadRef));
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        URL urlPath = binDir.toURI().toURL();
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath});
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
        	ClassMirror traceClass = Reflection.classMirrorForName(vm, thread, "tracing.version1.Trace", true, loader);
        	traceClass.getStaticField("TRACELEVEL").setInt(2);
        	
        	InstanceMirror baos = vm.findBootstrapClassMirror(ByteArrayOutputStream.class.getName())
        		.getConstructor().newInstance(thread);
        	InstanceMirror stream = vm.findBootstrapClassMirror(PrintStream.class.getName())
        		.getConstructor(vm.findBootstrapClassMirror(OutputStream.class.getName())).newInstance(thread, baos);
        	MethodMirror method = traceClass.getMethod("initStream", vm.findBootstrapClassMirror(PrintStream.class.getName()));
                method.invoke(thread, null, stream);
        	
        	ClassMirror aspect = Reflection.classMirrorForName(vm, thread, "tracing.version1.TraceMyClasses", true, loader);
                RetroactiveWeaver.weave(aspect, thread);
                
                System.out.println(Reflection.toString(baos));
                
                return null;
            }
        });
    }
    
    
}
