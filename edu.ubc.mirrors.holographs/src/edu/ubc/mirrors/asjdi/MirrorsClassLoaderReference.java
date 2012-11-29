package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class MirrorsClassLoaderReference extends MirrorsObjectReference implements ClassLoaderReference {

    private final ClassMirrorLoader wrapped;
    
    public MirrorsClassLoaderReference(MirrorsVirtualMachine vm, ClassMirrorLoader wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public List<ReferenceType> definedClasses() {
        return visibleClasses();
    }

    @Override
    public List<ReferenceType> visibleClasses() {
        List<ReferenceType> result = new ArrayList<ReferenceType>();
        for (ClassMirror klass : wrapped.loadedClassMirrors()) {
            result.add((ReferenceType)vm.typeForClassMirror(klass));
        }
        return result;
    }

}
