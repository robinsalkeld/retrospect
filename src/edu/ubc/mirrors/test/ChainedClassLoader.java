package edu.ubc.mirrors.test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class ChainedClassLoader extends ClassLoader {

    private final ClassLoader child;
    
    public ChainedClassLoader(ClassLoader parent, ClassLoader child) {
        super(parent);
        this.child = child;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return child.loadClass(name);
    }
    
    @Override
    protected URL findResource(String name) {
        return child.getResource(name);
    }
    
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return child.getResources(name);
    }
    
}
