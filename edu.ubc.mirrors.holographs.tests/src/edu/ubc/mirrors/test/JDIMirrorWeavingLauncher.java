package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class JDIMirrorWeavingLauncher {
    
    public static String launch(String mainClassName, String options, String aspectPath, File hologramClassPath) throws Exception {
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        OutputStream teedOut = new TeeOutputStream(mergedOutput, System.out);
        OutputStream teedErr = new TeeOutputStream(mergedOutput, System.err);
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(mainClassName, options, true, teedOut, teedErr);
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
        
        // Make sure the mechanisms for loading bytecode are primed so we don't hit
        // tons of side-effects later.
        ClassMirror mainClass = jdiVMM.makeClassMirror(cpe.referenceType());
        ClassMirrorLoader classLoader = mainClass.getLoader();
        ClassMirror loaderClass = jdiVMM.findBootstrapClassMirror("java.lang.ClassLoader");
        MethodMirror getBootstrapResourceMethod = loaderClass.getDeclaredMethod("getBootstrapResource", "java.lang.String");
        getBootstrapResourceMethod.invoke(thread, classLoader, jdiVMM.makeString("foo"));
        
        return RetroactiveWeaving.weave(vm, thread, aspectPath, hologramClassPath, mergedOutput);
    }
}
