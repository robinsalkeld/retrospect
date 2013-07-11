package edu.ubc.mirrors.mirages;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;

public class BetterVerifier extends SimpleVerifier {
    
    private final VirtualMachineMirror vm;
    protected final ClassMirrorLoader loader;
    
    public BetterVerifier(VirtualMachineMirror vm, ClassMirrorLoader loader) {
        this.vm = vm;
        this.loader = loader;
    }
    
    @Override
    public BasicValue merge(final BasicValue v, final BasicValue w) {
        if (!v.equals(w)) {
            Type t = v.getType();
            Type u = w.getType();
            if (t != null
                    && (t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY))
            {
                if (u != null
                        && (u.getSort() == Type.OBJECT || u.getSort() == Type.ARRAY))
                {
                    if ("Lnull;".equals(t.getDescriptor())) {
                        return w;
                    }
                    if ("Lnull;".equals(u.getDescriptor())) {
                        return v;
                    }
                    if (isAssignableFrom(t, u)) {
                        return v;
                    }
                    if (isAssignableFrom(u, t)) {
                        return w;
                    }
                    
                    if (t.getSort() == Type.ARRAY && u.getSort() == Type.ARRAY) {
                        BasicValue vComp = new BasicValue(Reflection.makeArrayType(t.getDimensions() - 1, t.getElementType()));
                        BasicValue wComp = new BasicValue(Reflection.makeArrayType(u.getDimensions() - 1, u.getElementType()));
                        BasicValue mergedComp = merge(vComp, wComp);
                        if (!mergedComp.equals(BasicValue.UNINITIALIZED_VALUE)) {
                            Type mergedCompType = mergedComp.getType();
                            return new BasicValue(Reflection.makeArrayType(1, mergedCompType));
                        }
                    }
                    
                    do {
                        if (t == null || isInterface(t)) {
                            return BasicValue.REFERENCE_VALUE;
                        }
                        t = getSuperClass(t);
                        if (isAssignableFrom(t, u)) {
                            return newValue(t);
                        }
                    } while (true);
                }
            }
            return BasicValue.UNINITIALIZED_VALUE;
        }
        return v;
    }
    
    protected ClassMirror getClassMirror(Type t) {
        try {
            String className;
            if (t.getSort() == Type.ARRAY) {
                className = t.getInternalName().replace('/', '.');
            } else {
                className = t.getClassName();
            }
            return Reflection.classMirrorForName(vm, ThreadHolograph.currentThreadMirrorNoError(), className, false, loader);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
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
        return Reflection.isAssignableFrom(tNode, uNode);
    }
    
    protected boolean isSubTypeOf(final BasicValue value, final BasicValue expected) {
        boolean result = super.isSubTypeOf(value, expected);
        if (!result && (value.getType().getSort() == Type.OBJECT && expected.getType().getSort() == Type.OBJECT ||
                value.getType().getSort() == Type.ARRAY && expected.getType().getSort() == Type.ARRAY)) {
            super.isSubTypeOf(value, expected);
        }
        return result;
    }
}