package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class MutableClassMirror extends WrappingClassMirror {

    public MutableClassMirror(MutableVirtualMachineMirror vm, ClassMirror immutableClassMirror) {
        super(vm, immutableClassMirror);
    }

    @Override
    public InstanceMirror newRawInstance() {
        if (Reflection.isAssignableFrom(getVM().findBootstrapClassMirror(ClassLoader.class.getName()), this)) {
            FieldMapClassMirrorLoader immutableLoader = new FieldMapClassMirrorLoader(wrapped);
            return new MutableClassMirrorLoader((MutableVirtualMachineMirror)getVM(), immutableLoader);
        } else {
            return new FieldMapMirror(this);
        }
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        return newArray(new int[] {size});
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        return new DirectArrayMirror(wrapped.getVM().getArrayClass(dims.length, wrapped), dims);
    }
    
    public String toString() {
        return "MutableClassMirror on " + wrapped;
    };
}

