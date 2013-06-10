package edu.ubc.mirrors.test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.VoidProgressListener;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class LiveVersusDeadToStringEvaluation implements IApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(args[0]);
        
        final VirtualMachine jdiVM = JDIVirtualMachineMirror.connectOnPort(port);
        System.out.println("Connected.");
        
        EventSet eventSet = pauseAndGetThread(jdiVM);
        LocatableEvent event = (LocatableEvent)eventSet.iterator().next();
        ThreadReference thread = event.thread();
         
        final JDIVirtualMachineMirror liveVM = new JDIVirtualMachineMirror(jdiVM);
        
        ThreadMirror threadMirror = (ThreadMirror)liveVM.makeMirror(thread);
//        final VirtualMachineHolograph holograpOnLiveVM = new VirtualMachineHolograph(liveVM, Reflection.getStandardMappedFiles());
//        final ThreadHolograph threadHolograph = (ThreadHolograph)holograpOnLiveVM.getWrappedMirror(threadMirror);
//        Reflection.withThread(threadHolograph, new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
                ToStringer.toStringAllTheObjects(liveVM, threadMirror);
//                return null;
//            }
//        });
        
        int pid = getPID(jdiVM, thread);

        eventSet.resume();
        File snapshotPath = HeapDumper.dumpHeap(pid, new VoidProgressListener());
        
        // Open memory snapshot
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                snapshotPath, 
                Collections.<String, String>emptyMap(), 
                new VoidProgressListener());
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror heapDumpVM = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
        Map<String, String> mappedFiles = Collections.singletonMap("/", "/");
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(heapDumpVM, mappedFiles);

        holographVM.prepare();
        
        ToStringer.toStringAllTheObjects(holographVM, holographVM.getThreads().get(0));
    }
    
    public static ThreadReference getMainThread(final VirtualMachine jdiVM) throws InterruptedException {
        for (ThreadReference t : jdiVM.allThreads()) {
            if (t.name().equals("main")) {
                return t;
            }
        }
        throw new IllegalArgumentException("No main thread?");
    }
    
    private static EventSet pauseAndGetThread(final VirtualMachine jdiVM) throws InterruptedException {
        MethodEntryRequest mer = jdiVM.eventRequestManager().createMethodEntryRequest();
        mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        mer.enable();
        
        final EventSet es = jdiVM.eventQueue().remove();
        mer.disable();
        
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
        
        return es;
    }
    
    public static int getPID(VirtualMachine vm, ThreadReference thread) throws Exception {
        ReferenceType classType = vm.classesByName(Class.class.getName()).get(0);
        ClassObjectReference mfObjRef = (ClassObjectReference)classType.classObject().invokeMethod(
                thread, 
                classType.methodsByName("forName").get(0), 
                Collections.singletonList(vm.mirrorOf(ManagementFactory.class.getName())), 
                ObjectReference.INVOKE_SINGLE_THREADED);
        ClassType mfType = (ClassType)mfObjRef.reflectedType();
        
        ObjectReference bean = (ObjectReference)mfType.invokeMethod(thread, mfType.methodsByName("getRuntimeMXBean").get(0), Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        ReferenceType beanType = vm.classesByName(RuntimeMXBean.class.getName()).get(0);
        StringReference nameRef = (StringReference)bean.invokeMethod(thread, beanType.methodsByName("getName").get(0), Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        String name = nameRef.value();
        return Integer.parseInt(name.substring(0, name.indexOf("@")));
    }
    

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
