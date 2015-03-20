package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Collections;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
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
        EventSet es = q.remove();
        // Ignore the VMStartEvent
        if (es.size() == 1 && es.iterator().next() instanceof VMStartEvent) {
            es.resume();
            es = q.remove();
        }
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
