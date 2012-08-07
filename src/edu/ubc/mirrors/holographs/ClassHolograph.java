package edu.ubc.mirrors.holographs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import edu.ubc.mirrors.fieldmap.FieldMapThreadMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeConstructorMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class ClassHolograph extends WrappingClassMirror {

    private ClassMirror bytecodeMirror;
    
    public static ThreadLocal<ThreadMirror> currentThreadMirror = new ThreadLocal<ThreadMirror>();
    
    public static class MethodPattern {
        public final String className;
        private final Pattern classNamePattern;
        public final String methodName;
        private final Pattern methodNamePattern;
        public final String category;
        
        public MethodPattern(String line) {
            String[] pieces = line.split(",");
            className = pieces[0];
            classNamePattern = getPattern(className);
            methodName = pieces[1];
            methodNamePattern = getPattern(methodName);
            category = pieces[2];
        }
        
        private Pattern getPattern(String pattern) {
            if (pattern.isEmpty()) {
                return Pattern.compile(".*");
            } else {
                return Pattern.compile(pattern.replace(".", "\\.").replace("$", "\\$").replace("*", ".*"));
            }
        }
        
        public boolean matches(String owner, org.objectweb.asm.commons.Method method) {
            return classNamePattern.matcher(owner).matches() && methodNamePattern.matcher(method.getName()).matches();
        }
        
        public String getCategory() {
            return category;
        }
    }
    
    public static List<MethodPattern> illegalMethodPatterns = new ArrayList<MethodPattern>();
    static {
        InputStream in = ClassHolograph.class.getClassLoader().getResourceAsStream("edu/ubc/mirrors/holographs/IllegalNativeMethods.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        try {
            // Skip the header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                illegalMethodPatterns.add(new MethodPattern(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static List<MethodPattern> missingMethodPatterns = new ArrayList<MethodPattern>();
    static {
        InputStream in = ClassHolograph.class.getClassLoader().getResourceAsStream("edu/ubc/mirrors/holographs/MissingNativeMethods.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        try {
            // Skip the header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                missingMethodPatterns.add(new MethodPattern(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
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
        for (MethodPattern pattern : illegalMethodPatterns) {
            if (pattern.matches(owner, method)) {
                return pattern.category;
            }
        }
        return null;
    }
    
    public static String getMissingNativeMethodMessage(String owner, org.objectweb.asm.commons.Method method) {
        for (MethodPattern pattern : missingMethodPatterns) {
            if (pattern.matches(owner, method)) {
                return pattern.category;
            }
        }
        return null;
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
            return vm.getWrappedClassLoaderMirror(new FieldMapClassMirrorLoader(this) {
                @Override
                public ClassMirror getClassMirror() {
                    return getWrappedClassMirror();
                }
            });
        } else if (Reflection.isAssignableFrom(getVM().findBootstrapClassMirror(Thread.class.getName()), this)) {
            return new FieldMapThreadMirror(this);
        } else {
            return new FieldMapMirror(this);
        }
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
