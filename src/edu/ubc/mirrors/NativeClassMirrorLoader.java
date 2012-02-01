package edu.ubc.mirrors;

public class NativeClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    @Override
    public ClassMirror<?> loadClassMirror(String name) throws ClassNotFoundException {
        return new NativeClassMirror(classLoader.loadClass(name));
    }
    
}
