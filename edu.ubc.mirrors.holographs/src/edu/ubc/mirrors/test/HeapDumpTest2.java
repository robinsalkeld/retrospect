package edu.ubc.mirrors.test;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
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
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
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
      
    // Create a holograph VM
    Map<String, String> mappedFiles = Reflection.getStandardMappedFiles();
    String jrubyJar = "/Users/robinsalkeld/Documents/UBC/Code/jruby-1.6.4/lib/jruby.jar";
    // TODO-RS: Haxxors to avoid handling "." correctly.
    mappedFiles.put("./jruby-1.6.4/lib/jruby.jar", jrubyJar);
    mappedFiles.put(jrubyJar, jrubyJar);
    String javaExtDir = "/System/Library/Java/Extensions";
    mappedFiles.put(javaExtDir, javaExtDir);
  
    final VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm,
            mappedFiles);
    
    Reflection.withThread(holographVM.getThreads().get(0), new Callable<Void>() {
        @Override
        public Void call() throws Exception {
    
            // Create a new class loader in the holograph VM and define more bytecode.
            ClassMirror rubyClass = holographVM.findAllClasses(Ruby.class.getName(), false).get(0);
            ThreadMirror thread = holographVM.getThreads().get(0);
            ClassMirror printerClass = Reflection.injectBytecode(holographVM, thread, 
        	    rubyClass.getLoader(), new NativeClassMirror(JRubyStackTraces.class));

            // Redirect standard out
            InstanceMirror baos = (InstanceMirror)printerClass.getMethod("redirectStdErr").invoke(thread, null);

            // For each class instance (in this case we only expect one)...
            MethodMirror method = printerClass.getMethod("printStackTraces", rubyClass);
            for (InstanceMirror ruby : rubyClass.getInstances()) {
        	// Invoke JRubyStackTraces#printStackTraces reflectively.
        	method.invoke(thread, null, ruby);
        	System.out.println(Reflection.toString(baos));
            }
            return null;
        }
    });
  }

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
