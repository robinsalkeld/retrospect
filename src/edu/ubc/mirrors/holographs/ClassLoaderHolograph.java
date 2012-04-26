package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class ClassLoaderHolograph extends WrappingClassMirrorLoader {

    protected ClassLoaderHolograph(VirtualMachineHolograph vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.mirageLoader = new MirageClassLoader(vm, this, null);
    }

    private final MirageClassLoader mirageLoader;
    
    public MirageClassLoader getMirageClassLoader() {
        return mirageLoader;
    }
}
