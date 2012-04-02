package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class BytecodeClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader loader;
    
    public BytecodeClassMirrorLoader(ClassLoader loader) {
        super(getParent(loader));
        this.loader = loader;
    }
    
    private static ClassMirrorLoader getParent(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        } 
        
        return new NativeClassMirrorLoader(classLoader.getParent());
    }
    
    @Override
    public ClassMirror loadClassMirror(final String name) throws ClassNotFoundException {
        try {
            return super.loadClassMirror(name);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        
        final byte[] bytecode = NativeClassMirror.getNativeBytecode(loader, name);
        if (bytecode != null) {
            return new BytecodeClassMirror(this, name) {
                @Override
                public byte[] getBytecode() {
                    return bytecode;
                }
            };
        }
        
        throw new ClassNotFoundException(name);
    }
    
}
