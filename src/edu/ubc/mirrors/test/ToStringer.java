package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.ConsoleProgressListener;

import sun.misc.Unsafe;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpInstanceMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class ToStringer implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
//        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
//        unsafeField.setAccessible(true);
//        Unsafe unsafe = (Unsafe)unsafeField.get(null);
//        byte[] bytes = { 1, 2, 3, 4 };
//        System.out.println(unsafe.getInt(bytes, (long)16));
//        ByteBuffer buffer = ByteBuffer.allocate(4);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        buffer.put(0, bytes[0]);
//        buffer.put(1, bytes[1]);
//        buffer.put(2, bytes[2]);
//        buffer.put(3, bytes[3]);     
//        System.out.println(buffer.getInt(0));
        
     // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
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
        
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, 
                Reflection.getBootstrapPath(),
                mappedFiles);
        
        int numObjects = snapshot.getSnapshotInfo().getNumberOfObjects();
        System.out.println("numObjects: " + numObjects);
        for (int id = 0; id < numObjects; id++) {
            IObject obj = snapshot.getObject(id);
            ObjectMirror mirror = vm.makeMirror(obj);
            if (mirror instanceof InstanceMirror) {
                InstanceMirror holograph = (InstanceMirror)holographVM.getWrappedMirror(mirror);
                try {
                    System.out.println("***");
                    System.out.println(Reflection.toString(holograph.getClassMirror()));
                    System.out.println(Reflection.toString(holograph));
                } catch (Exception e) {
                    System.out.println("Error on object #" + id + " (" + Long.toHexString(obj.getObjectAddress())  + ")");
//                    throw e;
                }
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
