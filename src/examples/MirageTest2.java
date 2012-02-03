package examples;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMapMirror;
import edu.ubc.mirrors.MirageClassLoader;
import edu.ubc.mirrors.NativeClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirage;

public class MirageTest2 {
    public static void main(String[] args) {
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
        
        FieldMapMirror<Bar> mirror = new FieldMapMirror<Bar>(Bar.class);
        Object b = mirageClassLoader.makeMirage(mirror);
        b.toString();
    }
    
    
}
