package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIThreadMirror;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.retrospect.RetroactiveWeaver;

public class DebuggingTest {

    public static void main(String[] args) throws Exception {
        final VirtualMachineHolograph vm = new VirtualMachineHolograph(JDIVirtualMachineMirror.connectOnPort(8998),
                Collections.<URL>emptyList(),
                Collections.singletonMap("/", "/"));
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        URL urlPath = binDir.toURI().toURL();
        final ThreadMirror thread = vm.getThreads().get(0);
        final ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath});
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
                ClassMirror klass = Reflection.classMirrorForName(vm, thread, "tracing.version3.TraceMyClasses", true, loader);
                // Need to suspend thread first
//                RetroactiveWeaver.weave(klass);
                return null;
            }
        });
    }
    
    
}
