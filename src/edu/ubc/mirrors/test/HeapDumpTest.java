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
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpInstanceMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;
import edu.ubc.mirrors.mutable.MutableObjectArrayMirror;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class HeapDumpTest implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException {
        String snapshotPath = args[0];
        String testClass = args[1];
        
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        String mirageClass = "mirage." + testClass;
        MirageClassLoader.traceClass = mirageClass;
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File(snapshotPath), new HashMap<String, String>(), new org.eclipse.mat.util.VoidProgressListener());
        VirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        IClass rubyObjectClass = snapshot.getClassesByName("org.jruby.RubyObject", false).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(rubyObjectClass.getClassLoaderId());
        
        ClassLoader runtimeClassLoader = HeapDumpTest.class.getClassLoader();
        ClassMirrorLoader bytecodeLoader = new NativeClassMirrorLoader(runtimeClassLoader);
        HeapDumpClassMirrorLoader loader = new HeapDumpClassMirrorLoader(vm, bytecodeLoader, classLoader);
        
        MutableVirtualMachineMirror mutableVM = new MutableVirtualMachineMirror(vm);
        MutableClassMirrorLoader mutableLoader = new MutableClassMirrorLoader(mutableVM, loader);
        
        MirageClassLoader mirageLoader = new MirageClassLoader(vm, loader, System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        
//        mirageLoader.loadClass(mirageClass).getMethods();
        
        for (int id : rubyObjectClass.getObjectIds()) {
            IInstance object = (IInstance)snapshot.getObject(id);
            HeapDumpInstanceMirror immutableMirror = new HeapDumpInstanceMirror(loader, object);
            ObjectMirror mirror = mutableVM.makeMirror(immutableMirror); 
            
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
