package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class NativeClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    @Override
    public ClassMirror<?> loadClassMirror(String name) throws ClassNotFoundException {
        if (name.equals("java.lang.System") || name.equals("java.lang.VMSystem")) {
            return new SystemClassMirror(classLoader.loadClass(name));
        } else if (name.equals("java.lang.Class")) {
            return new ClassClassMirror(classLoader.loadClass(name));
        } else {
            return new NativeClassMirror(classLoader.loadClass(name));
        }
    }
    
}
