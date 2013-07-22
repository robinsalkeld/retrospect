package edu.ubc.mirrors;

import java.util.concurrent.Callable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;

public abstract class MethodHandle {
    public static MethodHandle OBJECT_HOLOGRAM_GET_MIRROR = new MethodHandle() {
        @Override 
        public void methodCall() {
            ObjectHologram.getMirror(null);
        }
    };
    
    public static MethodHandle OBJECT_HOLOGRAM_MAKE = new MethodHandle() {
        @Override 
        public void methodCall() {
            ObjectHologram.make(null);
        }
    };
    
    public static MethodHandle HOLOGRAM_CLASS_LOADER_LOAD_ORIGINAL_CLASS_MIRROR = new MethodHandle() {
        HologramClassLoader loader = null;
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
    
    public Object invoke(ObjectMirror obj, Object ... args) {
        return invoke(obj, ThreadHolograph.currentThreadMirror(), args);
    }
	
    public Object invoke(ObjectMirror obj, ThreadMirror thread, Object ... args) {
        try {
            return invokeWithErrors(obj, thread, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object invokeWithErrors(ObjectMirror obj, ThreadMirror thread, Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        ClassMirror klass = obj.getClassMirror();
        VirtualMachineMirror vm = klass.getVM();
        final ClassMirror targetClass;
        try {
            targetClass = Reflection.classMirrorForName(vm, thread, getMethod().owner, false, klass.getLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        Type[] paramTypes = Type.getArgumentTypes(getMethod().desc);
        final ClassMirror[] paramClasses = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            try {
                paramClasses[i] = Reflection.classMirrorForType(vm, thread, paramTypes[i], false, klass.getLoader());
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        MethodMirror method = Reflection.withThread(thread, new Callable<MethodMirror>() {
            @Override
            public MethodMirror call() throws Exception {
                return HolographInternalUtils.getMethod(targetClass, getMethod().name, paramClasses);
            }
        });
                
        return method.invoke(thread, obj, args);
    }
}