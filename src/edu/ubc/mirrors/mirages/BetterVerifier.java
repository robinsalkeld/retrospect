package edu.ubc.mirrors.mirages;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;

class BetterVerifier extends SimpleVerifier {
    
    private final VirtualMachineMirror vm;
    private final ClassMirrorLoader loader;
    
    public BetterVerifier(VirtualMachineMirror vm, ClassMirrorLoader loader) {
        this.vm = vm;
        this.loader = loader;
    }
    
    private ClassMirror getClassMirror(Type t) {
        try {
            String className;
            if (t.getSort() == Type.ARRAY) {
                className = t.getInternalName().replace('/', '.');
            } else {
                className = t.getClassName();
            }
            return MirageClassLoader.loadClassMirror(vm, loader, className);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    @Override
    protected boolean isInterface(Type t) {
        return getClassMirror(t).isInterface();
    }
    
    public Type getSuperClass(Type t) {
        ClassMirror superClassMirror = getClassMirror(t).getSuperClassMirror();
        return superClassMirror == null ? null : Type.getObjectType(superClassMirror.getClassName().replace('.', '/'));
    }
    
    protected boolean isAssignableFrom(Type t, Type u) {
        if (t.equals(u)) {
            return true;
        }
        ClassMirror tNode = getClassMirror(t);
        if (tNode.isInterface()) {
            return true;
        }
        ClassMirror uNode = getClassMirror(u);
        if (uNode.isInterface()) {
            return t.getInternalName().equals("java/lang/Object");
        }
        return tNode.isAssignableFrom(uNode);
    }
}