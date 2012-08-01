package edu.ubc.mirrors.holographs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeConstructorMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class ClassHolograph extends WrappingClassMirror {

    private ClassMirror bytecodeMirror;
    
    public static ThreadLocal<ThreadMirror> currentThreadMirror = new ThreadLocal<ThreadMirror>();
    
    protected ClassHolograph(VirtualMachineHolograph vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }
    
    public ClassMirror getWrappedClassMirror() {
        return wrapped;
    }
    
    public static Class<?> getNativeStubsClass(String name) {
        String nativeStubsName = "edu.ubc.mirrors.raw.nativestubs." + name + "Stubs";
        try {
            return Class.forName(nativeStubsName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    public static String getIllegalNativeMethodMessage(String owner, org.objectweb.asm.commons.Method method) {
        if (owner.equals(Object.class.getName()) || owner.equals(Throwable.class.getName())) {
            return "Shared superclass";
        } else if (owner.startsWith("com.sun.java.swing.plaf.gtk")) { 
            return "GUI";
        } else if (owner.startsWith("com.sun.management")) {
            return "System";
        } else if (owner.startsWith("com.sun.media.sound")) {
            return "Media";
        } else if (owner.equals("sun.tracing.dtrace.JVM")) {
            return "Management";
        } else if (owner.startsWith("com.sun.demo.jvmti.hprof")) {
            return "Management";
        } else if (owner.startsWith("sun.management")) {
            return "Management";
        } else if (owner.startsWith("java.io")) {
            return "IO";
        } else if (owner.startsWith("java.nio")) {
            return "IO";
        } else if (owner.startsWith("sun.nio")) {
            return "IO";
        } else if (owner.startsWith("sun.print")) {
            return "Printers";
        } else if (owner.startsWith("sun.security.smartcardio")) {
            return "Drivers";
        } else if (owner.startsWith("sun.java2d")) {
            return "Graphics";
        } else if (owner.startsWith("com.apple.eawt")) {
            return "GUI";
        } else if (owner.startsWith("com.apple.eio")) {
            return "IO";
        } else if (owner.startsWith("com.sun.imageio.plugins.jpeg")) {
            return "Graphics";
        } else if (owner.startsWith("sun.dc.pr")) {
            return "Graphics";
        } else if (owner.startsWith("sun.font")) {
            return "Graphics";
        } else if (owner.startsWith("sun.awt")) {
            return "GUI";
        } else if (owner.startsWith("sun.lwawt")) {
            return "GUI";
        } else if (owner.startsWith("java.awt")) {
            return "GUI";
        } else if (owner.startsWith("apple.laf")) {
            return "GUI";
        } else if (owner.startsWith("com.apple.laf")) {
            return "GUI";
        } else if (owner.startsWith("sun.security")) {
            return "Security";
        } else if (owner.startsWith("java.net")) {
            return "Network";
        } else if (owner.startsWith("sun.net")) {
            return "Network";
        } else if (owner.startsWith("apple.applescript")) {
            return "System";
        } else if (owner.startsWith("apple.launcher")) {
            return "System";
        } else if (owner.startsWith("apple.security")) {
            return "System";
        } else if (owner.startsWith("sun.rmi")) {
            return "Network";
        } else if (owner.equals("java.lang.UNIXProcess")) {
            return "System";
        } else if (owner.equals("sun.misc.Perf")) {
            return "System";
        } else if (owner.equals("java.util.TimeZone")) {
            return "System";
        } else if (owner.equals("sun.misc.MessageUtils")) {
            return "IO";
        } else if (owner.startsWith("java.util.prefs")) {
            // TODO-RS: May come up in read-only mapped fs
            return "IO";
        } else if (owner.startsWith("java.lang.invoke")) {
            return "Java 7 Method Handles";
        } else if (owner.startsWith("com.apple.concurrent")) {
            return "System";
        } else if (owner.startsWith("com.apple.jobjc")) {
            return "System (???)";
        } else if (owner.startsWith("oracle.jrockit.jfr")) {
            return "Management";
        } else if (owner.equals("java.lang.ClassLoader$NativeLibrary")) {
            return "Native Libraries";
        } else if (owner.equals("java.lang.System") && method.getName().equals("mapLibraryName")) {
            return "Native Libraries";
        } else if (owner.equals("java.lang,System") && method.getName().equals("registerNatives")) {
            return "Class init";
        } else {
            return null;
        }
    }
    
    public static String getMissingNativeMethodMessage(String owner, org.objectweb.asm.commons.Method method) {
        if (owner.equals("sun.instrument.InstrumentationImpl")) {
            return "Class loading reflection";
        } else if (owner.equals("sun.misc.Unsafe")) {
            // TODO-RS: Some of these should be illegal. Absolute address memory access if nothing else.
            return "Direct memory-model reflection";
        } else if (owner.equals("sun.misc.Unsafe")) {
            // TODO-RS: Some of these should be illegal. Absolute address memory access if nothing else.
            return "Direct memory-model reflection";
        } else if (owner.equals("java.lang.reflect.Array")) {
            return "Reflection";
        } else if (owner.startsWith("java.util.zip")) {
            return "Read-only mapped FS";
        } else if (owner.equals("java.lang.StrictMath")) {
            return "Math";
        } else {
            return null;
        }
    }
        
    
    private ClassMirror getBytecodeMirror(ClassMirror klass) {
        if (klass instanceof ClassHolograph) {
            return ((ClassHolograph)klass).getBytecodeMirror();
        } else {
            return klass;
        }
    }

    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        // Just to check that the method exists.
//        ClassMirror[] bytecodeParamTypes = new ClassMirror[paramTypes.length];
//        for (int i = 0; i < paramTypes.length; i++) {
//            bytecodeParamTypes[i] = getBytecodeMirror(paramTypes[i]);
//        }
//        
//        getBytecodeMirror().getMethod(name, bytecodeParamTypes);

        return new MirageMethod(name, paramTypes);
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        // Add the extra implicit mirror parameter
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length + 1];
        for (int i = 0; i < paramTypes.length; i++) {
            mirageParamTypes[i] = getMirageClass(paramTypes[i], false);
        }
        mirageParamTypes[paramTypes.length] = InstanceMirror.class;
        
        Class<?> mirageClass = getMirageClass(this, true);
        Constructor<?> constructor = mirageClass.getDeclaredConstructor(mirageParamTypes);
        return new MirageConstructor(constructor);
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        Class<?> mirageClass = getMirageClass(this, true);
        Constructor<?>[] constructors = publicOnly ? mirageClass.getConstructors() : mirageClass.getDeclaredConstructors();
        List<ConstructorMirror> result = new ArrayList<ConstructorMirror>();
        for (Constructor<?> constructor : constructors) {
            result.add(new MirageConstructor(constructor));
        }
        return result;
    }
    
    @Override
    public VirtualMachineHolograph getVM() {
        return (VirtualMachineHolograph)super.getVM();
    }
    
    public ClassLoaderHolograph getLoader() {
        return (ClassLoaderHolograph)super.getLoader();
    }
    
    public static MirageClassLoader getMirageClassLoader(ClassMirror classMirror) {
        return getMirageClassLoader(classMirror.getVM(), classMirror.getLoader());
    }
    
    public static MirageClassLoader getMirageClassLoader(VirtualMachineMirror vm, ClassMirrorLoader loader) {
        return loader == null ? 
                ((VirtualMachineHolograph)vm).getMirageClassLoader() : 
                    ((ClassLoaderHolograph)loader).getMirageClassLoader();
    }
    
    public MirageClassLoader getMirageClassLoader() {
        return getMirageClassLoader(getVM(), getLoader());
    }
    
    public static Class<?> getMirageClass(ClassMirror classMirror, boolean impl) {
        try {
            return getMirageClassLoader(classMirror).getMirageClass(classMirror, impl);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Class<?> getMirageClass(boolean impl) {
        return getMirageClass(this, impl);
    }
    
    private class MirageMethod implements MethodMirror {

        private final String name;
        private final ClassMirror[] paramTypes;
        
        private Method mirageClassMethod;
        private boolean accessible = false;
        
        public MirageMethod(String name, ClassMirror[] paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes;
        }
        
        private void resolveMethod() {
            Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                mirageParamTypes[i] = getMirageClass(paramTypes[i], false);
            }
            Class<?> mirageClass = getMirageClass(true);
            try {
                mirageClassMethod = mirageClass.getDeclaredMethod(name, mirageParamTypes);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(name);
            }
            mirageClassMethod.setAccessible(accessible);
        }
        
        @Override
        public Object invoke(ThreadMirror thread, InstanceMirror obj, Object ... args) throws IllegalAccessException, InvocationTargetException {
            ThreadMirror original = currentThreadMirror.get();
            currentThreadMirror.set(thread);
            try {
                resolveMethod();
                
                Object[] mirageArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    mirageArgs[i] = makeMirage(args[i]);
                }
                Object mirageObj = makeMirage(obj);
                Object result = mirageClassMethod.invoke(mirageObj, mirageArgs);
                // Account for the fact that toString() has to return a real String here
                if (result instanceof String) {
                    return Reflection.makeString(vm, (String)result);
                } else {
                    return unwrapMirage(result);
                }
            } finally {
                currentThreadMirror.set(original);
            }
        }
        
        @Override
        public void setAccessible(boolean flag) {
            if (mirageClassMethod != null) {
                mirageClassMethod.setAccessible(flag);
            } else {
                accessible = flag;
            }
        }
    }
    
    private class MirageConstructor extends NativeConstructorMirror implements ConstructorMirror {

        private final Constructor<?> mirageClassConstructor;
        
        public MirageConstructor(Constructor<?> mirageClassConstructor) {
            super(mirageClassConstructor);
            this.mirageClassConstructor = mirageClassConstructor;
            this.mirageClassConstructor.setAccessible(true);
        }
        
        @Override
        public InstanceMirror newInstance(Object... args)
                throws InstantiationException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {

            // Add the extra implicit mirror parameter
            Class<?> classLoaderLiteral = mirageClassConstructor.getDeclaringClass();
            InstanceMirror mirror = ObjectMirage.newInstanceMirror(classLoaderLiteral, classLoaderLiteral.getName().replace('/', '.'));
            Object[] mirageArgs = new Object[args.length + 1];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = makeMirage(args[i]);
            }
            mirageArgs[args.length] = mirror;
            Object result = mirageClassConstructor.newInstance(mirageArgs);
            return (InstanceMirror)unwrapMirage(result);
        }
        
        @Override
        public ClassMirror getDeclaringClass() {
            return ClassHolograph.this;
        }
        
        @Override
        public List<ClassMirror> getParameterTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            Class<?>[] mirageParamTypes = mirageClassConstructor.getParameterTypes();
            // Skip the extra mirror argument
            for (int i = 0; i < mirageParamTypes.length - 1; i++) {
                result.add(getOriginalClassMirror(mirageParamTypes[i]));
            }
            return result;
        }
        
        @Override
        public List<ClassMirror> getExceptionTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            Class<?>[] mirageParamTypes = mirageClassConstructor.getExceptionTypes();
            for (Class<?> mirageParamType : mirageParamTypes) {
                result.add(getOriginalClassMirror(mirageParamType));
            }
            return result;
        }
        
        // TODO-RS: May need to translate annotations back to original classes too...
    }
    
    public static Object makeMirage(Object mirror) {
        if (mirror instanceof ObjectMirror) {
            ObjectMirror objectMirror = (ObjectMirror)mirror;
            return getMirageClassLoader(objectMirror.getClassMirror()).makeMirage(objectMirror);
        } else {
            return mirror;
        }
    }
    
    public static ClassMirror getOriginalClassMirror(Class<?> mirageValue) {
        MirageClassLoader loader = (MirageClassLoader)mirageValue.getClassLoader();
        return loader.loadOriginalClassMirror(MirageClassGenerator.getOriginalBinaryClassName(mirageValue.getName()));
    }

    public static Object unwrapMirage(Object mirage) {
        if (mirage instanceof Mirage) {
            return ((Mirage)mirage).getMirror();
        } else {
            return mirage;
        }
    }
    
    private ClassMirror getBytecodeMirror() {
        if (bytecodeMirror == null) {
            bytecodeMirror = getVM().getBytecodeClassMirror(this);
        }
        return bytecodeMirror;
    }

    @Override
    public byte[] getBytecode() {
        try {
            return super.getBytecode();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getBytecode();
        }
    }
    
    @Override
    public boolean isInterface() {
        try {
            return super.isInterface();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().isInterface();
        }
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        try {
            return super.getInterfaceMirrors();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getInterfaceMirrors();
        }
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        try {
            return super.getDeclaredFields();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getDeclaredFields();
        }
    }

    @Override
    public InstanceMirror newRawInstance() {
        if (Reflection.isAssignableFrom(getVM().findBootstrapClassMirror(ClassLoader.class.getName()), this)) {
            return newRawClassLoaderInstance();
        } else {
            return new FieldMapMirror(this);
        }
    }
    
    @Override
    public ClassMirrorLoader newRawClassLoaderInstance() {
        return vm.getWrappedClassLoaderMirror(new FieldMapClassMirrorLoader(this) {
            @Override
            public ClassMirror getClassMirror() {
                return getWrappedClassMirror();
            }
        });
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        return newArray(new int[] {size});
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        ClassMirror arrayClass = wrapped.getVM().getArrayClass(dims.length, wrapped);
        return new DirectArrayMirror(vm.getWrappedClassMirror(arrayClass), dims);
    }
    
    
}
