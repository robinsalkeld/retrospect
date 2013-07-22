package edu.ubc.mirrors.test;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
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
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.retrospect.RetroactiveWeaver;

public class DebuggingTest {

    private static class Thing {
        public Map<String, String> properties;
        public String toString() {
            return "Hi, I'm a Thing named 'Fred'";
        }
    }
    
    public static void main(String[] args) throws Exception {
        Thing thing = new Thing();
        thing.properties = new HashMap<String, String>();
        
        thing.properties.put("foo", "bar");
        thing.properties.put("qaz", "quz");
        thing.properties.put("arfle", "barfle");
        thing.properties.put("blip", "blop");
        thing.properties.put("something", "else");
        thing.properties.put("who", "knows");
        
        VirtualMachine jdiVM = JDIVirtualMachineMirror.commandLineLaunch(
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
	final VirtualMachineHolograph vm = new VirtualMachineHolograph(jdiVMM,
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
