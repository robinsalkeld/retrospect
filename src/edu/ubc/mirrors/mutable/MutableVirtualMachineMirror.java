package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class MutableVirtualMachineMirror extends WrappingVirtualMachine {

    public MutableVirtualMachineMirror(VirtualMachineMirror immutableVM) {
        super(immutableVM);
    }
    
    public ObjectMirror wrapMirror(ObjectMirror immutableMirror) {
        final String internalClassName = immutableMirror.getClassMirror().getClassName();
        
        if (internalClassName.equals("[Z")) {
            return new MutableBooleanArrayMirror((BooleanArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[B")) {
            return new MutableByteArrayMirror((ByteArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[C")) {
            return new MutableCharArrayMirror((CharArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[S")) {
            return new MutableShortArrayMirror((ShortArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[I")) {
            return new MutableIntArrayMirror((IntArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[J")) {
            return new MutableLongArrayMirror((LongArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[F")) {
            return new MutableFloatArrayMirror((FloatArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[D")) {
            return new MutableDoubleArrayMirror((DoubleArrayMirror)immutableMirror);
        // TODO: fix - should check immutableMirror.getClassMirror().getClassName() instead!
        } else if (immutableMirror instanceof ClassMirror) {
            return new MutableClassMirror(this, (ClassMirror)immutableMirror);
        } else if (immutableMirror instanceof ClassMirrorLoader) {
            return new MutableClassMirrorLoader(this, (ClassMirrorLoader)immutableMirror);
        } else if (immutableMirror instanceof ThreadMirror) {
            return new MutableThreadMirror(this, (ThreadMirror)immutableMirror);
        } else if (immutableMirror instanceof InstanceMirror) {
            return new MutableInstanceMirror(this, (InstanceMirror)immutableMirror);
        } else if (immutableMirror instanceof ObjectArrayMirror) {
            return new MutableObjectArrayMirror(this, (ObjectArrayMirror)immutableMirror);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + immutableMirror.getClass());
        }
    }
    
    @Override
    protected FieldMirror wrapFieldMirror(FieldMirror fieldMirror) {
        return new MutableFieldMirror(this, fieldMirror);
    }
}
