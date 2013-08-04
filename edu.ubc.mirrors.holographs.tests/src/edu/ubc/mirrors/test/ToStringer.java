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
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holograms.Stopwatch;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIClassMirror;
import edu.ubc.mirrors.jdi.JDIObjectMirror;

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
        in.close();
        return sample;
    }
    
    private static List<ObjectMirror> collectObjects(VirtualMachineMirror vm, ThreadMirror thread) {
        int total = 0;
        int classCount = 0;
        List<ObjectMirror> objects = new ArrayList<ObjectMirror>();
        for (ClassMirror klass : vm.findAllClasses()) {
            if (!hasNonDefaultToString(klass, thread)) {
                continue;
            }
            classCount++;
            List<ObjectMirror> instances = klass.getInstances();
            objects.addAll(instances);
            total += instances.size();
            System.out.print(".");
            if (classCount % 40 == 0) {
                System.out.println(classCount);
            }
        }
        System.out.println();
        System.out.println("Total classes: " + classCount);
        System.out.println("Total objects: " + total);
        return objects;
    }
    
    // toString() ALL the objects!!!
    public static void toStringAllTheObjects(VirtualMachineMirror vm, ThreadMirror thread) {
        boolean catchErrors = Boolean.getBoolean("edu.ubc.mirrors.holograms.catchErrors");
        
        int count = 0;
        int collected = 0;
        long minTime = 0;
        long maxTime = 0;
        
        // Using Welford's algorithm for online variance calculation
        // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
        float mean = 0;
        float delta = 0;
        float M2 = 0;
        
        int errors = 0;
        
        List<ObjectMirror> objects = collectObjects(vm, thread);
        try {
            for (ObjectMirror object : objects) {
                try {
                    if (object instanceof JDIObjectMirror && ((JDIObjectMirror)object).getObjectReference().isCollected()) {
                        collected++;
                        continue;
                    }
                    Stopwatch perObjectSW = new Stopwatch();
                    perObjectSW.start();
                    Reflection.toString(object, thread);
                    long time = perObjectSW.stop();
                    
                    count++;
                    delta = (float)time - mean;
                    mean += delta / count;
                    M2 += delta * (time - mean);
                            
                    minTime = Math.min(minTime, time);
                    maxTime = Math.max(maxTime, time);
                    
                    if (count % 25 == 0) {
                        System.out.print(".");
                    }
                    if (count % 1000 == 0) {
                        System.out.println(count);
                    }
                } catch (Throwable e) {
                    if (catchErrors) {
                        errors++;
                        try {
                            System.out.println("Error on object " + object);
                        } catch (Throwable e2) {
                            //
                        }
                        e.printStackTrace();
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            float variance = M2 / count;
            double std = Math.sqrt(variance);
            System.out.println();
            System.out.println("Num objects toString-ed: " + count);
            System.out.println("GC'd objects skipped: " + collected);
            System.out.println("Errors: " + errors);
            if (count != 0) {
                System.out.println("Total: " + (mean * count) + "ms");
                System.out.println("Min: " + minTime + "ms");
                System.out.println("Mean: " + mean + "ms");
                System.out.println("Max: " + maxTime + "ms");
                System.out.println("Std: " + std + "ms");
//                System.out.println("Max output: " + maxString);
//                for (String s : timesOver500ms) {
//                    System.out.println(s);
//                }
            }
//            HologramClassLoader.printStats();
        }
    }
    
    private static boolean hasNonDefaultToString(final ClassMirror klass, ThreadMirror thread) {
        try {
            if (klass.isArray()) {
                return false;
            }
            
            // Skip String - uninteresting
            if (klass.getClassName().equals(String.class.getName())) {
                return false;
            }
            // TODO-RS: Mild hack - if the class isn't prepared we can't look at its methods. But it
            // also can't have any instances so it's safe to skip.
            if (klass instanceof JDIClassMirror && !((JDIClassMirror)klass).getReferenceType().isInitialized()) {
                return false;
            }
            
            if (klass.isInterface()) {
                return false;
            }
            return !klass.getMethod("toString").getDeclaringClass().getClassName().equals(Object.class.getName());
        } catch (NoSuchMethodException e) {
            // Should never happen
            throw new RuntimeException(e);
        } catch (UnsupportedOperationException e) {
            // TODO-RS: Mild hack - JDIClassMirror might throw this because one of the parameter types isn't loaded.
            // Another reason I should fix the API to match JDI more closely.
            return false;
        } catch (Throwable e) {
            // Last ditch catch all
            return false;
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
