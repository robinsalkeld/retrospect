package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.jhat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.jhat.HeapDumpObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;
import edu.ubc.mirrors.mutable.MutableObjectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class HeapDumpTest implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException {
        String snapshotPath = args[0];
        String testClass = args[1];
        
        MirageClassLoader.setTraceDir(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        String mirageClass = "mirage." + testClass;
        MirageClassLoader.traceClass = mirageClass;
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File(snapshotPath), new HashMap<String, String>(), new org.eclipse.mat.util.VoidProgressListener());
        IClass rubyObjectClass = snapshot.getClassesByName("org.jruby.RubyObject", false).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(rubyObjectClass.getClassLoaderId());
        
        ClassLoader runtimeClassLoader = HeapDumpTest.class.getClassLoader();
        ClassMirrorLoader nativeParent = new NativeClassMirrorLoader(runtimeClassLoader);
        HeapDumpClassMirrorLoader loader = new HeapDumpClassMirrorLoader(nativeParent, runtimeClassLoader, classLoader);
        
        MirageClassLoader mirageLoader = new MirageClassLoader(runtimeClassLoader, nativeParent);
        
//        mirageLoader.loadClass(mirageClass).getMethods();
        
        for (int id : rubyObjectClass.getObjectIds()) {
            IInstance object = (IInstance)snapshot.getObject(id);
            HeapDumpObjectMirror immutableMirror = new HeapDumpObjectMirror(loader, object);
            ObjectMirror mirror = new MutableInstanceMirror(immutableMirror); 
            
            Object rubyRuntime = getRubyObjectRuntime((InstanceMirror)mirror);
            InstanceMirror threadClass = (InstanceMirror)((InstanceMirror)rubyRuntime).getMemberField("threadClass").get();
            Object rubyThreadRuntime = getRubyObjectRuntime(threadClass);
            
            Object o = mirageLoader.makeMirage(mirror);
            try {
                System.out.println(o);
            } catch (Exception e) {
                System.out.println("Crap!");
//                e.printStackTrace();
            }
        }
    }
    
    private static ObjectMirror getRubyObjectRuntime(InstanceMirror mirror) throws IllegalAccessException, NoSuchFieldException {
        return ((InstanceMirror)mirror.getMemberField("metaClass").get()).getMemberField("runtime").get();
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
    
}
