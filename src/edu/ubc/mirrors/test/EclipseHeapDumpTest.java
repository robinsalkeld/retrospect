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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class EclipseHeapDumpTest implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        IClass iClass = snapshot.getClassesByName(Bundle.class.getName(), true).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(iClass.getClassLoaderId());
        vm.addNativeBytecodeLoaders(classLoader, Bundle.class.getClassLoader());
        
        // Create a mutable layer on the object model.
        MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
        
        // Create a holograph VM
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(mutableVM);
        
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        ClassMirror bundleRepositoryClass = holographVM.findAllClasses(BundleRepository.class.getName()).get(0);
        ClassMirror printerClass = Reflection.injectBytecode(bundleRepositoryClass.getLoader(), 
                new NativeClassMirror(PrintOSGiBundles.class));
        
        // For each class instance (in this case we only expect one)...
        MethodMirror method = printerClass.getMethod("print", bundleRepositoryClass);
        for (InstanceMirror bundleRepository : bundleRepositoryClass.getInstances()) {
            // Invoke PrintOSGiBundles#print reflectively.
            Object result = method.invoke(null, bundleRepository);
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
