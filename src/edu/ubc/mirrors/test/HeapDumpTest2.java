package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        String snapshotPath = args[0];
        
        MirageClassLoader.debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
        
        ClassLoader runtimeClassLoader = HeapDumpTest.class.getClassLoader();
//        URL java7classes = new URL("file:/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/jre/lib/rt.jar");
//        ClassLoader runtimeClassLoader = new ChainedClassLoader(new SandboxedClassLoader(new URL[] {java7classes}), HeapDumpTest.class.getClassLoader());
        
        ClassMirrorLoader bytecodeLoader = new BytecodeClassMirrorLoader(runtimeClassLoader);
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File(snapshotPath), new HashMap<String, String>(), new ConsoleProgressListener(System.out));
        IClass iClass = snapshot.getClassesByName(Ruby.class.getName(), false).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(iClass.getClassLoaderId());
        HeapDumpClassMirrorLoader loader = new HeapDumpClassMirrorLoader(bytecodeLoader, classLoader);
        
        HeapDumpClassMirror klass = new HeapDumpClassMirror(loader, iClass);
        
//        Snapshot snapshot = Reader.readFile(snapshotPath, false, 0);
//        snapshot.resolve(false);
//        JHatClassMirrorLoader loader = new JHatClassMirrorLoader(snapshot, runtimeClassLoader);
//        JHatClassMirror klass = (JHatClassMirror)loader.loadClassMirror(HashMap.class.getName());
        
        MutableClassMirrorLoader mutableLoader = new MutableClassMirrorLoader(loader);
        MirageClassLoader mirageLoader = new MirageClassLoader(runtimeClassLoader, mutableLoader, System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
        Class<?> mirageClass = mirageLoader.loadMirageClass(JRubyStackTraces.class);
        mirageClass.getMethods();
        
        List<ObjectMirror> instances = klass.getInstances();
        int good = 0;
        for (ObjectMirror mirror : instances) {
            mirror = mutableLoader.makeMirror(mirror); 
            
            Object o = mirageLoader.makeMirage(mirror);
            try {
                Object result = Reflection.invoke(mirageClass, "printStackTraces", o);
                System.out.println(result);
                good++;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        System.out.println("good: " + good + "/" + instances.size());
        
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
