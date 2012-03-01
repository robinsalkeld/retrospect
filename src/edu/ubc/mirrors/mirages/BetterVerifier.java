package edu.ubc.mirrors.mirages;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

class BetterVerifier extends SimpleVerifier {
    
    private final ClassHierarchy hierarchy;
    
    public BetterVerifier(ClassHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }
    
    private ClassHierarchy.Node getNode(Type t) {
        try {
            return hierarchy.getNode(t.getInternalName());
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    @Override
    protected boolean isInterface(Type t) {
        return getNode(t).isInterface();
    }
    
    public Type getSuperClass(Type t) {
        return Type.getObjectType(getNode(t).getSuperclass().getInternalClassName());
    }
    
    protected boolean isAssignableFrom(Type t, Type u) {
        if (u.equals(Type.getObjectType("[Lorg/jruby/runtime/builtin/IRubyObject;"))){
            int bp = 5;
        }
        if (t.equals(u)) {
            return true;
        }
        ClassHierarchy.Node tNode = getNode(t);
        if (tNode.isInterface()) {
            return true;
        }
        ClassHierarchy.Node uNode = getNode(u);
        if (uNode.isInterface()) {
            return t.getInternalName().equals("java/lang/Object");
        }
        return tNode.isAssignableFrom(uNode);
    }
}