package edu.ubc.mirrors.test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.aspectj.lang.annotation.Aspect;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class EclipseHeapDumpTest implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        
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

//        for (int id : snapshot.getClassesByName("java.lang.Class", false).iterator().next().getObjectIds()) {
//            IInstance instance = (IInstance) snapshot.getObject(id);
//            IPrimitiveArray value = (IPrimitiveArray) instance.getField("name").getValue();
//            if (value != null) {
//                String name = value.getValueArray(instance.getField("offset").getValue(), instance.getField("length").getValue());
//                IClass voidClassClass = snapshot.getClassesByName("java.lang.Void", false).iterator().next();
//                
//            }
//        }
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, 
                Reflection.getBootstrapPath(),
                Reflection.getStandardMappedFiles());
        
        ClassMirror bundleRepositoryClass = holographVM.findAllClasses(BundleRepository.class.getName(), false).get(0);
        InstanceMirror bundleRepository = bundleRepositoryClass.getInstances().get(0);
        printBundlesFromRepository(bundleRepository);
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
    
    public static void printBundles(ThreadMirror thread, ArrayMirror bundles) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassMirror classMirror = bundles.getClassMirror();
        VirtualMachineMirror vm = classMirror.getVM();
        ClassMirrorLoader loader = classMirror.getLoader();
        ClassMirror bundleArrayClass = vm.getArrayClass(1, loader.findLoadedClassMirror(Bundle.class.getName()));
        ClassMirror printerClass = (ClassMirror)getWrapped(vm, new NativeClassMirror(PrintOSGiBundles.class));
        
        MethodMirror method = printerClass.getMethod("printBundles", bundleArrayClass);
        
        // Invoke PrintOSGiBundles#print reflectively.
        long before = System.currentTimeMillis();
        InstanceMirror result = (InstanceMirror)method.invoke(thread, null, bundles);
        System.out.println(Reflection.getRealStringForMirror(result));
        System.out.println(System.currentTimeMillis() - before);
    }
    
    public static void printBundlesFromRepository(InstanceMirror bundleRepository) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        ClassMirror bundleRepositoryClass = bundleRepository.getClassMirror();
        VirtualMachineMirror vm = bundleRepositoryClass.getVM();
        // TODO-RS: Does this make sense?
        ThreadMirror thread = vm.getThreads().get(0);
        ClassMirror printerClass = Reflection.injectBytecode(vm, thread, bundleRepositoryClass.getLoader(), 
                new NativeClassMirror(PrintOSGiBundles.class));
        
        Aspect a = PrintOSGiBundles.class.getAnnotation(Aspect.class);
        
        ObjectArrayMirror annotationsMirror = (ObjectArrayMirror)Reflection.invokeMethodHandle(printerClass, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Class<?>)null).getAnnotations();
            }
        });
        
        MethodMirror method = printerClass.getMethod("print", bundleRepositoryClass);
        
        // Invoke PrintOSGiBundles#print reflectively.
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
