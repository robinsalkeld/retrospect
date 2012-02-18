package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.jar.JarFile;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.backtrace.BacktraceElement;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.jhat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.jhat.HeapDumpObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class HeapDumpTest implements IApplication {

    public static void main(String[] args) throws SnapshotException, SecurityException, ClassNotFoundException, IOException {
        String snapshotPath = args[0];
        String traceDir = args[1];
        String jarPath = args[2];
        
        MirageClassLoader.setTraceDir(traceDir);
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File(snapshotPath), new HashMap<String, String>(), new org.eclipse.mat.util.VoidProgressListener());
        IClass rubyObjectClass = snapshot.getClassesByName("org.jruby.RubyObject", false).iterator().next();
        IClassLoader classLoader = (IClassLoader)snapshot.getObject(rubyObjectClass.getClassLoaderId());
        
        ClassLoader runtimeClassLoader = HeapDumpTest.class.getClassLoader();
        ClassMirrorLoader nativeParent = new NativeClassMirrorLoader(runtimeClassLoader);
        HeapDumpClassMirrorLoader loader = new HeapDumpClassMirrorLoader(nativeParent, runtimeClassLoader, classLoader);
        
        MirageClassLoader mirageLoader = new MirageClassLoader(runtimeClassLoader, nativeParent);
        JarVerifier.verifyJar(mirageLoader, new JarFile(jarPath));
        
        mirageLoader.loadMirageClass(String.class).getMethods();
        mirageLoader.loadMirageClass(BacktraceElement.class).getMethods();
        mirageLoader.loadMirageClass(ISourcePosition.class).getMethods();
        mirageLoader.loadMirageClass(ThreadContext.class).getMethods();
        
//        for (int id : rubyObjectClass.getObjectIds()) {
//            IInstance object = (IInstance)snapshot.getObject(id);
//            ObjectMirror<?> mirror = new HeapDumpObjectMirror(loader, object); 
//            Object o = mirageLoader.makeMirage(mirror);
//            System.out.println(o);
//        }
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
    
}
