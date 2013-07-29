package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public class HolographField extends Holograph implements Field {

    public HolographField(JDIHolographVirtualMachine vm, ReferenceType declaringType, String name) {
        super(vm, vm.mirrorOf(name));
        this.declaringType = declaringType;
        this.name = name;
    }

    private final ReferenceType declaringType;
    private final String name;
    
    @Override
    public ReferenceType declaringType() {
        return declaringType;
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String signature() {
        return null;
    }

    @Override
    public boolean isPackagePrivate() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public int modifiers() {
        return 0;
    }

    @Override
    public int compareTo(Field o) {
        return name.compareTo(((HolographField)o).name);
    }

    @Override
    public boolean isEnumConstant() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public Type type() throws ClassNotLoadedException {
        return null;
    }

    @Override
    public String typeName() {
        return null;
    }

}
