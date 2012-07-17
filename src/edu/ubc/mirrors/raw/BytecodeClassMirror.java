package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Reflection;

public abstract class BytecodeClassMirror implements ClassMirror {

    public class StaticField extends BoxingFieldMirror {

        private final String name;
        private final ClassMirror type;
        
        public StaticField(String name, ClassMirror type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StaticField)) {
                return false;
            }
            
            return name.equals(((StaticField)obj).name);
        }
        
        @Override
        public int hashCode() {
            return 17 + name.hashCode();
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public ClassMirror getType() {
            return type;
        }

        @Override
        public ObjectMirror get() throws IllegalAccessException {
            return null;
        }

        @Override
        public void set(ObjectMirror o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getBoxedValue() throws IllegalAccessException {
            return null;
        }

        @Override
        public void setBoxedValue(Object o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

    }

    private static class MethodPlaceholder implements MethodMirror {

        @Override
        public Object invoke(ThreadMirror thread, InstanceMirror obj,
                Object... args) throws IllegalArgumentException,
                IllegalAccessException, InvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAccessible(boolean flag) {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private boolean resolved = false;
    
    private int access;
    private ClassMirror superclassNode;
    private List<ClassMirror> interfaceNodes;
    private boolean isInterface;
    private Map<String, ClassMirror> memberFieldNames = new LinkedHashMap<String, ClassMirror>();
    private Map<String, ClassMirror> staticFields = new LinkedHashMap<String, ClassMirror>();
    private final Set<Method> methods = new HashSet<Method>();
    
    protected String className;
    
    public BytecodeClassMirror(String className) {
        this.className = className;
    }

    private class Visitor extends ClassVisitor {

        public Visitor() {
            super(Opcodes.ASM4);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            BytecodeClassMirror.this.access = access;
            isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
            
            superclassNode = superName == null ? null : loadClassMirrorInternal(Type.getObjectType(superName));
            
            interfaceNodes = new ArrayList<ClassMirror>(interfaces.length);
            for (String i : interfaces) {
                interfaceNodes.add(loadClassMirrorInternal(Type.getObjectType(i)));
            }
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if ((Opcodes.ACC_STATIC & access) == 0) {
                memberFieldNames.put(name, loadClassMirrorInternal(Type.getType(desc)));
            } else {
                staticFields.put(name, loadClassMirrorInternal(Type.getType(desc)));
            }
            return null;
        }
        
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            methods.add(new org.objectweb.asm.commons.Method(name, desc));
            return null;
        }
    }
    
    protected ClassMirror loadClassMirrorInternal(Type type) {
        try {
            return Reflection.classMirrorForType(getVM(), type, false, getLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
    private void resolve() {
        if (resolved) {
            return;
        }
        
        new ClassReader(getBytecode()).accept(new Visitor(), 0);
        
        resolved = true;
    }
    
    @Override
    public String getClassName() {
        return className;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
    
    @Override
    public abstract byte[] getBytecode();

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        resolve();
        return superclassNode;
    }

    @Override
    public boolean isInterface() {
        resolve();
        return isInterface;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        resolve();
        return interfaceNodes;
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        resolve();
        return Collections.unmodifiableMap(memberFieldNames);
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        resolve();
        ClassMirror type = staticFields.get(name);
        if (type != null) {
            return new StaticField(name, type);
        } else {
            throw new NoSuchFieldException(name);
        }
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramClasses)
            throws SecurityException, NoSuchMethodException {
        resolve();
        for (Method m : methods) {
            if (!m.getName().equals(name)) {
                continue;
            }
            Type[] paramTypes = m.getArgumentTypes();
            if (paramTypes.length != paramClasses.length) {
                continue;
            }
            for (int i = 0; i < paramTypes.length; i++) {
                if (!paramClasses[i].getClassName().equals(paramTypes[i].getClassName())) {
                    continue;
                }
            }
            return new MethodPlaceholder();
        }

        throw new NoSuchMethodException(name);
    }
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        // Could create un-invocable methods, but no use for that yet.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        // Could create un-invocable methods, but no use for that yet.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        resolve();
        return access;
    }
    
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getClassName();
    }
}
