package edu.ubc.mirrors.asjdi;

import java.lang.reflect.Modifier;

import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsMirrorWithModifiers extends MirrorsMirror {

    public MirrorsMirrorWithModifiers(MirrorsVirtualMachine vm, Object wrapped) {
        super(vm, wrapped);
    }

    public abstract int modifiers();

    public boolean isFinal() {
        return Modifier.isFinal(modifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers());
    }

    public boolean isSynthetic() {
        return (0x00001000 & modifiers()) != 0;
    }

    public boolean isPackagePrivate() {
        return !(isPublic() || isProtected() || isPrivate());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(modifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(modifiers());
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers());
    }

    public boolean isBridge() {
        return (0x00000040 & modifiers()) != 0;
    }

    public boolean isNative() {
        return Modifier.isNative(modifiers());
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers());
    }
    
    public boolean isVarArgs() {
        return (0x00000080 & modifiers()) != 0;
    }

    public boolean isTransient() {
        return Modifier.isTransient(modifiers());
    }
    
    public boolean isVolatile() {
        return Modifier.isVolatile(modifiers());
    }
    
    public boolean isEnum() {
        return (0x00004000 & modifiers()) != 0;
    }
    
}
