package edu.ubc.mirrors.holographs;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

public class DefinedClassMirror extends BytecodeClassMirror implements NewHolographInstance {
    
    public DefinedClassMirror(VirtualMachineHolograph vm, ClassLoaderHolograph loader, String className, byte[] bytecode) {
        super(className);
        this.vm = vm;
        this.loader = loader;
        this.bytecode = bytecode;
    }

    private final VirtualMachineHolograph vm;
    private final ClassLoaderHolograph loader;
    private final byte[] bytecode;
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm.getWrappedVM();
    }
    
    @Override
    public ClassMirrorLoader getLoader() {
        return loader == null ? null : (ClassMirrorLoader)loader.getWrapped();
    }
    
    @Override
    protected ClassMirror loadClassMirrorInternal(Type type) {
        ClassMirror classHolograph;
        try {
            classHolograph = Reflection.classMirrorForType(vm, ThreadHolograph.currentThreadMirror(), type, false, loader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        return unwrapClassMirror(classHolograph);
    }
    
    private ClassMirror unwrapClassMirror(ClassMirror klass) {
        if (klass instanceof ClassHolograph) {
            return ((ClassHolograph)klass).getWrappedClassMirror();
        } else if (klass.isArray()) {
            return new ArrayClassMirror(1, unwrapClassMirror(klass.getComponentClassMirror()));
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public byte[] getBytecode() {
        return bytecode;
    }
    
    @Override
    public boolean initialized() {
        return false;
    }
}
