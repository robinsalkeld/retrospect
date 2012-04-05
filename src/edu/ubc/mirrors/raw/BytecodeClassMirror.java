package edu.ubc.mirrors.raw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;

public abstract class BytecodeClassMirror extends ClassMirror {

    private final ClassMirrorLoader loader;
    
    private boolean resolved = false;
    
    private ClassMirror superclassNode;
    private List<ClassMirror> interfaceNodes;
    private boolean isInterface;
    private List<String> memberFieldNames = new ArrayList<String>();
    
    
    private String className;
    
    public BytecodeClassMirror(ClassMirrorLoader loader, String className) {
        this.loader = loader;
        this.className = className;
    }

    private class Stop extends RuntimeException {}
    
    private class Visitor extends ClassVisitor {

        public Visitor() {
            super(Opcodes.ASM4);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
            
            superclassNode = superName == null ? null : loadClassMirrorInternal(superName.replace('/', '.'));
            
            interfaceNodes = new ArrayList<ClassMirror>(interfaces.length);
            for (String i : interfaces) {
                interfaceNodes.add(loadClassMirrorInternal(i.replace('/', '.')));
            }
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if ((Opcodes.ACC_STATIC & access) == 0) {
                memberFieldNames.add(name);
            }
            return null;
        }
        
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return null;
        }
    }
    
    private void resolve() {
        if (resolved) {
            return;
        }
        
        try {
            new ClassReader(getBytecode()).accept(new Visitor(), 0);
        } catch (Stop e) {
            // expected
        }
        
        resolved = true;
    }
    
    @Override
    public String getClassName() {
        return className;
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
    public List<String> getDeclaredFieldNames() {
        resolve();
        return Collections.unmodifiableList(memberFieldNames);
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
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
    public ClassMirrorLoader getLoader() {
        return loader;
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + ": " + getClassName();
    }
}
