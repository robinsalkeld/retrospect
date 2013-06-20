package edu.ubc.mirrors.raw;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class SandboxedClassLoader extends URLClassLoader {

    public SandboxedClassLoader(URL[] urls) {
        super(urls);
    }
    
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            c = findClass(name);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    
    @Override
    public URL getResource(String name) {
        return findResource(name);
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }
    
}
