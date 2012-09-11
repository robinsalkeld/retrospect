package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class DebuggingTest {

    public static void main(String[] args) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, IllegalConnectorArgumentsException, InterruptedException {
        VirtualMachineHolograph vm = new VirtualMachineHolograph(JDIVirtualMachineMirror.connectOnPort(8998),
                Collections.<URL>emptyList(),
                Collections.singletonMap("/", "/"));
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        URL urlPath = binDir.toURI().toURL();
        ThreadMirror thread = vm.getThreads().get(0);
        ClassMirrorLoader loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {urlPath});
        ClassMirror klass = Reflection.classMirrorForName(vm, "tracing.version3.TraceMyClasses", true, loader);
//        for (Method method : klass.getMethods()) {
//            for (Annotation a : method.getAnnotations()) {
//                System.out.println(a);
//            }
//        }
    }
    
    
}
