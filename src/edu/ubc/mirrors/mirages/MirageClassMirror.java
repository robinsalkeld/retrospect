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
    
    public static ClassMirror getFrameworkClassMirror(String className) {
        try {
            return new NativeClassMirror(Class.forName(className, false, MirageClassMirror.class.getClassLoader()));
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
        
        
        MirageClassMirror mirageMirror = new MirageClassMirror(vm, original, true);
        if (mirageMirror.getClassName().startsWith("edu.ubc.mirrors")) {
            return getFrameworkClassMirror(mirageMirror.getClassName());
        }
        return mirageMirror;
    }
    
}
