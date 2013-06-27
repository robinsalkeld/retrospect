package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class CDTBugTest implements IApplication {

    private static final String CPPASTName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName";
    
    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));

        final VirtualMachineHolograph holographVM = VirtualMachineHolograph.fromSnapshotWithIniFile(snapshot);
        
        Reflection.withThread(holographVM.getThreads().get(0), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (MirageClassLoader.debug) {
                    System.out.println("Finding target class...");
                }
        	ClassMirror nameClass = holographVM.findAllClasses(CPPASTName, false).get(0);
        	if (MirageClassLoader.debug) {
                    System.out.println("Finding target instance...");
                }
        	analyse(nameClass);
                return null;
            }
	});
        
    }
    
    public static void analyse(ClassMirror nameClass) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException, IOException {
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        VirtualMachineMirror vm = nameClass.getVM();
        // TODO-RS: Does this make sense?
        ThreadMirror thread = vm.getThreads().get(0);
        if (MirageClassLoader.debug) {
            System.out.println("Injecting bytecode...");
        }
        
        String analyzerClassName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameDuplicateAnalysis";
        String path = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/" +analyzerClassName.replace('.', '/') + ".class"; 
        FileInputStream fis = new FileInputStream(path);
        byte[] analyzerClassBytecode = NativeClassMirror.readFully(fis);
        fis.close();
        ClassMirror analyzerClass = Reflection.injectBytecode(vm, thread, nameClass.getLoader(), 
                analyzerClassName, analyzerClassBytecode);
        MethodMirror method = analyzerClass.getMethod("analyse", vm.findBootstrapClassMirror(Collection.class.getName()));
        
        if (MirageClassLoader.debug) {
            System.out.println("Building collection...");
        }
        
        final List<ObjectMirror> instances = nameClass.getInstances().subList(0, 50000);
        int count = instances.size();
        ClassMirror objectArrayClass = vm.getArrayClass(1, vm.findBootstrapClassMirror(Object.class.getName()));
        ClassMirror nameArrayClass = vm.getArrayClass(1, nameClass);
        ObjectArrayMirror array = new CalculatedObjectArrayMirror(nameArrayClass, count) {
            @Override
            public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
                return instances.get(index);
            }
        };
        InstanceMirror arrayList = (InstanceMirror)vm.findBootstrapClassMirror(Arrays.class.getName()).getMethod("asList", objectArrayClass).invoke(thread, null, array);
        
        if (MirageClassLoader.debug) {
            System.out.println("Invoking...");
        }
        method.invoke(thread, null, arrayList);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
