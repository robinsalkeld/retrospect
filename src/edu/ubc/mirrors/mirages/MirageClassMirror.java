package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class MirageClassMirror extends WrappingClassMirror {

    private final MirageVirtualMachine vm;
    private final boolean isImplementationClass;
    
    protected MirageClassMirror(MirageVirtualMachine vm, ClassMirror wrapped, boolean isImplementationClass) {
        super(vm, wrapped);
        this.vm = vm;
        this.isImplementationClass = isImplementationClass;
    }
    
    public ClassMirror getOriginal() {
        return wrapped;
    }
    
    public boolean isImplementationClass() {
        return isImplementationClass;
    }
    
    @Override
    public boolean isInterface() {
        if (getOriginal().getClassName().equals(Object.class.getName()) && !isImplementationClass) {
            return true;
        } else {
            return super.isInterface();
        }
    }
    
    @Override
    public String getClassName() {
        return MirageClassGenerator.getMirageBinaryClassName(getOriginal().getClassName(), isImplementationClass);
    }
    
    @Override
    public byte[] getBytecode() {
        return ClassHolograph.getMirageClassLoader(getOriginal()).getBytecode(this);
    }
    
    @Override
    public ClassMirror getSuperClassMirror() {
        if (getOriginal().getClassName().equals(Throwable.class.getName())) {
            return new NativeClassMirror(Throwable.class);
        }
        ClassMirror original = wrapped.getSuperClassMirror();
        return original == null ? null : new MirageClassMirror(vm, original, true);
    }
    
}
