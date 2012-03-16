package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalInternalClassName;

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

import org.objectweb.asm.ClassReader;
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
    public final File myTraceDir;
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
            ClassMirror classMirror = classMirrorLoader.loadClassMirror(originalClassName);
            byte[] b;
            try {
                b = MirageClassGenerator.generate(this, classMirror);
            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading bytecode from class mirror: " + originalClassName, e);
            } catch (Throwable e) {
                String target = name;
                if (MirageMethodGenerator.activeMethod != null) {
                    target += "#" + MirageMethodGenerator.activeMethod;
                }
                throw new RuntimeException("Error caught while generating bytecode for " + target, e);
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
        try {
            if (myTraceDir != null) {
                File file = new File(myTraceDir, name + ".class");
                OutputStream classFile = new FileOutputStream(file);
                classFile.write(b);
                classFile.flush();
                classFile.close();
            }
            Class<?> c = super.defineClass(name, b, 0, b.length);
            return c;
        } catch (Throwable e) {
            throw new RuntimeException("Error caught while defining class " + name, e);
        }
    }
    
    private final Map<ObjectMirror, Object> mirages = new HashMap<ObjectMirror, Object>();
    
    public Object makeMirage(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        Object mirage = mirages.get(mirror);
        if (mirage != null) {
            return mirage;
        }
        
        final String internalClassName = mirror.getClassMirror().getClassName();
        
        if (internalClassName.equals("[Z")) {
            mirage = new BooleanArrayMirage((BooleanArrayMirror)mirror);
        } else if (internalClassName.equals("[B")) {
            mirage = new ByteArrayMirage((ByteArrayMirror)mirror);
        } else if (internalClassName.equals("[C")) {
            mirage = new CharArrayMirage((CharArrayMirror)mirror);
        } else if (internalClassName.equals("[S")) {
            mirage = new ShortArrayMirage((ShortArrayMirror)mirror);
        } else if (internalClassName.equals("[I")) {
            mirage = new IntArrayMirage((IntArrayMirror)mirror);
        } else if (internalClassName.equals("[J")) {
            mirage = new LongArrayMirage((LongArrayMirror)mirror);
        } else if (internalClassName.equals("[F")) {
            mirage = new FloatArrayMirage((FloatArrayMirror)mirror);
        } else if (internalClassName.equals("[D")) {
            mirage = new DoubleArrayMirage((DoubleArrayMirror)mirror);
        } else {
        
            try {
                final String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(internalClassName, true);
                final Class<?> mirageClass = loadClass(mirageClassName);
                final Constructor<?> c = mirageClass.getConstructor(mirror instanceof InstanceMirror ? Object.class : ObjectArrayMirror.class);
                mirage = c.newInstance(mirror);
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
        
        mirages.put(mirror, mirage);
        return mirage;
    }
    
    public void invokeMirageMethod(Object mirage, String methodName, Object... args) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?> mirageClass = loadClass(getMirageBinaryClassName(mirage.getClass().getName(), false));
        final Class<?> mirageStringArray = Class.forName("[Ljava.lang.String;", true, mirageClass.getClassLoader());
        
        
        mirageClass.getDeclaredMethod(methodName, mirageStringArray).invoke(null, args);
    }
}