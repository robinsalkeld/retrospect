package edu.ubc.mirrors;

public class ChainedClassMirrorLoader extends ClassMirrorLoader {

    private final ClassMirrorLoader child;
    
    public ChainedClassMirrorLoader(ClassMirrorLoader parent, ClassMirrorLoader child) {
        super(parent);
        this.child = child;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        try {
            return super.loadClassMirror(name);
        } catch (ClassNotFoundException e) {
            return child.loadClassMirror(name);
        }
    }
}
