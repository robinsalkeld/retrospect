package edu.ubc.mirrors.mirages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.verifier.Verifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeClassGenerator;

public class MirageClassLoader extends ClassLoader {
        
    public static String traceClass = null;
    public static String traceDir = null;
    private final File myTraceDir;
    
    final ClassMirrorLoader classMirrorLoader;
    
    public ClassMirrorLoader getClassMirrorLoader() {
        return classMirrorLoader;
    }
    
    public static final String CLASS_LOADER_LITERAL_NAME = "edu/ubc/mirrors/ClassLoaderLiteral";
    
    public MirageClassLoader(ClassLoader originalLoader, ClassMirrorLoader classMirrorLoader) {
        super(originalLoader);
        this.classMirrorLoader = classMirrorLoader;
        
        if (traceDir != null) {
            myTraceDir = new File(traceDir/*, hashCode() + "/"*/);
            myTraceDir.mkdir();
            for (File f : myTraceDir.listFiles()) {
                f.delete();
            }
        } else {
            myTraceDir = null;
        }
    }
    
    public static void setTraceDir(String newTraceDir) {
        traceDir = newTraceDir;
        for (File dir : new File(traceDir + "/").listFiles()) {
//            for (File f : dir.listFiles()) {
//                f.delete();
//            }
            dir.delete();
        }
        
    }
    
    public ClassLoader getOriginalLoader() {
        return getParent();
    }
    
    public static final Map<String, Class<?>> COMMON_CLASSES = new HashMap<String, Class<?>>();
    private static void registerCommonClasses(Class<?> ... classes) {
        for (Class<?> c : classes) {
            COMMON_CLASSES.put(c.getName(), c);
        }
    }
    
    static {
        registerCommonClasses(Object.class, ObjectMirage.class, ObjectMirror.class, FieldMirror.class,
                FieldMapMirror.class,
                // Necessary because only subclasses of this can be thrown.
                // Probably need to introduce a new root subclass as with ObjectMirage.
                Throwable.class);
    }
    
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("loadClass: " + name);
        Class<?> c = super.loadClass(name, resolve);
//        System.out.println("Done loading class: " + name);
        return c;
    }
    
    public Class<?> loadMirageClass(Class<?> original) throws ClassNotFoundException {
        return loadClass(MirageClassGenerator.getMirageBinaryClassName(original.getName()));
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(CLASS_LOADER_LITERAL_NAME.replace('/', '.'))) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
            writer.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, CLASS_LOADER_LITERAL_NAME, null, "java/lang/Object", null);
            byte[] b = writer.toByteArray();
            return defineClass(name, b, 0, b.length);
        } else if (name.startsWith("miragearray")) {
            String originalName = MirageClassGenerator.getOriginalClassName(name);
            String originalElement = Type.getObjectType(originalName).getElementType().getInternalName();
            ClassMirror<?> originalClassMirror = classMirrorLoader.loadClassMirror(originalElement);
            ClassMirror<?> superClassMirror = originalClassMirror.getSuperClassMirror();
            String superName = superClassMirror != null ? 
                    MirageClassGenerator.getMirageInternalClassName("[L" + superClassMirror.getClassName().replace('.', '/') + ";") : 
                    Type.getInternalName(ObjectArrayMirage.class);
            
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
            writer.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, name.replace('.', '/'), null, superName, null);

            // Generate thunk constructors
            String initDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectArrayMirror.class));
            MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", initDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", initDesc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            
            initDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE);
            mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", initDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", initDesc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            
            byte[] b = writer.toByteArray();
            return defineClass(name, b);
        } else if (name.startsWith("mirage")) {
            String originalClassName = MirageClassGenerator.getOriginalClassName(name);
            ClassMirror<?> classMirror = classMirrorLoader.loadClassMirror(originalClassName);
            byte[] b;
            try {
                b = MirageClassGenerator.generate(classMirror);
            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading bytecode from class mirror: " + originalClassName, e);
            }
            return defineClass(name, b);
        } else if (name.startsWith("native")) {
            String originalClassName = MirageClassGenerator.getOriginalClassName(name);
            ClassMirror<?> classMirror = classMirrorLoader.loadClassMirror(originalClassName);
            byte[] b;
            try {
                b = NativeClassGenerator.generate(classMirror);
            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading bytecode from class mirror: " + originalClassName, e);
            }
            return defineClass(name, b);
        } else {
            throw new ClassNotFoundException();
        }
    }
    
    protected Class<?> defineClass(String name, byte[] b) {
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        try {
            JavaClass javaClass = new ClassParser(is, "").parse();
            Repository.addClass(javaClass);
        } catch (ClassFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (myTraceDir != null) {
            File file = new File(myTraceDir, name + ".class");
            try {
                OutputStream classFile = new FileOutputStream(file);
                classFile.write(b);
                classFile.flush();
                classFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            File txtFile = new File(myTraceDir, name + ".txt");
            try {
                PrintWriter textFileWriter = new PrintWriter(txtFile);
                new ClassReader(b).accept(new TraceClassVisitor(null, textFileWriter), 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            if (name.equals(traceClass)) {
                try {
                    CheckClassAdapter.verify(new ClassReader(new FileInputStream(file)), this, false, new PrintWriter(System.out));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                Verifier.main(new String[] {name});
            }
        }
//        System.out.println("defining class: " + name);
        return super.defineClass(name, b, 0, b.length);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T makeMirage(ObjectMirror<T> mirror) {
        if (mirror == null) {
            return null;
        }
        final String internalClassName = mirror.getClassMirror().getClassName();
        try {
            if (mirror instanceof InstanceMirror || mirror instanceof ObjectArrayMirror) {
                final String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(internalClassName);
                final Class<?> mirageClass = loadClass(mirageClassName);
                final Constructor<?> c = mirageClass.getConstructor(mirror instanceof InstanceMirror ? InstanceMirror.class : ObjectArrayMirror.class);
                return (T)c.newInstance(mirror);
            } else {
                return (T)mirror;
            }
        } catch (NoSuchMethodException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + internalClassName);
            error.initCause(e);
            throw error;
        } catch (IllegalAccessException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + internalClassName);
            error.initCause(e);
            throw error;
        } catch (InstantiationException e) {
            InternalError error = new InternalError("Result of ObjectMirage#getMirrorClass() is abstract: " + internalClassName);
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            InternalError error = new InternalError("Error on instantiating Mirage class: " + internalClassName);
            error.initCause(e);
            throw error;
        } catch (ClassNotFoundException e) {
            InternalError error = new InternalError("Error on loading Mirage class: " + internalClassName);
            error.initCause(e);
            throw error;
        }
    }
}