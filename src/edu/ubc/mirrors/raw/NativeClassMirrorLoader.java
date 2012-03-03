package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class NativeClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        super(null);
        this.classLoader = classLoader;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        
        Class<?> klass;
        try {
        
            klass = Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw e;
        }
        
        if (name.equals(System.class.getName()) || name.equals("java.lang.VMSystem")) {
            return new SystemClassMirror(klass);
        } if (name.equals(Thread.class.getName())) {
            return new ThreadClassMirror(klass);
        } else {
            return new NativeClassMirror(klass);
        }
    }
    
}
