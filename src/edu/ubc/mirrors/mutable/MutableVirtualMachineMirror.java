package edu.ubc.mirrors.mutable;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class MutableVirtualMachineMirror implements VirtualMachineMirror {

    private final VirtualMachineMirror immutableVM;
    
    public MutableVirtualMachineMirror(VirtualMachineMirror immutableVM) {
        this.immutableVM = immutableVM;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        return (ClassMirror)makeMirror(immutableVM.findBootstrapClassMirror(name));
    }

    private final Map<ObjectMirror, ObjectMirror> mirrors = new HashMap<ObjectMirror, ObjectMirror>();
    
    public ObjectMirror makeMirror(ObjectMirror immutableMirror) {
        if (immutableMirror == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(immutableMirror);
        if (result != null) {
            return result;
        }
        
        final String internalClassName = immutableMirror.getClassMirror().getClassName();
        
        if (internalClassName.equals("[Z")) {
            result = new MutableBooleanArrayMirror((BooleanArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[B")) {
            result = new MutableByteArrayMirror((ByteArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[C")) {
            result = new MutableCharArrayMirror((CharArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[S")) {
            result = new MutableShortArrayMirror((ShortArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[I")) {
            result = new MutableIntArrayMirror((IntArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[J")) {
            result = new MutableLongArrayMirror((LongArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[F")) {
            result = new MutableFloatArrayMirror((FloatArrayMirror)immutableMirror);
        } else if (internalClassName.equals("[D")) {
            result = new MutableDoubleArrayMirror((DoubleArrayMirror)immutableMirror);
        // TODO: fix - should check immutableMirror.getClassMirror().getClassName() instead!
        } else if (immutableMirror instanceof ClassMirror) {
            result = new MutableClassMirror(this, (ClassMirror)immutableMirror);
        } else if (immutableMirror instanceof ClassMirrorLoader) {
            
            result = new MutableClassMirrorLoader(this, (ClassMirrorLoader)immutableMirror);
        } else if (immutableMirror instanceof ThreadMirror) {
            result = new MutableThreadMirror(this, (ThreadMirror)immutableMirror);
        } else if (immutableMirror instanceof InstanceMirror) {
            result = new MutableInstanceMirror(this, (InstanceMirror)immutableMirror);
        } else if (immutableMirror instanceof ObjectArrayMirror) {
            result = new MutableObjectArrayMirror(this, (ObjectArrayMirror)immutableMirror);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + immutableMirror.getClass());
        }
        
        mirrors.put(immutableMirror, result);
        return result;
    }
    
    private static Map<ClassMirrorLoader, MutableClassMirrorLoader> loaders = new
               HashMap<ClassMirrorLoader, MutableClassMirrorLoader>();
}
