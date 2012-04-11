package edu.ubc.mirrors.test;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class MirageTest2 {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        String className = args[0];
        String traceDir = args[1];
        MirageClassLoader.traceClass = className;
        
        ClassLoader originalLoader = MirageTest2.class.getClassLoader();
        ClassMirrorLoader mirrorLoader = new NativeClassMirrorLoader(originalLoader);
        
        MirageClassLoader mirageClassLoader = new MirageClassLoader(NativeVirtualMachineMirror.INSTANCE, mirrorLoader, System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
//        try {
//            mirageClassLoader.loadClass(className).getMethod("main", String[].class).invoke(null, (Object)new String[0]);
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
        
//        System.out.println("Loading class...");
//        Class<?> klass = mirageClassLoader.loadClass("mirage.java.lang.ClassLoader");
//        System.out.println("Resolving class...");
//        klass.getMethods();
//        System.out.println("Resolved class!");
        
        FieldMapMirror mirror = new FieldMapMirror(new NativeClassMirror(Bar.class));
        mirror.getMemberField("f").setInt(47);
        Object b = mirageClassLoader.makeMirage(mirror);
        System.out.println("s! " + b.toString());
        System.out.println("s! " + b.toString());
        System.out.println("s! " + b.toString());
    }
    
    
}
