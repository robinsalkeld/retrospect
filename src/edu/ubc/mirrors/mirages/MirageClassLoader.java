package edu.ubc.mirrors.mirages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.test.Breakpoint;

public class MirageClassLoader extends ClassLoader {
    public static String traceClass = null;
    public static File traceDir;
    static {
        String path = System.getProperty("edu.ubc.mirrors.mirages.tracepath");
        if (path != null) {
            traceDir = new File(path);
        }
    }
    public final File myTraceDir;
    public static boolean debug = Boolean.getBoolean("edu.ubc.mirrors.mirages.debug");
    public static boolean preverify = Boolean.getBoolean("edu.ubc.mirrors.mirages.preverify");
    
    private static Stack<Stopwatch> timerStack = new Stack<Stopwatch>();
    private static Map<String, Stopwatch> timers = new HashMap<String, Stopwatch>();
    
    public static void printStats() {
        long allTime = 0;
        for (Stopwatch sw : timers.values()) {
            allTime += sw.total();
        }
        System.out.println("Count: " + timers.size());
        System.out.println("Total: " + allTime);
        if (!timers.isEmpty()) {
            System.out.println("Average: " + (allTime / timers.size()));
        }
    }
    
    final VirtualMachineHolograph vm;
    // May be null
    final ClassMirrorLoader originalLoader;
    
    private final Set<String> inFlightClasses = new HashSet<String>();
    
    final MirageClassMirrorLoader mirageClassMirrorLoader;
    
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    public ClassMirrorLoader getOriginalClassMirrorLoader() {
        return originalLoader;
    }
    
    public static final String CLASS_LOADER_LITERAL_NAME = "edu/ubc/mirrors/ClassLoaderLiteral";
    
    public MirageClassLoader(VirtualMachineHolograph vm, ClassMirrorLoader originalLoader) {
        super(MirageClassLoader.class.getClassLoader());
        this.vm = vm;
        this.originalLoader = originalLoader;
        this.mirageClassMirrorLoader = new MirageClassMirrorLoader(vm.getMirageVM(), new NativeClassMirrorLoader(getParent()), originalLoader);

        if (traceDir != null) {
            String loaderID = originalLoader == null ? "0" : String.valueOf(originalLoader.hashCode());
            myTraceDir = new File(traceDir, loaderID);
            myTraceDir.mkdir();
        } else {
            myTraceDir = null;
        }
    }
    
    public File createClassFile(String internalName) {
        File classFile = new File(myTraceDir, internalName);
        createDirRecursive(classFile.getParentFile());
        return classFile;
    }
    
    private void createDirRecursive(File dir) {
        if (!dir.exists()) {
            File parent = dir.getParentFile();
            if (parent == null) {
                throw new IllegalArgumentException("Cannot create a root");
            }
            createDirRecursive(parent);
            dir.mkdir();
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
    
    public boolean isCommonClassName(String className) {
        return COMMON_CLASSES.containsKey(className);
    }
    
    static {
        registerCommonClasses(
                // Every class must ultimately extend this.
                Object.class,
                // Necessary because only subclasses of this can be thrown.
                Throwable.class);
    }
    
    public ClassMirror loadOriginalClassMirror(String originalClassName) {
        try {
            return Reflection.classMirrorForName(vm, ThreadHolograph.currentThreadMirror(), originalClassName, false, originalLoader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(originalClassName);
        }
    }
    
    public ClassMirror loadOriginalClassMirror(Type originalType) {
        try {
            return Reflection.classMirrorForType(vm, ThreadHolograph.currentThreadMirror(), originalType, false, originalLoader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(originalType.getInternalName());
        }
    }
    
    public Class<?> getMirageClass(ClassMirror classMirror, boolean impl) throws ClassNotFoundException {
        if (classMirror.isPrimitive()) {
            return NativeVirtualMachineMirror.getNativePrimitiveClass(classMirror.getClassName());
        }
        
        MirageClassMirror mirageMirror = new MirageClassMirror(vm.getMirageVM(), classMirror, impl);
        String name = mirageMirror.getClassName();
        
        if (!name.startsWith("mirage")) {
            return super.loadClass(name);
        }
        
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        
        byte[] b;
        try {
            b = mirageMirror.getBytecode();
        } catch (Throwable e) {
            String target = name;
            if (MirageMethodGenerator.activeMethod != null) {
                target += "#" + MirageMethodGenerator.activeMethod;
            }
            throw new RuntimeException("Error caught while generating bytecode for " + target, e);
        }
        return defineDynamicClass(name, b);
    }
    
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        
        if (name.startsWith("mirage")) {
            String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(name);
            ClassMirror classMirror = loadOriginalClassMirror(originalClassName);
            Class<?> c = ClassHolograph.getMirageClass(classMirror, MirageClassGenerator.isImplementationClass(name));
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } else {
            return super.loadClass(name, resolve);
        }
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.replace('.', '/').equals(CLASS_LOADER_LITERAL_NAME)) {
            byte[] b = mirageClassMirrorLoader.classLoaderLiteralMirror.getBytecode();
            return defineDynamicClass(name, b);
        } else {
            return super.findClass(name);
        }
    }
    
    protected Class<?> defineDynamicClass(String name, byte[] b) {
        try {
            return defineClass(name, b, 0, b.length);
        } catch (Throwable e) {
            throw new RuntimeException("Error caught while defining class " + name, e);
        }
    }
    
    private final Map<ObjectMirror, Mirage> mirages = new HashMap<ObjectMirror, Mirage>();
    
    public Mirage makeMirage(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        Mirage mirage = mirages.get(mirror);
        if (mirage != null) {
            return mirage;
        }
        
        final String internalClassName = mirror.getClassMirror().getClassName().replace('.', '/');
        
        if (internalClassName.equals("java/util/AbstractMap")) {
            Breakpoint.bp();
        }
        
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
            final String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(internalClassName, true);
            try {
                final Class<?> mirageClass = loadClass(mirageClassName);
                final Constructor<?> c = mirageClass.getConstructor(mirror instanceof InstanceMirror ? Object.class : ObjectArrayMirror.class);
                mirage = (Mirage)c.newInstance(mirror);
            } catch (NoSuchMethodException e) {
                InternalError error = new InternalError("Mirage class constructor not accessible: " + mirageClassName);
                error.initCause(e);
                throw error;
            } catch (IllegalAccessException e) {
                InternalError error = new InternalError("Mirage class constructor not accessible: " + mirageClassName);
                error.initCause(e);
                throw error;
            } catch (InstantiationException e) {
                InternalError error = new InternalError("Result of ObjectMirage#getMirrorClass() is abstract: " + mirageClassName);
                error.initCause(e);
                throw error;
            } catch (InvocationTargetException e) {
                InternalError error = new InternalError("Error on instantiating Mirage class: " + mirageClassName);
                error.initCause(e);
                throw error;
            } catch (ClassNotFoundException e) {
                InternalError error = new InternalError("Error on loading Mirage class: " + mirageClassName);
                error.initCause(e);
                throw error;
            }
        }
        
        return mirage;
    }
    
    public void registerMirage(Mirage mirage) {
        mirages.put(mirage.getMirror(), mirage);
    }
    
    public byte[] getBytecode(MirageClassMirror mirageClassMirror) {
        String name = mirageClassMirror.getClassName();
        String internalName = name.replace('.', '/');
        if (myTraceDir != null) {
            File classFile = createClassFile(internalName + ".class");
            if (classFile.exists()) {
                try {
                    return NativeClassMirror.readFully(new FileInputStream(classFile));
                } catch (Throwable e) {
                    throw new RuntimeException("Error caught while using cached class definition " + name, e);
                }
            }
        }
        
        // Similar check to what the VM does - we can also get into infinite recursion
        // if the code generation is circular.
        if (inFlightClasses.contains(name)) {
            throw new ClassCircularityError(name);
        }
        
        if (debug) {
            printIndent();
            System.out.println("Generating bytecode for class: " + name);
        }
        
        if (!timerStack.isEmpty()) {
            timerStack.peek().stop();
        }
        Stopwatch sw = new Stopwatch();
        timers.put(name, sw);
        timerStack.push(sw);
        sw.start();
        
        inFlightClasses.add(name);
        try {
            byte[] result = generateBytecode(mirageClassMirror);
            
            if (myTraceDir != null) {
                File file = createClassFile(name.replace('.', '/') + ".class");
                OutputStream classFile;
                try {
                    classFile = new FileOutputStream(file);
                    classFile.write(result);
                    classFile.flush();
                    classFile.close();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
            
            long time = sw.stop();
            timerStack.pop();
            if (!timerStack.isEmpty()) {
                timerStack.peek().start();
            }
            if (debug) {
                printIndent();
                System.out.println("Generated bytecode for class " + name + " in " + time + " ms");
            }
            
            return result;
        } finally {
            inFlightClasses.remove(name);
        }
    }
    
    public static void printIndent() {
        for (int i = 0; i < timerStack.size(); i++) {
            System.out.print("  ");
        }
    }
    
    public byte[] generateBytecode(MirageClassMirror mirageClassMirror) {
        if (mirageClassMirror.getOriginal().isArray()) {
            return MirageClassGenerator.generateArray(this, mirageClassMirror.getOriginal(), !mirageClassMirror.isImplementationClass());
        }
        
        String internalName = mirageClassMirror.getClassName().replace('.', '/');
        
        ClassMirror original = mirageClassMirror.getOriginal();
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        if (preverify) {
            visitor = new FrameAnalyzerAdaptor(original.getVM(), original.getLoader(), visitor, false, true);
        }
        if (myTraceDir != null) {
            File txtFile = createClassFile(internalName + ".txt");
            PrintWriter textFileWriter;
            try {
                textFileWriter = new PrintWriter(txtFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            visitor = new TraceClassVisitor(visitor, textFileWriter);
        }
        visitor = new MirageClassGenerator(original, visitor);
        visitor = new RemappingClassAdapter(visitor, MirageClassGenerator.REMAPPER);
        if (myTraceDir != null) {
            File txtFile = createClassFile(internalName + ".afterframes.txt");
            PrintWriter textFileWriter;
            try {
                textFileWriter = new PrintWriter(txtFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ClassVisitor traceVisitor = new TraceClassVisitor(null, textFileWriter);
            ClassVisitor frameGenerator = new FrameAnalyzerAdaptor(original.getVM(), original.getLoader(), traceVisitor, true, false);
            byte[] bytecode = mirageClassMirror.getOriginal().getBytecode();
            if (bytecode == null) {
                mirageClassMirror.getOriginal().getBytecode();
            }
            new ClassReader(bytecode).accept(frameGenerator, ClassReader.EXPAND_FRAMES);
        }
        visitor = new FrameAnalyzerAdaptor(original.getVM(), original.getLoader(), visitor, true, false);
        if (myTraceDir != null) {
            File txtFile = createClassFile(internalName + ".original.txt");
            PrintWriter textFileWriter;
            try {
                textFileWriter = new PrintWriter(txtFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            visitor = new TraceClassVisitor(visitor, textFileWriter);
        }
        ClassReader reader = new ClassReader(original.getBytecode());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    
    @Override
    public String toString() {
        return "MirageClassLoader: " + originalLoader;
    }

    public static void initializeClassMirror(ClassHolograph klass) {
        Class<?> mirageClass = klass.getMirageClass(true);
        if (!mirageClass.isInterface()) {
            try {
        	// Reading a non-constant field forces class initialization
                mirageClass.getField("classMirror").get(null);
            } catch (IllegalAccessException e) {
                throw new InternalError();
            } catch (NoSuchFieldException e) {
                throw new InternalError();
            }
        }
    }
}