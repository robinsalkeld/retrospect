package edu.ubc.mirrors.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import edu.ubc.mirrors.mirages.Stopwatch;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class ToStringer implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        String samplePath = args[1];
        
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        Method method = Package.class.getDeclaredMethod("getSystemPackage0", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null, "java.lang.instrument");
        
        
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
        
//        int id2 = snapshot.mapAddressToId(0x7e332dfd8l);
//        IObject obj2 = snapshot.getObject(id2);
//        ObjectMirror mirror2 = vm.makeMirror(obj2);
//        ObjectMirror holograph2 = holographVM.getWrappedMirror(mirror2);
//        
//        System.out.println("***");
//        System.out.println(Reflection.toString(holograph2
//                .getClassMirror()));
//        System.out.println(Reflection.toString(holograph2));
//
//        System.exit(0);
        
        
        int numObjects = snapshot.getSnapshotInfo().getNumberOfObjects();
        Stopwatch sw = new Stopwatch();
        sw.start();
//        int firstId = 37625 + 91225 + 82525;
//        int lastId = numObjects;
        int firstId = numObjects - 20000;
        int lastId = firstId + 2000;
        int count = 0;
        int charCount = 0;
        List<String> timesOver500ms = new ArrayList<String>();
        long maxTime = 0;
        float maxTimePerChar = 0;
        int errors = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(samplePath))));
        String line;
        List<Integer> sample = new ArrayList<Integer>();
        while ((line = in.readLine()) != null) {
            sample.add(Integer.parseInt(line));
        }
        try {
            for (int id : sample.subList(0, 15000)) {
                IObject obj = snapshot.getObject(id);
                ObjectMirror mirror = vm.makeMirror(obj);
                if (mirror != null) {
                    ObjectMirror holograph = holographVM.getWrappedMirror(mirror);
                    try {
                        Stopwatch perObjectSW = new Stopwatch();
                        perObjectSW.start();
                        String s = Reflection.toString(holograph);
                        long time = perObjectSW.stop();
                        
                        if (time > 500) {
                            timesOver500ms.add("" + Long.toHexString(obj.getObjectAddress()) + ": " + time);
                        }
                        maxTime = Math.max(maxTime, time);
                        
                        charCount += s.length();
                        float timePerChar = ((float)time) / charCount;
                        maxTimePerChar = Math.max(maxTimePerChar, timePerChar);
                        
                        count++;
                        if (count % 25 == 0) {
                            System.out.println(count + ": " + s);
                        }
                    } catch (Exception e) {
                        errors++;
                        System.out.println("Error on object #" + id + " ("
                                + Long.toHexString(obj.getObjectAddress())
                                + ")");
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            long time = sw.stop();
            System.out.println("Num objects toString-ed: " + count);
            System.out.println("Errors: " + errors);
            if (count != 0) {
                System.out.println("Total time: " + time);
                System.out.println("Average time per object: " + ((float)time) / count);
                System.out.println("Max time per object: " + maxTime);
                System.out.println("Objects taking more than 1/2 second: " + timesOver500ms);
                System.out.println("Average string length: " + ((float)charCount) / count);
                System.out.println("Average time per character: " + ((float)time) / charCount);
                System.out.println("Max time per character: " + maxTimePerChar);
            }
            MirageClassLoader.printStats();
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
