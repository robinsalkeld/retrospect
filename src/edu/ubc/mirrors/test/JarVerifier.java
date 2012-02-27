package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

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
    
    public static void verifyJar(MirageClassLoader loader, JarFile jar) {
        int total = 0;
        int good = 0;
        for (JarEntry entry : Collections.list(jar.entries())) {
            String name = entry.getName();
            total++;
            if (name.endsWith(".class")) {
                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                try {
                    loader.loadClass("mirage." + className).getMethods();
                    good++;
                } catch (Throwable t) {
                    System.out.println(className + " - " + t.getClass().getName() + ": " + t.getMessage());
                }
            }
        }
        System.out.println("Hit rate: " + good + "/" + total);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
