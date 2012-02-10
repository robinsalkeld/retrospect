package edu.ubc.mirrors.test;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class MirageTest2 {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        String className = args[0];
        String traceDir = args[1];
        MirageClassLoader.traceClass = className;
        MirageClassLoader.setTraceDir(traceDir);
        
        ClassLoader originalLoader = MirageTest2.class.getClassLoader();
        ClassMirrorLoader mirrorLoader = new NativeClassMirrorLoader(originalLoader);
        
        MirageClassLoader mirageClassLoader = new MirageClassLoader(originalLoader, mirrorLoader);
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
        
        FieldMapMirror<Bar> mirror = new FieldMapMirror<Bar>(Bar.class);
        mirror.getMemberField("f").setInt(47);
        Object b = mirageClassLoader.makeMirage(mirror);
        System.out.println("s! " + b.toString());
        System.out.println("s! " + b.toString());
        System.out.println("s! " + b.toString());
    }
    
    
}
