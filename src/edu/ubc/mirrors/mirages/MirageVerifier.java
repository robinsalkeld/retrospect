package edu.ubc.mirrors.mirages;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class MirageVerifier extends BetterVerifier {

    private final MirageVirtualMachine vm;
    
    public MirageVerifier(VirtualMachineHolograph vm, ClassLoaderHolograph loader) {
        super(vm, loader);
        this.vm = new MirageVirtualMachine(vm);
    }

    @Override
    protected ClassMirror getClassMirror(Type t) {
        if (t.getInternalName().equals(ClassLoaderLiteralMirror.CLASS_LOADER_LITERAL_NAME)) {
            return new ClassLoaderLiteralMirror(vm, loader);
        }
        String className = t.getClassName();
        String tPackage = className.substring(0, className.lastIndexOf('.'));
        if (tPackage.equals("edu.ubc.mirrors") || tPackage.equals("edu.ubc.mirrors.mirages") || className.equals(Throwable.class.getName())) {
            return MirageClassMirror.getFrameworkClassMirror(className);
        }
        Type originalType = MirageClassGenerator.getOriginalType(t);
        boolean isImplementation = MirageClassGenerator.isImplementationClass(className);
        ClassHolograph klass = (ClassHolograph)super.getClassMirror(originalType);
        return new MirageClassMirror(vm, klass, isImplementation);
    }
    
}
