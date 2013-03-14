package edu.ubc.mirrors.test;

import java.util.Collections;
import java.util.List;

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

import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class LiveToStringer {
    public static void main(String[] args) throws Exception {
        final VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(7777);
        ReferenceType objectType = jdiVM.classesByName(Object.class.getName()).get(0);
        Method toStringMethod = objectType.methodsByName("toString").get(0);
        
        ThreadReference thread = pauseAndGetThread(jdiVM);
//        ThreadReference thread = getMainThread(jdiVM);
        
        
        List<ReferenceType> allClasses = jdiVM.allClasses();
        int count = 0;
        for (ReferenceType rt : allClasses) {
            if (rt instanceof ArrayType) {
                continue;
            }
            
            for (ObjectReference o : rt.instances(0)) {
                StringReference s = (StringReference)o.invokeMethod(thread, toStringMethod, Collections.<Value>emptyList(), ThreadReference.INVOKE_SINGLE_THREADED);
                
                count++;
                if (count % 25 == 0) {
                    System.out.println(count + ": " + s.value());
                }
            }
        }
        System.out.println("Total: " + count);
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
