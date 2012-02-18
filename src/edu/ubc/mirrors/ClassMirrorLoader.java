package edu.ubc.mirrors;

import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class ClassMirrorLoader {

    private final ClassMirrorLoader parent;
    
    public ClassMirrorLoader() {
        this(new NativeClassMirrorLoader(ClassLoader.getSystemClassLoader()));
    }
    
    public ClassMirrorLoader(ClassMirrorLoader parent) {
        this.parent = parent;
    }
    
    public ClassMirror<?> loadClassMirror(String name) throws ClassNotFoundException {
        if (parent != null) {
            return parent.loadClassMirror(name);
        } else {
            throw new ClassNotFoundException();
        }
    }
    
}
