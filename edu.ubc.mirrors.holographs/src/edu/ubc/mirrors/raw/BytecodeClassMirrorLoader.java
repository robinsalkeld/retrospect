package edu.ubc.mirrors.raw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeClassMirrorLoader extends NativeInstanceMirror implements ClassMirrorLoader {

    private final VirtualMachineMirror vm;
    private final ClassLoader loader;
    
    private final Map<String, ClassMirror> classes = new HashMap<String, ClassMirror>();
    
    public BytecodeClassMirrorLoader(VirtualMachineMirror vm, ClassLoader loader) {
        super(loader);
        this.vm = vm;
        this.loader = loader;
    }
    
    public static ClassMirror loadBytecodeClassMirror(final VirtualMachineMirror vm, final ClassMirrorLoader loaderMirror, final ClassLoader loader, Type type) {
        if (type.getSort() == Type.OBJECT) {
            String name = type.getClassName();
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
                    @Override
                    protected ClassMirror loadClassMirrorInternal(Type type) {
                        return loadBytecodeClassMirror(vm, loaderMirror, loader, type);
                    }
                    @Override
                    public boolean initialized() {
                        return false;
                    }
                };
            }
            return null;
        } else if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror;
            elementClassMirror = loadBytecodeClassMirror(vm, loaderMirror, loader, elementType);
            return vm.getArrayClass(type.getDimensions(), elementClassMirror);
        } else {
            return vm.getPrimitiveClass(type.getClassName());
        }
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        return new ArrayList<ClassMirror>(classes.values());
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(final String name) {
        ClassMirror result = classes.get(name);
        if (result != null) {
            return result;
        }

        result = loadBytecodeClassMirror(vm, this, loader, Type.getObjectType(name.replace('.', '/')));
        classes.put(name, result);
        return result;
    }
    
    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source) {
        throw new UnsupportedOperationException();
    }
}
