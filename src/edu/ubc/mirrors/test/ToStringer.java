package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.jruby.Ruby;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeOnlyVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class ToStringer implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        String snapshotPath = args[0];
        
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
     // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("org.eclipse.equinox.launcher.Main$StartupClassLoader".replace('.', '/') + ".class");
        int b = stream.read();
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a mutable layer on the object model.
        MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
        
        // Create a holograph VM
        Map<String, String> mappedFiles = Reflection.getStandardMappedFiles();
//        String launchFolder = "/Users/robinsalkeld/Documents/workspace/.metadata/.plugins/org.eclipse.pde.core/Eclipse + holograph connector (java 7)";
//        mappedFiles.put(launchFolder, launchFolder);
//        String jrubyBuildLib = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/build_lib";
//        mappedFiles.put(jrubyBuildLib, jrubyBuildLib);
//        String workspace = "/Users/robinsalkeld/Documents/workspace";
//        mappedFiles.put(workspace, workspace);
//        String jrubyJar = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/dist/jruby-complete-1.6.4.jar";
//        mappedFiles.put(jrubyJar, jrubyJar);
        String javaExtDir = "/System/Library/Java/Extensions";
        mappedFiles.put(javaExtDir, javaExtDir);
        
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(mutableVM, 
                Reflection.getBootstrapPath(),
                mappedFiles);
        
        for (ClassMirror classLoaderClass : holographVM.findAllClasses(HashMap.class.getName(), true)) {
            for (InstanceMirror classLoaderInstance : classLoaderClass.getInstances()) {
                System.out.println(Reflection.toString(classLoaderInstance));
            }
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
