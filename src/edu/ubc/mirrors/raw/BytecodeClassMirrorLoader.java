package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class BytecodeClassMirrorLoader extends NativeObjectMirror implements ClassMirrorLoader {

    private final ClassLoader loader;
    
    public BytecodeClassMirrorLoader(ClassLoader loader) {
        super(loader);
        this.loader = loader;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(final String name) {
        final byte[] bytecode = NativeClassMirror.getNativeBytecode(loader, name);
        if (bytecode != null) {
            return new BytecodeClassMirror(this, name) {
                @Override
                public byte[] getBytecode() {
                    return bytecode;
                }
            };
        }
        
        return null;
    }
    
}
