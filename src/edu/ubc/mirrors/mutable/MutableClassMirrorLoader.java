package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class MutableClassMirrorLoader extends WrappingClassMirrorLoader {

    public MutableClassMirrorLoader(MutableVirtualMachineMirror vm, ClassMirrorLoader immutableLoader) {
        super(vm, immutableLoader);
    }
    
    
}
