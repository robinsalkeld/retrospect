package examples;

import java.io.IOException;

import edu.ubc.mirrors.MirageClassLoader;

public class MirageTest2 {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String className = args[0];
        String traceDir = args[1];
        
        new MirageClassLoader(null, traceDir).loadClass(className).getMethods();
    }
    
    
}
