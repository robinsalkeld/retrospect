package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class MirageClassMirrorLoader extends WrappingClassMirrorLoader {

    ClassMirrorLoader parent;
    
    public MirageClassMirrorLoader(MirageVirtualMachine vm, ClassMirrorLoader parent, ClassMirrorLoader originalLoader) {
        super(vm, originalLoader);
        this.parent = parent;
    }
    
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = parent.findLoadedClassMirror(name);
        if (result != null) {
            return result;
        }
                    
        if (name.startsWith("mirage") && !name.startsWith("miragearray")) {
            String originalClassName = getOriginalBinaryClassName(name);
            ClassMirror original = HolographInternalUtils.loadClassMirrorInternal(vm, wrappedLoader, originalClassName);
            return new MirageClassMirror((MirageVirtualMachine)vm, original, MirageClassGenerator.isImplementationClass(name));
        } else {
            return null;
        }
    }
}
