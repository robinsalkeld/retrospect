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

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class EclipseHeapDumpTest implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
//        Bundle thisBundle = FrameworkUtil.getBundle(EclipseHeapDumpTest.class);
//        thisBundle.start();
//        Bundle[] bundles = thisBundle.getBundleContext().getBundles();
//        
//        VirtualMachineHolograph holographNativeVM = new VirtualMachineHolograph(NativeVirtualMachineMirror.INSTANCE);
//        ObjectMirror bundlesMirror = NativeInstanceMirror.makeMirror(bundles);
//        ObjectMirror holographBundlesMirror = getWrapped(holographNativeVM, bundlesMirror);
//        
//        ThreadMirror thread = (ThreadMirror)getWrapped(holographNativeVM, NativeInstanceMirror.makeMirror(Thread.currentThread()));
//        
//        long before = System.currentTimeMillis();
//        printBundles(thread, (ArrayMirror)holographBundlesMirror);
//        System.out.println(System.currentTimeMillis() - before);
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));

        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
        final VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, 
                HeapDumpVirtualMachineMirror.defaultHolographicVMClassCacheDir(snapshot),
                Reflection.getStandardMappedFiles());
        
        Reflection.withThread(holographVM.getThreads().get(0), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (HologramClassLoader.debug) {
                    System.out.println("Finding target class...");
                }
        	ClassMirror bundleRepositoryClass = holographVM.findAllClasses(BundleRepository.class.getName(), false).get(0);
        	if (HologramClassLoader.debug) {
                    System.out.println("Finding target instance...");
                }
        	ObjectMirror bundleRepository = bundleRepositoryClass.getInstances().get(0);
        	printBundlesFromRepository(bundleRepository);
                return null;
            }
	});
        
    }
    
    public static ObjectMirror getWrapped(VirtualMachineMirror vm, ObjectMirror o) {
        if (vm instanceof WrappingVirtualMachine) {
            WrappingVirtualMachine wrappingVM = (WrappingVirtualMachine)vm;
            ObjectMirror inner = getWrapped(wrappingVM.getWrappedVM(), o);
            return wrappingVM.getWrappedMirror(inner);
        } else {
            return o;
        }
    }
    
    public static void printBundles(ThreadMirror thread, ArrayMirror bundles) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException {
        ClassMirror classMirror = bundles.getClassMirror();
        VirtualMachineMirror vm = classMirror.getVM();
        ClassMirrorLoader loader = classMirror.getLoader();
        ClassMirror bundleArrayClass = vm.getArrayClass(1, loader.findLoadedClassMirror(Bundle.class.getName()));
        ClassMirror printerClass = (ClassMirror)getWrapped(vm, new NativeClassMirror(PrintOSGiBundles.class));
        
        MethodMirror method = printerClass.getDeclaredMethod("printBundles", bundleArrayClass);
        
        // Invoke PrintOSGiBundles#print reflectively.
        long before = System.currentTimeMillis();
        InstanceMirror result = (InstanceMirror)method.invoke(thread, null, bundles);
        System.out.println(Reflection.getRealStringForMirror(result));
        System.out.println(System.currentTimeMillis() - before);
    }
    
    public static void printBundlesFromRepository(ObjectMirror bundleRepository) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException {
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        ClassMirror bundleRepositoryClass = bundleRepository.getClassMirror();
        VirtualMachineMirror vm = bundleRepositoryClass.getVM();
        // TODO-RS: Does this make sense?
        ThreadMirror thread = vm.getThreads().get(0);
        if (HologramClassLoader.debug) {
            System.out.println("Injecting bytecode...");
        }
        
        NativeClassMirror nativePrinterClass = new NativeClassMirror(PrintOSGiBundles.class);
        ClassMirror printerClass = Reflection.injectBytecode(vm, thread, bundleRepositoryClass.getLoader(), 
                nativePrinterClass.getClassName(), nativePrinterClass.getBytecode());
        MethodMirror method = printerClass.getDeclaredMethod("print", bundleRepositoryClass);
        
        // Invoke PrintOSGiBundles#print reflectively.
        if (HologramClassLoader.debug) {
            System.out.println("Invoking...");
        }
        long before = System.currentTimeMillis();
        InstanceMirror result = (InstanceMirror)method.invoke(thread, null, bundleRepository);
        System.out.println(Reflection.getRealStringForMirror(result));
        System.out.println(System.currentTimeMillis() - before);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
