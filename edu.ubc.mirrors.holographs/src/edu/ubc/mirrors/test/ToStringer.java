package edu.ubc.mirrors.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mirages.Stopwatch;

public class ToStringer implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
//        Map<String, String> mappedFiles = Reflection.getStandardMappedFiles();
////        String launchFolder = "/Users/robinsalkeld/Documents/workspace/.metadata/.plugins/org.eclipse.pde.core/Eclipse + holograph connector (java 7)";
////        mappedFiles.put(launchFolder, launchFolder);
////        String jrubyBuildLib = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/build_lib";
////        mappedFiles.put(jrubyBuildLib, jrubyBuildLib);
////        String workspace = "/Users/robinsalkeld/Documents/workspace";
////        mappedFiles.put(workspace, workspace);
////        String jrubyJar = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/dist/jruby-complete-1.6.4.jar";
////        mappedFiles.put(jrubyJar, jrubyJar);
//        String javaExtDir = "/System/Library/Java/Extensions";
//        mappedFiles.put(javaExtDir, javaExtDir);
        
        Map<String, String> mappedFiles = Collections.singletonMap("/", "/");
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, mappedFiles);
        
        holographVM.prepare();
        
        toStringAllTheObjects(holographVM, holographVM.getThreads().get(0));
    }

    public static List<Integer> readSample(String samplePath) throws NumberFormatException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(samplePath))));
        String line;
        List<Integer> sample = new ArrayList<Integer>();
        while ((line = in.readLine()) != null) {
            sample.add(Integer.parseInt(line));
        }
        return sample;
    }
    
    private static void countClasses(VirtualMachineMirror vm, ThreadMirror thread) {
        int total = 0;
        int classCount = 0;
        for (ClassMirror klass : vm.findAllClasses()) {
            if (!hasNonDefaultToString(klass, thread)) {
                continue;
            }
            classCount++;
            total += klass.getInstances().size();
            System.out.print(".");
            if (classCount % 40 == 0) {
                System.out.println(classCount);
            }
        }
        System.out.println();
        System.out.println("Total classes: " + classCount);
        System.out.println("Total objects: " + total);
    }
    
    // toString() ALL the objects!!!
    public static void toStringAllTheObjects(VirtualMachineMirror vm, ThreadMirror thread) {
//        ClassMirror barClass = vm.findAllClasses(Bar.class.getName(), false).get(0);
//        ObjectMirror bar = barClass.getInstances().get(0);
//        System.out.println(bar.identityHashCode());
//        if (true) return;
        
        int count = 0;
        int charCount = 0;
        List<String> timesOver500ms = new ArrayList<String>();
        long maxTime = 0;
        float maxTimePerChar = 0;
        int errors = 0;
        
        countClasses(vm, thread);
        
        Stopwatch sw = new Stopwatch();
        sw.start();
        try {
            for (ClassMirror klass : vm.findAllClasses()) {
                if (!hasNonDefaultToString(klass, thread)) {
                    continue;
                }
                
                for (ObjectMirror object : klass.getInstances()) {
                    try {
                        Stopwatch perObjectSW = new Stopwatch();
                        perObjectSW.start();
                        String s = Reflection.toString(object, thread);
                        long time = perObjectSW.stop();
                        
                        if (time > 500) {
                            timesOver500ms.add("" + object + "(" + time + "): " + s);
                        }
                        maxTime = Math.max(maxTime, time);
                        
                        charCount += s.length();
                        float timePerChar = ((float)time) / charCount;
                        maxTimePerChar = Math.max(maxTimePerChar, timePerChar);
                        
                        count++;
                        if (count % 25 == 0) {
                            System.out.print(".");
                        }
                        if (count % 1000 == 0) {
                            System.out.println(count);
                        }
                    } catch (Throwable e) {
                        errors++;
                        System.out.println("Error on object " + object);
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            long time = sw.stop();
            System.out.println();
            System.out.println("Num objects toString-ed: " + count);
            System.out.println("Errors: " + errors);
            if (count != 0) {
                System.out.println("Total time: " + time);
                System.out.println("Average time per object: " + ((float)time) / count);
                System.out.println("Max time per object: " + maxTime);
                System.out.println("Objects taking more than 1/2 second ("+timesOver500ms.size()+"):");
//                for (String s : timesOver500ms) {
//                    System.out.println(s);
//                }
                System.out.println("Average string length: " + ((float)charCount) / count);
                System.out.println("Average time per character: " + ((float)time) / charCount);
                System.out.println("Max time per character: " + maxTimePerChar);
            }
            MirageClassLoader.printStats();
        }
    }
    
    private static boolean hasNonDefaultToString(final ClassMirror klass, ThreadMirror thread) {
        if (klass.isArray()) {
            return false;
        }
        return true;
//        ClassMirror thisClass = klass;
//        while (thisClass != null) {
//            try {
//                thisClass.getMethod("toString");
//                return true;
//            } catch (NoSuchMethodException e) {
//                thisClass = thisClass.getSuperClassMirror();
//            } catch (Throwable t) {
//                // This may trigger errors in holographic execution
//                t.printStackTrace();
//                return false;
//            }
//        }
//        return false;
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
