package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.jruby.Ruby;

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

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        printJRubyThreadsFromSnapshot(snapshot);
    }
    
  public static void printJRubyThreadsFromSnapshot(ISnapshot snapshot) throws Exception {

    // Create an instance of the mirrors API backed by the snapshot
    HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
      
    // Create a mutable layer on the object model.
    MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
    
    // Create a holograph VM
    VirtualMachineHolograph holographVM = new VirtualMachineHolograph(mutableVM,
            Reflection.getBootstrapPath(),
            Reflection.getStandardMappedFiles());
    
    // Create a new class loader in the holograph VM and define more bytecode.
    ClassMirror rubyClass = holographVM.findAllClasses(Ruby.class.getName(), false).get(0);
    ThreadMirror thread = vm.getThreads().get(0);
    ClassMirror printerClass = Reflection.injectBytecode(holographVM, thread, 
            rubyClass.getLoader(), new NativeClassMirror(JRubyStackTraces.class));
 
    // For each class instance (in this case we only expect one)...
    MethodMirror method = printerClass.getMethod("printStackTraces", rubyClass);
    for (InstanceMirror ruby : rubyClass.getInstances()) {
      // Invoke JRubyStackTraces#printStackTraces reflectively.
      Object result = method.invoke(holographVM.getThreads().get(0), null, ruby);
      String asString = Reflection.getRealStringForMirror((InstanceMirror)result);
      System.out.println(asString);
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
