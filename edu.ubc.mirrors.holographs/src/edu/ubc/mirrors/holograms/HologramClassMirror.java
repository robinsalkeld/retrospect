package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassMirror;
import edu.ubc.mirrors.holograms.HologramVirtualMachine;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class HologramClassMirror extends WrappingClassMirror {

    private final HologramVirtualMachine vm;
    private final boolean isImplementationClass;
    
    protected HologramClassMirror(HologramVirtualMachine vm, ClassMirror wrapped, boolean isImplementationClass) {
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
        return HologramClassGenerator.getHologramBinaryClassName(getOriginal().getClassName(), isImplementationClass);
    }
    
    @Override
    public byte[] getBytecode() {
        return ClassHolograph.getHologramClassLoader(getOriginal()).getBytecode(this);
    }
    
    public static ClassMirror getFrameworkClassMirror(String className) {
        try {
            return new NativeClassMirror(Class.forName(className, false, HologramClassMirror.class.getClassLoader()));
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    @Override
    public ClassMirror getSuperClassMirror() {
        if (getOriginal().getClassName().equals(Throwable.class.getName())) {
            return new NativeClassMirror(Throwable.class);
        }
        ClassMirror original = wrapped.getSuperClassMirror();
        if (original == null) {
            return null;
        }
        
        
        HologramClassMirror hologramMirror = new HologramClassMirror(vm, original, true);
        if (hologramMirror.getClassName().startsWith("edu.ubc.mirrors")) {
            return getFrameworkClassMirror(hologramMirror.getClassName());
        }
        return hologramMirror;
    }
    
}
