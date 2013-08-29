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
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holograms.Stopwatch;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIClassMirror;
import edu.ubc.mirrors.jdi.JDIObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingMirror;

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
        
        Map<String, String> mappedFiles = Collections.singletonMap("/", "/");
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm,
                HeapDumpVirtualMachineMirror.defaultHolographicVMClassCacheDir(snapshot),
                mappedFiles);
        
//        holographVM.prepare();
        
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
        
        List<Integer> errorObjects = new ArrayList<Integer>();
        
//        List<ObjectMirror> objects = collectObjects(vm, thread);
        
        int[] errorObjectIds = new int[]{360988, 319641, 316713, 316708, 316703, 316698, 316693, 304594, 304589, 304584, 304579, 304574, 300932, 300927, 300920, 290282, 290277, 290272, 290191, 290184, 290177, 290170, 288166, 288161, 288156, 288151, 288146, 288141, 288136, 288131, 288126, 288121, 283254, 283249, 283242, 283237, 283232, 283227, 283222, 283217, 283212, 283207, 283202, 283197, 283192, 283187, 283182, 283177, 283172, 283167, 283162, 283157, 283150, 283145, 276879, 273501, 269948, 269943, 269938, 269933, 268219, 268214, 268209, 268081, 268076, 258174, 258169, 258164, 258159, 256027, 256022, 256017, 256012, 256007, 250976, 250971, 250966, 250961, 246313, 234255, 234250, 234245, 234240, 234235, 234230, 234225, 234220, 234215, 234210, 234205, 234200, 234195, 234190, 229099, 229094, 229089, 229084, 229079, 229074, 229069, 229064, 229059, 226876, 226638, 186715, 185756, 185686, 185591, 185464, 184511, 184377, 183146, 180820, 177799, 177794, 177440, 176508, 176034, 174964, 174849, 172107, 171389, 171345, 171084, 170793, 170681, 170595, 170371, 170046, 169566, 169225, 169013, 169006, 168725, 168312, 166172, 165487, 165008, 164855, 164625, 164615, 164410, 163560, 161838, 160357, 160208, 159388, 159383, 158158, 157751, 156642, 156344, 154909, 154164, 152733, 152655, 150653, 150474, 148490, 144956, 143215, 143200, 143012, 142049, 140797, 139641, 139491, 139471, 139429, 136879, 135059, 134944, 133302, 133297, 132002, 130763, 129262, 129009, 128294, 128072, 126724, 126719, 88682, 88067, 88019, 87390, 87081, 86765, 86735, 86415, 86293, 85747, 85742, 85714, 85359, 85081, 84968, 84871, 84789, 84453, 84211, 84206, 84152, 83909, 83446, 83342, 82775, 82678, 82580, 82460, 82320, 81710, 81676, 81117, 81105, 80948, 80756, 79773, 79707, 79463, 79189, 78984, 78937, 78798, 78652, 78439, 78411, 77454, 76945, 359200, 359164, 349052, 339894, 237783, 302550, 302549, 303262, 255337, 126053, 361378, 335366, 319637, 316957, 316689, 304570, 300914, 291683, 290563, 290268, 290164, 290161, 288117, 288114, 287372, 283141, 276875, 273968, 273495, 269929, 268205, 268072, 258155, 256003, 250957, 246309, 234186, 230205, 229055, 184874, 183398, 179668, 179496, 177768, 176713, 176229, 174035, 173859, 170661, 170455, 170133, 169318, 168847, 166915, 166254, 166253, 165167, 162212, 156613, 154738, 154169, 154067, 152701, 151773, 149082, 146568, 146063, 145179, 145136, 143361, 141473, 139407, 134948, 129194, 128603, 128166, 88656, 88016, 87675, 84117, 83046, 81691, 81681, 80923, 80682, 80458, 79608, 78889, 77812, 77451, };

        List<ObjectMirror> objects = new ArrayList<ObjectMirror>();
        VirtualMachineHolograph holographVM = (VirtualMachineHolograph)vm;
        HeapDumpVirtualMachineMirror heapDumpVM = (HeapDumpVirtualMachineMirror)holographVM.getWrappedVM();
        ISnapshot snapshot = heapDumpVM.getSnapshot();
        for (int id : errorObjectIds) {
            try {
                objects.add(holographVM.getWrappedMirror(heapDumpVM.makeMirror(snapshot.getObject(id))));
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
                
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
                } catch (final Throwable e) {
                    if (catchErrors) {
                        HeapDumpObjectMirror wrapped = (HeapDumpObjectMirror)((WrappingMirror)object).getWrapped();
                        errorObjects.add(wrapped.getHeapDumpObject().getObjectId());
                        try {
                            System.out.println("Error on object " + object);
                        } catch (Throwable e2) {
                            //
                        }
                        Reflection.withThread(thread, new Callable<Void>() {
                            public Void call() throws Exception {
                                e.printStackTrace();
                                return null;
                            }
                        });
                        if (e instanceof MirrorInvocationTargetException) {
                            InstanceMirror cause = ((MirrorInvocationTargetException)e).getTargetException();
                            Reflection.invokeMethodHandle(cause, thread, new MethodHandle(){
                                protected void methodCall() throws Throwable {
                                    ((Throwable)null).printStackTrace();
                                }
                            });
                        }
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
            System.out.println("Errors: " + errorObjects.size());
            System.out.println("IDs of error objects: ");
            System.out.print("{");
            for (int id : errorObjects) {
                System.out.print(id + ", ");
            }
            System.out.println("};");
            
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
            if (vm instanceof VirtualMachineHolograph) {
                ((VirtualMachineHolograph)vm).reportErrors();
            }
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
