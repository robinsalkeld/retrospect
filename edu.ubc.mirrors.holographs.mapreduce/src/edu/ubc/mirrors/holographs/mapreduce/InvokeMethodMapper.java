package edu.ubc.mirrors.holographs.mapreduce;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.VoidProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.plugins.HolographVMRegistry;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class InvokeMethodMapper extends MapReduceBase 
    implements Mapper<IntWritable, IntWritable, Text, IntWritable> {

    private ISnapshot snapshot;
    private VirtualMachineHolograph holographVM;
    private ThreadMirror thread;
    private MethodMirror method;
    
    private final static IntWritable one = new IntWritable(1);
    private Text name = new Text();
    
    @Override
    public void configure(final JobConf job) {
        super.configure(job);
        
        snapshot = SnapshotUtils.openSnapshot(job);
        holographVM = HolographVMRegistry.getHolographVM(snapshot, new VoidProgressListener());
        
        thread = holographVM.getThreads().get(0);
        Reflection.withThread(thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    ClassMirror appClassLoaderClass = holographVM.findBootstrapClassMirror("sun.misc.Launcher$AppClassLoader");
                    InstanceMirror appClassLoader = (InstanceMirror)appClassLoaderClass.getInstances().get(0);
                    ClassMirror urlClassLoaderClass = holographVM.findBootstrapClassMirror(URLClassLoader.class.getName());
                    InstanceMirror ucp = (InstanceMirror)appClassLoader.get(urlClassLoaderClass.getDeclaredField("ucp"));
//                    ClassMirror urlClassPathClass = holographVM.findBootstrapClassMirror("sun.misc.URLClassPath");
//                    InstanceMirror stack = (InstanceMirror)ucp.get(urlClassPathClass.getDeclaredField("stack"));
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                }
                if (MirageClassLoader.debug) {
                    System.out.println("Finding target class...");
                }
                String targetClassName = job.get("targetClassName");
                ClassMirror nameClass = holographVM.findAllClasses(targetClassName, false).get(0);
                String analyzerClassName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameDuplicateAnalysis";
                String path = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/" +analyzerClassName.replace('.', '/') + ".class"; 
                String classPath = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/org/eclipse/cdt/internal/core/dom/parser/cpp/CPPASTNameDuplicateAnalysis.class";
                FileInputStream fis = new FileInputStream(path);
                byte[] analyzerClassBytecode = NativeClassMirror.readFully(fis);
                fis.close();
                
                ClassMirror analyzerClass = Reflection.injectBytecode(holographVM, thread, nameClass.getLoader(), 
                        analyzerClassName, analyzerClassBytecode);
                method = analyzerClass.getMethod("locationKey", nameClass);
                return null;
            }
        });
    }
    
    @Override
    public void map(IntWritable key, IntWritable value,
            OutputCollector<Text, IntWritable> output, Reporter reporter)
            throws IOException {
        
        int objectId = value.get();
        reporter.setStatus("Invoking method on object " + objectId);
        // TODO-RS: Reporter -> ProgressListener adaptor
        try {
            ObjectMirror mirror = HolographVMRegistry.getObjectMirror(snapshot, objectId, new VoidProgressListener());
            String nameString = Reflection.getRealStringForMirror((InstanceMirror)method.invoke(thread, null, mirror));
            name.set(nameString);
            output.collect(name, one);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        HolographVMRegistry.dispose(snapshot);
        SnapshotFactory.dispose(snapshot);
        snapshot = null;
        holographVM = null;
        thread = null;
        method = null;
    }
}
