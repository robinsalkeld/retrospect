package edu.ubc.mirrors.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.sun.jdi.ArrayType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class LiveToStringer {
    public static void main(String[] args) throws Exception {
        final VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ReferenceType objectType = jdiVM.classesByName(Object.class.getName()).get(0);
        Method toStringMethod = objectType.methodsByName("toString").get(0);
        
        ThreadReference thread = pauseAndGetThread(jdiVM);
//        ThreadReference thread = getMainThread(jdiVM);
        
        final JDIVirtualMachineMirror vm = new JDIVirtualMachineMirror(jdiVM);
        ThreadMirror threadMirror = (ThreadMirror)vm.makeMirror(thread);
//      final VirtualMachineHolograph holographicVM = new VirtualMachineHolograph(vm, Reflection.getStandardMappedFiles());
//        ThreadHolograph threadHolograph = (ThreadHolograph)holographicVM.getWrappedMirror(threadMirror);
//        Reflection.withThread(threadHolograph, new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                ClassMirror barClass = vm.findAllClasses(Bar.class.getName(), false).get(0);
//                ObjectMirror bar = barClass.getInstances().get(0);
//                System.out.println(bar.identityHashCode());
//                return null;
//            }
//        });
//        
//        if (true) return;
        
        ToStringer.toStringAllTheObjects(vm, threadMirror);
    }
    
    private static ThreadReference getMainThread(final VirtualMachine jdiVM) throws InterruptedException {
        for (ThreadReference t : jdiVM.allThreads()) {
            if (t.name().equals("main")) {
                return t;
            }
        }
        throw new IllegalArgumentException("No main thread?");
    }
    
    private static ThreadReference pauseAndGetThread(final VirtualMachine jdiVM) throws InterruptedException {
        MethodEntryRequest mer = jdiVM.eventRequestManager().createMethodEntryRequest();
        mer.enable();
        
        final EventSet es = jdiVM.eventQueue().remove();
        mer.disable();
        MethodEntryEvent mee = (MethodEntryEvent)es.eventIterator().next();
        ThreadReference thread = mee.thread();
        
        System.out.println("Thread: " + thread.name());
        
        // Spawn a worker thread to work through the remaining events
        Thread worker = new Thread() {
            public void run() {
                for (;;) {
                    EventSet es2;
                    try {
                        es2 = jdiVM.eventQueue().remove();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (es2 == null) break;
                    es2.resume();
                }
            }
            
            @Override
            protected void finalize() throws Throwable {
                es.resume();
            }
        };
        worker.setDaemon(true);
        worker.start();
        
        return thread;
    }
}
