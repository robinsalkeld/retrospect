package examples;

import java.io.IOException;

import edu.ubc.mirrors.MirageClassLoader;

public class MirageTest2 {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String className = args[0];
        String mirageTraceDir = args[1];
        String nativeTraceDir = args[2];
        
        new MirageClassLoader(MirageTest2.class.getClassLoader(), mirageTraceDir, nativeTraceDir).loadClass(className).getMethods();
    }
    
    
}
