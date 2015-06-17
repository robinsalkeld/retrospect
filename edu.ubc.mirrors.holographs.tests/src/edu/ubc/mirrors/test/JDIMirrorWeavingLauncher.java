package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;

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
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.retrospect.MirrorWorld;

public class JDIMirrorWeavingLauncher {
    
    private static String GUARD_ASPECTS_PATH = "/Users/robinsalkeld/Documents/UBC/Code/Retrospect/Retroactive Aspect Guards/bin";
    
    public static String launch(String mainClassName, String options, File aspectPath, File hologramClassPath) throws Exception {
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
        
        URL urlPath = aspectPath.toURI().toURL();
        
        File guardAspects = new File(GUARD_ASPECTS_PATH);
        URL guardAspectsPath = guardAspects.toURI().toURL();
        
        if (hologramClassPath != null) {
	        System.out.println("Booting up holographic VM...");
	        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(jdiVMM, hologramClassPath,
	                Collections.singletonMap("/", "/"));
	        vmh.setSystemOut(teedOut);
	        vmh.setSystemErr(teedErr);
	        
	        vm = vmh;
	        thread = (ThreadMirror)vmh.getWrappedMirror(thread);
	        
                vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
                vmh.addBootstrapPathURL(urlPath);               
                vmh.addBootstrapPathURL(guardAspectsPath);
        }
        
        final VirtualMachineMirror finalVM = vm;
        final ThreadMirror finalThread = thread;
        
        MirrorWorld world = new MirrorWorld(finalThread, null);
        world.weave();
        
        vm.resume();
        MethodMirrorExitRequest endRequest = vm.eventRequestManager().createMethodMirrorExitRequest();
        endRequest.setMethodFilter(mainClassName, "main", Collections.singletonList("java.lang.String[]"));
        endRequest.enable();
        vm.dispatch().runUntil(endRequest);
        
//        return mergedOutput.toString();
        return Reflection.withThread(thread, new Callable<String>() {
            @Override
            public String call() throws Exception {
                ClassMirror guardAspect = finalVM.findBootstrapClassMirror("edu.ubc.aspects.JDKAroundFieldSets");
                ObjectMirror newOut = guardAspect.getStaticFieldValues().get(guardAspect.getDeclaredField("newStderrBaos"));
                String output = Reflection.toString(newOut, finalThread);
                System.out.print(output);
                return output;
            }
        });
    }
}
