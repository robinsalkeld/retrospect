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
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class EclipseHeapDumpTest implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        // Open memory snapshot and find the BundleRepository class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        VirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        IClass iClass = snapshot.getClassesByName(Bundle.class.getName(), true).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(iClass.getClassLoaderId());
        
        // Create an instance of the mirrors API backed by the snapshot
        ClassMirrorLoader bytecodeLoader = new NativeClassMirrorLoader(Bundle.class.getClassLoader());
        HeapDumpClassMirrorLoader heapDumpLoader = new HeapDumpClassMirrorLoader(vm, bytecodeLoader, classLoader);
        HeapDumpClassMirror klass = new HeapDumpClassMirror(heapDumpLoader, iClass);
        
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        ClassLoader nativeLoader = EclipseHeapDumpTest.class.getClassLoader();
        ClassMirrorLoader nativeMirrorLoader = new NativeClassMirrorLoader(nativeLoader);
        
        ClassMirrorLoader extendedLoader = Reflection.makeChainedClassLoaderMirror(heapDumpLoader, nativeMirrorLoader);

        // Create a mutable layer on the object model.
        MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
        MutableClassMirrorLoader mutableLoader = new MutableClassMirrorLoader(mutableVM, extendedLoader);
        
        // Create a MirageClassLoader to load the transformed code.
        MirageClassLoader mirageLoader = new MirageClassLoader(vm, mutableLoader, System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        Class<?> mirageClass = mirageLoader.loadMirageClass(PrintOSGiBundles.class.getName());
        
        // For each class instance (in this case we only expect one)...
        List<ObjectMirror> instances = klass.getInstances();
        for (ObjectMirror mirror : instances) {
            // Create a mutable layer as above.
            mirror = mutableVM.makeMirror(mirror); 
            
            // Invoke PrintOSGiBundles#print reflectively.
            Object o = mirageLoader.makeMirage(mirror);
            Object result = Reflection.invoke(mirageClass, "print", o);
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
