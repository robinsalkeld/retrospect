package edu.ubc.mirrors.holograms;

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
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holograms.BooleanArrayHologram;
import edu.ubc.mirrors.holograms.ByteArrayHologram;
import edu.ubc.mirrors.holograms.CharArrayHologram;
import edu.ubc.mirrors.holograms.DoubleArrayHologram;
import edu.ubc.mirrors.holograms.FloatArrayHologram;
import edu.ubc.mirrors.holograms.FrameAnalyzerAdaptor;
import edu.ubc.mirrors.holograms.IntArrayHologram;
import edu.ubc.mirrors.holograms.LongArrayHologram;
import edu.ubc.mirrors.holograms.Hologram;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.HologramClassMirror;
import edu.ubc.mirrors.holograms.HologramClassLoaderMirror;
import edu.ubc.mirrors.holograms.HologramMethodGenerator;
import edu.ubc.mirrors.holograms.ShortArrayHologram;
import edu.ubc.mirrors.holograms.Stopwatch;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class HologramClassLoader extends ClassLoader {
    public static String traceClass = null;
    public static boolean debug = Boolean.getBoolean("edu.ubc.mirrors.holograms.debug");
    public static boolean preverify = Boolean.getBoolean("edu.ubc.mirrors.holograms.preverify");
    
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
    
    final HologramClassLoaderMirror hologramClassMirrorLoader;
    
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    public ClassMirrorLoader getOriginalClassMirrorLoader() {
        return originalLoader;
    }
    
    public HologramClassLoader(VirtualMachineHolograph vm, ClassMirrorLoader originalLoader) {
        super(HologramClassLoader.class.getClassLoader());
        this.vm = vm;
        this.originalLoader = originalLoader;
        this.hologramClassMirrorLoader = new HologramClassLoaderMirror(vm.getHologramVM(), new NativeClassMirrorLoader(getParent()), originalLoader);
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
    
    public Class<?> getHologramClass(ClassMirror classMirror, boolean impl) throws ClassNotFoundException {
        if (classMirror.isPrimitive()) {
            return NativeVirtualMachineMirror.getNativePrimitiveClass(classMirror.getClassName());
        }
        
        HologramClassMirror hologramMirror = new HologramClassMirror(vm.getHologramVM(), classMirror, impl);
        String name = hologramMirror.getClassName();
        
        if (!name.startsWith("hologram")) {
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
            b = hologramMirror.getBytecode();
        } catch (Throwable e) {
            String target = name;
            if (HologramMethodGenerator.activeMethod != null) {
                target += "#" + HologramMethodGenerator.activeMethod;
            }
            throw new RuntimeException("Error caught while generating bytecode for " + target, e);
        }
        return defineDynamicClass(name, b);
    }
    
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        
        // Specialized logic for loading hologram classes - we don't want to delegate as usual because
        // I'm not reproducing the class loader hierarchy. Instead just get the HologramClassLoader that
        // corresponds to the ClassLoaderMirror that defined the original class and get it to load
        // the hologram class directly.
        if (name.startsWith("hologram")) {
            String originalClassName = HologramClassGenerator.getOriginalBinaryClassName(name);
            ClassMirror classMirror = loadOriginalClassMirror(originalClassName);
            c = ClassHolograph.getHologramClass(classMirror, HologramClassGenerator.isImplementationClass(name));
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
    
    private final Map<ObjectMirror, Hologram> holograms = new WeakHashMap<ObjectMirror, Hologram>();
    
    public Hologram makeHologram(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        Hologram hologram = holograms.get(mirror);
        if (hologram != null) {
            return hologram;
        }
        
        final String signature = mirror.getClassMirror().getSignature();
        
        if (signature.equals("[Z")) {
            hologram = new BooleanArrayHologram((BooleanArrayMirror)mirror);
        } else if (signature.equals("[B")) {
            hologram = new ByteArrayHologram((ByteArrayMirror)mirror);
        } else if (signature.equals("[C")) {
            hologram = new CharArrayHologram((CharArrayMirror)mirror);
        } else if (signature.equals("[S")) {
            hologram = new ShortArrayHologram((ShortArrayMirror)mirror);
        } else if (signature.equals("[I")) {
            hologram = new IntArrayHologram((IntArrayMirror)mirror);
        } else if (signature.equals("[J")) {
            hologram = new LongArrayHologram((LongArrayMirror)mirror);
        } else if (signature.equals("[F")) {
            hologram = new FloatArrayHologram((FloatArrayMirror)mirror);
        } else if (signature.equals("[D")) {
            hologram = new DoubleArrayHologram((DoubleArrayMirror)mirror);
        } else {
            final String hologramClassName = HologramClassGenerator.getHologramBinaryClassName(mirror.getClassMirror().getClassName(), true);
            try {
                final Class<?> hologramClass = loadClass(hologramClassName);
                final Constructor<?> c = hologramClass.getConstructor(mirror instanceof InstanceMirror ? Object.class : ObjectArrayMirror.class);
                hologram = (Hologram)c.newInstance(mirror);
            } catch (NoSuchMethodException e) {
                InternalError error = new InternalError("Hologram class constructor not accessible: " + hologramClassName);
                error.initCause(e);
                throw error;
            } catch (IllegalAccessException e) {
                InternalError error = new InternalError("Hologram class constructor not accessible: " + hologramClassName);
                error.initCause(e);
                throw error;
            } catch (InstantiationException e) {
                InternalError error = new InternalError("Result of ObjectHologram#getMirrorClass() is abstract: " + hologramClassName);
                error.initCause(e);
                throw error;
            } catch (InvocationTargetException e) {
                InternalError error = new InternalError("Error on instantiating Hologram class: " + hologramClassName);
                error.initCause(e);
                throw error;
            } catch (ClassNotFoundException e) {
                InternalError error = new InternalError("Error on loading Hologram class: " + hologramClassName);
                error.initCause(e);
                throw error;
            }
        }
        
        return hologram;
    }
    
    public void registerHologram(Hologram hologram) {
        holograms.put(hologram.getMirror(), hologram);
    }
    
    public byte[] getBytecode(HologramClassMirror hologramClassMirror) {
        String name = hologramClassMirror.getClassName();
        String internalName = name.replace('.', '/');
        
        ClassMirror classMirrorForCacheKey = hologramClassMirror.getOriginal();
        while (classMirrorForCacheKey.isArray()) {
            classMirrorForCacheKey = classMirrorForCacheKey.getComponentClassMirror();
        }
        // This will be null for primitive classes
        byte[] originalBytecode = classMirrorForCacheKey.getBytecode();
        String originalInternalName = HologramClassGenerator.getOriginalInternalClassName(classMirrorForCacheKey.getClassName().replace('.', '/'));
        int cacheIndex = 0;
        if (vm.getBytecodeCacheDir() != null) {
            cacheIndex = findCacheIndex(originalInternalName, originalBytecode);
            byte[] result = readFromBytecodeCache(cacheIndex, internalName);
            if (result != null) {
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
            byte[] result = generateBytecode(cacheIndex, hologramClassMirror);
            
            if (vm.getBytecodeCacheDir() != null) {
        	writeToBytecodeCache(cacheIndex, internalName, result);

                // Optimization for heap dumps: let the underlying VM cache the location of bytecode,
                // since it has stable, persistent object identifiers.
                ClassMirror wrappedClass = ((ClassHolograph)hologramClassMirror.getOriginal()).getWrappedClassMirror();
                if (wrappedClass instanceof HeapDumpClassMirror) {
                    File originalBytecodeLocation = createClassFile(cacheIndex, originalInternalName + ".class");
                    ((HeapDumpClassMirror)wrappedClass).bytecodeLocated(originalBytecodeLocation);
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
    
    public byte[] generateBytecode(int cacheIndex, HologramClassMirror hologramClassMirror) {
        if (hologramClassMirror.getOriginal().isArray()) {
            return HologramClassGenerator.generateArray(this, hologramClassMirror.getOriginal(), !hologramClassMirror.isImplementationClass());
        }
        
        String internalName = hologramClassMirror.getClassName().replace('.', '/');
        
        ClassMirror original = hologramClassMirror.getOriginal();
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
        visitor = new HologramClassGenerator(original, visitor);
        visitor = new RemappingClassAdapter(visitor, HologramClassGenerator.REMAPPER);
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
            byte[] bytecode = hologramClassMirror.getOriginal().getBytecode();
            if (bytecode == null) {
                hologramClassMirror.getOriginal().getBytecode();
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
        return "HologramClassLoader: " + originalLoader;
    }
}