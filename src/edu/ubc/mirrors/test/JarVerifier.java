package edu.ubc.mirrors.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class JarVerifier implements IApplication {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String jarPath = args[0];
        String tracePath = args[1];
        JarFile jar = new JarFile(jarPath);
        ClassLoader thisLoader = JarVerifier.class.getClassLoader();
        MirageClassLoader mirageLoader = new MirageClassLoader(thisLoader, new NativeClassMirrorLoader(thisLoader));
        MirageClassLoader.setTraceDir(tracePath);
        verifyJar(mirageLoader, jar);
    }
    
    public static void verifyJar(MirageClassLoader loader, JarFile jar) throws IOException {
        int total = 0;
        int good = 0;
        String classPath = "com/kenai/jaffl/provider/jffi/DefaultInvokerFactory";
        testClass(loader, jar.getInputStream(jar.getEntry(classPath + ".class")), classPath.replace('/', '.'));
        for (JarEntry entry : Collections.list(jar.entries())) {
            String name = entry.getName();
            total++;
            if (name.endsWith(".class")) {
                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                if (testClass(loader, jar.getInputStream(entry), className)) good++;
            }
        }
        System.out.println("Hit rate: " + good + "/" + total);
    }
    
    private static boolean testClass(MirageClassLoader loader, InputStream bytesIn, String className) {
        try {
            try {
                CheckClassAdapter.verify(new ClassReader(bytesIn), loader, false, new PrintWriter(System.out));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            loader.loadClass("mirage." + className).getMethods();
            return true;
        } catch (Throwable t) {
            System.out.println(className + " - " + t.getClass().getName() + ": " + t.getMessage());
        }
        return false;
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
