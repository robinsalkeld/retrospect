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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

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
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.test.Breakpoint;

public class MirageClassLoader extends ClassLoader {
    public static String traceClass = null;
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
    
    public MirageClassLoader(VirtualMachineHolograph vm, ClassMirrorLoader originalLoader) {
        super(MirageClassLoader.class.getClassLoader());
        this.vm = vm;
        this.originalLoader = originalLoader;
        this.mirageClassMirrorLoader = new MirageClassMirrorLoader(vm.getMirageVM(), new NativeClassMirrorLoader(getParent()), originalLoader);
    }
    
    public File createClassFile(int index, String internalName) {
        File classFile = new File(new File(vm.getBytecodeCacheDir(), index + ""), internalName);
        createDirRecursive(classFile.getParentFile());
        return classFile;
    }
    
    private static void createDirRecursive(File dir) {
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
        return HolographInternalUtils.loadClassMirrorInternal(vm, originalLoader, originalClassName);
    }
    
    public ClassMirror loadOriginalClassMirror(Type originalType) {
        return HolographInternalUtils.classMirrorForType(vm, ThreadHolograph.currentThreadMirror(), originalType, false, originalLoader);
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
        
        if (debug) {
            System.out.println("Defining class " + classMirror.getClassName());
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
    
    protected Class<?> defineDynamicClass(String name, byte[] b) {
        try {
            return defineClass(name, b, 0, b.length);
        } catch (Throwable e) {
            throw new RuntimeException("Error caught while defining class " + name, e);
        }
    }
    
    private final Map<ObjectMirror, Mirage> mirages = new WeakHashMap<ObjectMirror, Mirage>();
    
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
        
        ClassMirror classMirrorForCacheKey = mirageClassMirror.getOriginal();
        while (classMirrorForCacheKey.isArray()) {
            classMirrorForCacheKey = classMirrorForCacheKey.getComponentClassMirror();
        }
        // This will be null for primitive classes
        byte[] originalBytecode = classMirrorForCacheKey.getBytecode();
        String originalInternalName = MirageClassGenerator.getOriginalInternalClassName(classMirrorForCacheKey.getClassName().replace('.', '/'));
        int cacheIndex = 0;
        if (vm.getBytecodeCacheDir() != null) {
            cacheIndex = findCacheIndex(originalInternalName, originalBytecode);
            byte[] result = readFromBytecodeCache(cacheIndex, internalName);
            if (result != null) {
//                new ClassReader(result).accept(new ClassVisitor(Opcodes.ASM4) {}, null, 0);
                return result;
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
            byte[] result = generateBytecode(cacheIndex, mirageClassMirror);
            
            if (vm.getBytecodeCacheDir() != null) {
        	writeToBytecodeCache(cacheIndex, internalName, result);
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
    
    private int findCacheIndex(String className, byte[] bytecode) {
        int cacheIndex = 0;
        if (bytecode == null) {
            return cacheIndex;
        }
        byte[] cacheKey = null;
        while ((cacheKey = readFromBytecodeCache(cacheIndex, className)) != null) {
            if (Arrays.equals(bytecode, cacheKey)) {
                break;
            }
            cacheIndex++;
        }
        if (cacheKey == null) {
            writeToBytecodeCache(cacheIndex, className, bytecode);
        }
        return cacheIndex;
    }
    
    private byte[] readFromBytecodeCache(int cacheIndex, String className) {
	try {
	    File classFile = createClassFile(cacheIndex, className + ".class");
	    if (classFile.exists()) {
		return NativeClassMirror.readFully(new FileInputStream(classFile));
	    } else {
		return null;
	    }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    
    private void writeToBytecodeCache(int cacheIndex, String className, byte[] bytecode) {
	try {
	    OutputStream classFile = new FileOutputStream(createClassFile(cacheIndex, className + ".class"));
            classFile.write(bytecode);
            classFile.flush();
            classFile.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    
    public static void printIndent() {
        for (int i = 0; i < timerStack.size(); i++) {
            System.out.print("  ");
        }
    }
    
    public byte[] generateBytecode(int cacheIndex, MirageClassMirror mirageClassMirror) {
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
        if (vm.getBytecodeCacheDir() != null) {
            File txtFile = createClassFile(cacheIndex, internalName + ".txt");
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
        if (vm.getBytecodeCacheDir() != null) {
            File txtFile = createClassFile(cacheIndex, internalName + ".afterframes.txt");
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
        if (vm.getBytecodeCacheDir() != null) {
            File txtFile = createClassFile(cacheIndex, internalName + ".original.txt");
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
        try {
	    // Reading a non-constant field forces class initialization
            mirageClass.getField("classMirror").get(null);
        } catch (IllegalAccessException e) {
            throw new InternalError();
        } catch (NoSuchFieldException e) {
            // Ignore - not a dynamically generated class
        }
    }
}