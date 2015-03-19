package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;

import org.aspectj.bridge.IMessageHandler;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.retrospect.AroundClosureMirror;
import edu.ubc.retrospect.MirrorWorld;

public class JDIMirrorWeavingLauncher {
    public static String launch(String mainClassName, String options, String aspectPath, String hologramClassPath) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(mainClassName, options, true, output, output);
//        VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ClassPrepareRequest r = jdiVM.eventRequestManager().createClassPrepareRequest();
        r.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        r.addClassFilter(mainClassName);
        r.enable();
        jdiVM.resume();
        EventQueue q = jdiVM.eventQueue();
        // Ignore the VMStartEvent
        q.remove();
        EventSet es = q.remove();
        ClassPrepareEvent cpe = (ClassPrepareEvent)es.eventIterator().next();
        final ThreadReference threadRef = cpe.thread();
        
        final JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM);
        ThreadMirror thread = (ThreadMirror)jdiVMM.makeMirror(threadRef);
        VirtualMachineMirror vm = jdiVMM; 
        
        File binDir = new File(aspectPath);
        URL urlPath = binDir.toURI().toURL();
        
        if (hologramClassPath != null) {
	        System.out.println("Booting up holographic VM...");
	        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(jdiVMM, new File(hologramClassPath),
	                Collections.singletonMap("/", "/"));
	        vmh.setSystemOut(output);
	        vmh.setSystemErr(output);
	        
	        vm = vmh;
	        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
        }
        
        final VirtualMachineMirror finalVM = vm;
        final ThreadMirror finalThread = thread;
        
        MirrorWorld world = new MirrorWorld(finalVM, finalThread, urlPath);
        world.weave();
        
        return output.toString();
    }
}
