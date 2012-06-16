package edu.ubc.mirrors.mirages;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public abstract class MethodHandle {
    public static MethodHandle MIRAGE_GET_MIRROR = new MethodHandle() {
        Mirage mirage;
        @Override 
        public void methodCall() {
            mirage.getMirror();
        }
    };
    
    public static MethodHandle OBJECT_MIRAGE_MAKE = new MethodHandle() {
        @Override 
        public void methodCall() {
            ObjectMirage.make(null);
        }
    };
    
    public static MethodHandle OBJECT_MIRAGE_MAKE_STRING_MIRAGE = new MethodHandle() {
        @Override
        public void methodCall() {
            ObjectMirage.makeStringMirage(null, null);
        }
    };

    public static MethodHandle OBJECT_MIRAGE_MAKE_CLASS_MIRAGE = new MethodHandle() {
        @Override
        public void methodCall() {
            ObjectMirage.makeClassMirage(null, null);
        }
    };
    
    public static MethodHandle MIRAGE_CLASS_LOADER_LOAD_ORIGINAL_CLASS_MIRROR = new MethodHandle() {
        MirageClassLoader loader = null;
        @Override
        public void methodCall() {
            loader.loadOriginalClassMirror((String)null);
        }
    };
    
    public static MethodHandle CLASS_MIRROR_GET_DECLARED_FIELDS = new MethodHandle() {
        ClassMirror classMirror = null;
        @Override
        public void methodCall() {
            classMirror.getDeclaredFields();
        }
    };

    ////
    
    private MethodInsnNode method;
    
    private class MyClassVisitor extends ClassVisitor {
        
        public MyClassVisitor() {
            super(Opcodes.ASM4);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            if (name.equals("methodCall")) {
                return new MyMethodVisitor();
            } else {
                return null;
            }
        }
    }
    
    private class MyMethodVisitor extends MethodVisitor {

        public MyMethodVisitor() {
            super(Opcodes.ASM4);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (method != null) {
                throw new IllegalStateException("Found more than one method call");
            }
            method = new MethodInsnNode(opcode, owner, name, desc);
        }
    }
    
    protected abstract void methodCall() throws Throwable;
    
    public synchronized MethodInsnNode getMethod() {
        if (method == null) {
            ClassReader myReader = new ClassReader(new NativeClassMirror(getClass()).getBytecode());
            myReader.accept(new MyClassVisitor(), 0);
            if (method == null) {
                throw new IllegalStateException("Found no method calls");
            }
        }
        return method;
    }
    
    public void invoke(MethodVisitor visitor) {
        getMethod().accept(visitor);
    }
}
