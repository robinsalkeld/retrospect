package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class MirageClassMirror extends WrappingClassMirror {

    private final boolean isImplementationClass;
    
    protected MirageClassMirror(MirageVirtualMachine vm, ClassMirror wrapped, boolean isImplementationClass) {
        super(vm, wrapped);
        this.isImplementationClass = isImplementationClass;
    }
    
    public ClassHolograph getOriginal() {
        return (ClassHolograph)wrapped;
    }
    
    public boolean isImplementationClass() {
        return isImplementationClass;
    }
    
    @Override
    public String getClassName() {
        return MirageClassGenerator.getMirageBinaryClassName(getOriginal().getClassName(), isImplementationClass);
    }
    
    @Override
    public MirageClassMirrorLoader getLoader() {
        return (MirageClassMirrorLoader)super.getLoader();
    }
    
    @Override
    public byte[] getBytecode() {
        return getOriginal().getMirageClassLoader().getBytecode(this);
    }
    
}
