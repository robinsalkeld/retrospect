package edu.ubc.mirrors;

// TODO: Probably need to add delegation like ClassLoaders
public class ClassMirrorLoader {

    public ClassMirror<?> loadClassMirror(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }
    
}
