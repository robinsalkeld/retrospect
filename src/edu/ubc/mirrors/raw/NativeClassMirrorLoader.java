package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class NativeClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        super(getParent(classLoader));
        this.classLoader = classLoader;
    }
    
    private static ClassMirrorLoader getParent(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        } 
        
        return new NativeClassMirrorLoader(classLoader.getParent());
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        try {
            return super.loadClassMirror(name);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
    
        Class<?> klass;
        try {
            klass = Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw e;
        }
        
        return new NativeClassMirror(klass);
    }
}
