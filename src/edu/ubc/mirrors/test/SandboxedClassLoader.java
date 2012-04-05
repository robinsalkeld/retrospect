package edu.ubc.mirrors.test;

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
            // Allow a couple of special cases through
            if (name.equals(Object.class.getName()) || name.equals(Throwable.class.getName())) {
                return super.loadClass(name, resolve);
            }
            
            c = findClass(name);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    
    @Override
    public URL getResource(String name) {
        // Allow a couple of special cases through
        if (name.equals(Object.class.getName().replace('.', '/') + ".class") || 
            name.equals(Throwable.class.getName().replace('.', '/') + ".class")) {
            return super.getResource(name);
        }
        
        return findResource(name);
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        // Allow a couple of special cases through
        if (name.equals(Object.class.getName().replace('.', '/') + ".class") || 
            name.equals(Throwable.class.getName().replace('.', '/') + ".class")) {
            return super.findResources(name);
        }
        
        return findResources(name);
    }
    
}
