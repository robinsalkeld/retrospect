/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
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
        
        final VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm,
                HeapDumpVirtualMachineMirror.defaultHolographicVMClassCacheDir(snapshot),
                mappedFiles);
        
        final ThreadMirror thread = holographVM.getThreads().get(0);
//        Reflection.withThread(thread, new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
////                ClassMirror obr = holographVM.findAllClasses("org.apache.felix.gogo.command.OBR", false).iterator().next();
////                Reflection.classMirrorForName(holographVM, thread, "org.apache.felix.bundlerepository.Repository", true, obr.getLoader());
//                String[] badClassNames = new String[] {"org.eclipse.jface.resource.ArrayFontDescriptor", "org.eclipse.jface.resource.FontDescriptor"};
//                for (String name : badClassNames) {
//                    ClassMirror klass = holographVM.findAllClasses(name, false).iterator().next();
//                    holographVM.prepareClass(klass);
//                }
//                return null;
//            }
//        });
        
//        holographVM.prepare();
        
        toStringAllTheObjects(holographVM, thread);
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
        
        List<ObjectMirror> objects = collectObjects(vm, thread);
        
        // Object that has a legitimate error (would have been garbage collected in a live system): 126053
//        int[] errorObjectIds = new int[]{118992};
//
//        List<ObjectMirror> objects = new ArrayList<ObjectMirror>();
//        VirtualMachineHolograph holographVM = (VirtualMachineHolograph)vm;
//        HeapDumpVirtualMachineMirror heapDumpVM = (HeapDumpVirtualMachineMirror)holographVM.getWrappedVM();
//        ISnapshot snapshot = heapDumpVM.getSnapshot();
//        for (int id : errorObjectIds) {
//            try {
//                objects.add(holographVM.getWrappedMirror(heapDumpVM.makeMirror(snapshot.getObject(id))));
//            } catch (SnapshotException e) {
//                throw new RuntimeException(e);
//            }
//        }
                
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
                        int objectId = wrapped.getHeapDumpObject().getObjectId();
                        errorObjects.add(objectId);
                        try {
                            System.out.println("Error on object " + object + " (" + objectId + ")");
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
