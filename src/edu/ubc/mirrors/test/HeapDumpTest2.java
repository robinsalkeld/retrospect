package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IThreadStack;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        String snapshotPath = args[0];
        
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        ClassLoader runtimeClassLoader = HeapDumpTest2.class.getClassLoader();
//        URL java7classes = new URL("file:/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/jre/lib/rt.jar");
//        ClassLoader runtimeClassLoader = new ChainedClassLoader(new SandboxedClassLoader(new URL[] {java7classes}), HeapDumpTest.class.getClassLoader());
        
     // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        printJRubyThreadsFromSnapshot(snapshot);
    }
    
  public static void printJRubyThreadsFromSnapshot(ISnapshot snapshot) 
          throws SnapshotException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    IClass iClass = snapshot.getClassesByName(Bundle.class.getName(), true).iterator().next();
    IClassLoader classLoader = (IClassLoader)snapshot.getObject(iClass.getClassLoaderId());

    // Create an instance of the mirrors API backed by the snapshot
    HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
    vm.addNativeBytecodeLoaders(classLoader, Bundle.class.getClassLoader());
    HeapDumpClassMirrorLoader heapDumpLoader = new HeapDumpClassMirrorLoader(vm, classLoader);

    // Create a mutable layer on the object model.
    MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
    
    // Note we need a class loader that can see the classes in the JRuby API 
    // as well as our additional code (i.e. this class).
    WrappingClassMirrorLoader mutableHeapDumpLoader = mutableVM.getWrappedClassLoaderMirror(heapDumpLoader);
    ClassMirror printerClass = Reflection.injectBytecode(mutableHeapDumpLoader, new NativeClassMirror(JRubyStackTraces.class));
    
    ClassMirror rubyClass = Reflection.loadClassMirror(mutableVM, mutableHeapDumpLoader,  Ruby.class.getName());

    // For each class instance (in this case we only expect one)...
    List<InstanceMirror> rubies = rubyClass.getInstances();
    MethodMirror method = printerClass.getMethod("printStackTraces", rubyClass);
    for (InstanceMirror ruby : rubies) {
      // Invoke JRubyStackTraces#printStackTraces reflectively.
      Object result = method.invoke(ruby);
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
