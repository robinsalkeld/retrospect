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
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;
import edu.ubc.mirrors.mutable.MutableObjectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        String snapshotPath = args[0];
        String testClass = args[1];
        
        MirageClassLoader.setTraceDir(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        String mirageClass = "mirage." + testClass;
        MirageClassLoader.traceClass = mirageClass;
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File(snapshotPath), new HashMap<String, String>(), new ConsoleProgressListener(System.out));
        IClass klass = snapshot.getClassesByName(HashMap.class.getName(), false).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(klass.getClassLoaderId());
        
        ClassLoader runtimeClassLoader = HeapDumpTest.class.getClassLoader();
        ClassMirrorLoader nativeParent = new NativeClassMirrorLoader(runtimeClassLoader);
        HeapDumpClassMirrorLoader loader = new HeapDumpClassMirrorLoader(nativeParent, runtimeClassLoader, classLoader);
        
        MirageClassLoader mirageLoader = new MirageClassLoader(runtimeClassLoader, nativeParent);
        
        for (int id : klass.getObjectIds()) {
            IInstance object = (IInstance)snapshot.getObject(id);
            HeapDumpObjectMirror immutableMirror = new HeapDumpObjectMirror(loader, object);
            ObjectMirror mirror = new MutableInstanceMirror(immutableMirror); 
            
            Object o = mirageLoader.makeMirage(mirror);
            try {
                System.out.println(o);
            } catch (Exception e) {
                System.out.println("Crap!");
//                e.printStackTrace();
            }
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
