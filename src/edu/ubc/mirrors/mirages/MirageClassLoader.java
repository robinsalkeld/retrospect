package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalInternalClassName;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class MirageClassLoader extends ClassLoader {
        
    public static String traceClass = null;
    public static String traceDir = null;
    private final File myTraceDir;
    public static boolean debug = false;
    
    final ClassMirrorLoader classMirrorLoader;
    final MirageClassMirrorLoader mirageClassMirrorLoader;
    
    public ClassMirrorLoader getClassMirrorLoader() {
        return classMirrorLoader;
    }
    
    public ClassMirrorLoader getMirageClassMirrorLoader() {
        return mirageClassMirrorLoader;
    }
    
    public static final String CLASS_LOADER_LITERAL_NAME = "edu/ubc/mirrors/ClassLoaderLiteral";
    
    public MirageClassLoader(ClassLoader originalLoader, ClassMirrorLoader classMirrorLoader) {
        super(originalLoader);
        this.classMirrorLoader = classMirrorLoader;
        this.mirageClassMirrorLoader = new MirageClassMirrorLoader(new NativeClassMirrorLoader(this), classMirrorLoader);
        
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
        if (traceDir != null) {
            for (File dir : new File(traceDir + "/").listFiles()) {
    //            for (File f : dir.listFiles()) {
    //                f.delete();
    //            }
                dir.delete();
            }
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
        registerCommonClasses(ObjectMirage.class, ObjectMirror.class, FieldMirror.class,
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
        return loadClass(MirageClassGenerator.getMirageBinaryClassName(original.getName(), false));
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String internalName = name.replace('.', '/');
        if (internalName.equals(CLASS_LOADER_LITERAL_NAME)) {
            byte[] b = mirageClassMirrorLoader.classLoaderLiteralMirror.getBytecode();
            return defineClass(name, b);
        } else if (name.startsWith("miragearray")) {
            boolean isInterface = !name.startsWith("miragearrayimpl");
            
            Type originalType = Type.getObjectType(getOriginalInternalClassName(internalName));
            Type originalElementType = originalType.getElementType();
            
            ClassMirror superClassMirror = null;
            String superName = isInterface ? Type.getInternalName(Object.class) : Type.getInternalName(ObjectArrayMirage.class);
            List<String> interfaces = new ArrayList<String>();
            int access = Opcodes.ACC_PUBLIC | (isInterface ? Opcodes.ACC_INTERFACE : 0);
            
            // TODO-RS: miragearray(n).java.lang.Object should extend miragearray(n-1).java.lang.Object
            
            if (originalElementType.getSort() == Type.OBJECT || originalElementType.getSort() == Type.ARRAY) {
                String originalElement = originalElementType.getInternalName();
                int dims = originalType.getDimensions();
                ClassMirror originalClassMirror = classMirrorLoader.loadClassMirror(originalElement.replace('/', '.'));
                
                superClassMirror = originalClassMirror.getSuperClassMirror();
                
                if (isInterface) {
                    if (superClassMirror != null) { 
                        Type superType = MirageClassGenerator.makeArrayType(dims, Type.getObjectType(superClassMirror.getClassName().replace('.', '/'))); 
                        String superInterfaceName = MirageClassGenerator.getMirageType(superType).getInternalName(); 
                        interfaces.add(superInterfaceName);
                    }
                    
                    for (ClassMirror interfaceMirror : originalClassMirror.getInterfaceMirrors()) {
                        Type superType = MirageClassGenerator.makeArrayType(dims, Type.getObjectType(interfaceMirror.getClassName().replace('.', '/'))); 
                        String interfaceName = MirageClassGenerator.getMirageType(superType).getInternalName(); 
                        interfaces.add(interfaceName);
                    }
                } else {
                    interfaces.add("miragearray" + name.replace('.', '/').substring("miragearrayimpl".length()));
                }
            }
            
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            writer.visit(Opcodes.V1_1, access, internalName, null, superName, interfaces.toArray(new String[0]));

            // Generate thunk constructors
            if (!isInterface) {
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
            }
            byte[] b = writer.toByteArray();
            return defineClass(name, b);
        } else if (name.startsWith("mirage")) {
            String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(name);
            if (originalClassName.equals("org/jruby/RubyFixnum[]")) {
                int bp = 4;
            }
            ClassMirror classMirror = classMirrorLoader.loadClassMirror(originalClassName);
            byte[] b;
            try {
                b = MirageClassGenerator.generate(this, classMirror);
            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading bytecode from class mirror: " + originalClassName, e);
            }
            return defineClass(name, b);
        } else if (name.startsWith("native")) {
            String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(name);
            ClassMirror classMirror = classMirrorLoader.loadClassMirror(originalClassName);
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
            
//            if (name.equals(traceClass)) {
//                Verifier.main(new String[] {name});
//            }
        }
        Class<?> c = super.defineClass(name, b, 0, b.length);
        return c;
    }
    
    public Object makeMirage(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        final String internalClassName = mirror.getClassMirror().getClassName();
        
        if (internalClassName.equals("[Z")) {
            return new BooleanArrayMirage((BooleanArrayMirror)mirror);
        } else if (internalClassName.equals("[B")) {
            return new ByteArrayMirage((ByteArrayMirror)mirror);
        } else if (internalClassName.equals("[C")) {
            return new CharArrayMirage((CharArrayMirror)mirror);
        } else if (internalClassName.equals("[S")) {
            return new ShortArrayMirage((ShortArrayMirror)mirror);
        } else if (internalClassName.equals("[I")) {
            return new IntArrayMirage((IntArrayMirror)mirror);
        } else if (internalClassName.equals("[J")) {
            return new LongArrayMirage((LongArrayMirror)mirror);
        } else if (internalClassName.equals("[F")) {
            return new FloatArrayMirage((FloatArrayMirror)mirror);
        } else if (internalClassName.equals("[D")) {
            return new DoubleArrayMirage((DoubleArrayMirror)mirror);
        }
        
        try {
            final String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(internalClassName, true);
            final Class<?> mirageClass = loadClass(mirageClassName);
            final Constructor<?> c = mirageClass.getConstructor(mirror instanceof InstanceMirror ? Object.class : ObjectArrayMirror.class);
            return c.newInstance(mirror);
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