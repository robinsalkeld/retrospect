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

public class MutableClassMirrorLoader extends MutableInstanceMirror implements ClassMirrorLoader {

    private final MutableVirtualMachineMirror vm;
    private final ClassMirrorLoader immutableLoader;
    
    public MutableClassMirrorLoader(MutableVirtualMachineMirror vm, ClassMirrorLoader immutableLoader) {
        super(vm, immutableLoader);
        this.vm = vm;
        this.immutableLoader = immutableLoader;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return (ClassMirror)vm.makeMirror(immutableLoader.findLoadedClassMirror(name));
    }
}
