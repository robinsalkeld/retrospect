package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class MirageClassMirrorLoader extends WrappingClassMirrorLoader {

    ClassMirrorLoader parent;
    
    ClassLoaderLiteralMirror classLoaderLiteralMirror = new ClassLoaderLiteralMirror(vm, this);
    
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
            ClassMirror original;
            try {
                original = Reflection.loadClassMirror(vm, vm.getThreads().get(0), wrappedLoader, originalClassName);
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
            return new MirageClassMirror((MirageVirtualMachine)vm, original, MirageClassGenerator.isImplementationClass(name));
        } else if (name.equals(ClassLoaderLiteralMirror.CLASS_LOADER_LITERAL_NAME)) {
            return classLoaderLiteralMirror;
        } else {
            return null;
        }
    }
}
