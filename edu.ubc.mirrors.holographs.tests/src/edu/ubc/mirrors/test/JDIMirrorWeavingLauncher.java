package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class JDIMirrorWeavingLauncher {
    
    public static String launch(String mainClassName, String programArgs, String classPath, 
            String aspectPath, File hologramClassPath) throws Exception {
        
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        OutputStream teedOut = new TeeOutputStream(mergedOutput, System.out);
        OutputStream teedErr = new TeeOutputStream(mergedOutput, System.err);
        
        VirtualMachine jdiVM = JDIUtils.commandLineLaunch(mainClassName + " " + programArgs, 
                classPath, true, teedOut, teedErr);
        ClassPrepareEvent cpe = JDIUtils.waitForMainClassLoad(jdiVM, mainClassName);
        final ThreadReference threadRef = cpe.thread();
        
        final JDIVirtualMachineMirror jdiVMM = new JDIVirtualMachineMirror(jdiVM);
        ThreadMirror thread = (ThreadMirror)jdiVMM.makeMirror(threadRef);
        VirtualMachineMirror vm = jdiVMM; 
        
        // Make sure the mechanisms for loading bytecode are primed so we don't hit
        // tons of side-effects later.
//        ClassMirror mainClass = jdiVMM.makeClassMirror(cpe.referenceType());
//        ClassMirrorLoader classLoader = mainClass.getLoader();
//        ClassMirror loaderClass = jdiVMM.findBootstrapClassMirror("java.lang.ClassLoader");
//        MethodMirror getBootstrapResourceMethod = loaderClass.getDeclaredMethod("getBootstrapResource", "java.lang.String");
//        getBootstrapResourceMethod.invoke(thread, classLoader, jdiVMM.makeString("foo"));
        
        return new RetroactiveWeaving().weave(vm, thread, aspectPath, hologramClassPath, mergedOutput);
    }
}
