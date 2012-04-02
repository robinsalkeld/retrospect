package edu.ubc.mirrors;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.raw.ArrayClassMirror;


public class ClassMirrorLoader {

    protected final ClassMirrorLoader parent;
    
    public ClassMirrorLoader() {
        this(null);
    }
    
    public ClassMirrorLoader(ClassMirrorLoader parent) {
        this.parent = parent;
    }
    
    public ClassMirrorLoader getParent() {
        return parent;
    }
    
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        Type type = Type.getObjectType(name);
        if (type.getSort() == Type.ARRAY) {
            return new ArrayClassMirror(this, type);
        }
        
        if (parent != null) {
            return parent.loadClassMirror(name);
        } else {
            throw new ClassNotFoundException(name);
        }
    }
}
