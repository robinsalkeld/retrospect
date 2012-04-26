package edu.ubc.mirrors.raw;

import java.net.URL;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeOnlyVirtualMachineMirror implements VirtualMachineMirror {

    // May be null
    private final ClassLoader bootstrapLoader;
    
    public BytecodeOnlyVirtualMachineMirror(ClassLoader bootstrapLoader) {
        this.bootstrapLoader = bootstrapLoader;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        // Note using null as the ClassMirrorLoader since the bootstrapLoader acts like the
        // VM's bootstrap loader, which is sometimes (and in this case) represented by null.
        return BytecodeClassMirrorLoader.loadBytecodeClassMirror(this, null, bootstrapLoader, name);
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name) {
        throw new UnsupportedOperationException(); 
    }    
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        throw new UnsupportedOperationException();
    }
}
