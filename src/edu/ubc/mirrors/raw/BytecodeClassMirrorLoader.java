package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeClassMirrorLoader extends NativeObjectMirror implements ClassMirrorLoader {

    private final VirtualMachineMirror vm;
    private final ClassLoader loader;
    
    public BytecodeClassMirrorLoader(VirtualMachineMirror vm, ClassLoader loader) {
        super(loader);
        this.vm = vm;
        this.loader = loader;
    }
    
    public static ClassMirror loadBytecodeClassMirror(final VirtualMachineMirror vm, final ClassMirrorLoader loaderMirror, ClassLoader loader, String name) {
        final byte[] bytecode = NativeClassMirror.getNativeBytecode(loader, name);
        if (bytecode != null) {
            return new BytecodeClassMirror(name) {
                @Override
                public byte[] getBytecode() {
                    return bytecode;
                }
                @Override
                public VirtualMachineMirror getVM() {
                    return vm;
                }
                @Override
                public ClassMirrorLoader getLoader() {
                    return loaderMirror;
                }
            };
        }
        
        return null;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(final String name) {
        return loadBytecodeClassMirror(vm, this, loader, name);
    }
    
    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean verify) {
        throw new UnsupportedOperationException();
    }
}
