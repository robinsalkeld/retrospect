package examples;

import edu.ubc.mirrors.MirageClassLoader;
import edu.ubc.mirrors.ObjectMirage;

public class MirageTest2 {
    public static void main(String[] args) {
        String className = args[0];
        String traceDir = args[1];
        MirageClassLoader.traceClass = className;
        MirageClassLoader.setTraceDir(traceDir);
        
        MirageClassLoader mirageClassLoader = ObjectMirage.getMirageClassLoader(MirageTest2.class.getClassLoader());
        try {
            mirageClassLoader.loadClass(className).getMethod("main", String[].class).invoke(null, (Object)new String[0]);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
}
