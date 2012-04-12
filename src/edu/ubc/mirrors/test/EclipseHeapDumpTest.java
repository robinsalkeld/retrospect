package edu.ubc.mirrors.test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class EclipseHeapDumpTest implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        IClass iClass = snapshot.getClassesByName(Bundle.class.getName(), true).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(iClass.getClassLoaderId());
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        vm.addNativeBytecodeLoaders(classLoader, Bundle.class.getClassLoader());
        HeapDumpClassMirrorLoader heapDumpLoader = new HeapDumpClassMirrorLoader(vm, classLoader);
        
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        ClassLoader nativeLoader = EclipseHeapDumpTest.class.getClassLoader();
        ClassMirrorLoader nativeMirrorLoader = new NativeClassMirrorLoader(nativeLoader);
        ClassMirrorLoader extendedLoader = Reflection.makeChainedClassLoaderMirror(heapDumpLoader, nativeMirrorLoader);

        // Create a mutable layer on the object model.
        MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
        MutableClassMirrorLoader mutableLoader = new MutableClassMirrorLoader(mutableVM, extendedLoader);
        
        ClassMirror bundleClass = Reflection.loadClassMirror(mutableVM, mutableLoader, Bundle.class.getName());
        ClassMirror printerClass = Reflection.loadClassMirror(mutableVM, mutableLoader, PrintOSGiBundles.class.getName());
        
        // For each class instance (in this case we only expect one)...
        List<InstanceMirror> bundles = bundleClass.getInstances();
        for (InstanceMirror bundle : bundles) {
            // Invoke PrintOSGiBundles#print reflectively.
            Object result = Reflection.invoke(printerClass, "print", bundle);
            System.out.println(result);
        }
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
