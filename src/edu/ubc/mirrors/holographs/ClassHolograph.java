package edu.ubc.mirrors.holographs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
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
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror.StaticsInfo;
import edu.ubc.mirrors.raw.NativeConstructorMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class ClassHolograph extends WrappingClassMirror {

    private ClassMirror bytecodeMirror;
    
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
        MethodMirror original;
        try {
            original = super.getMethod(name, paramTypes);
        } catch (UnsupportedOperationException e) {
            original = getBytecodeMirror().getMethod(name, paramTypes);
        }
        return new MirageMethod(original);
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

        private MethodMirror wrapped;
        private Method mirageClassMethod;
        private boolean accessible = false;
        
        public MirageMethod(MethodMirror wrapped) {
            this.wrapped = wrapped;
        }

        private void resolveMethod() {
            List<ClassMirror> paramTypes = getParameterTypes();
            Class<?>[] mirageParamTypes = new Class<?>[paramTypes.size()];
            for (int i = 0; i < mirageParamTypes.length; i++) {
                mirageParamTypes[i] = getMirageClass(paramTypes.get(i), false);
            }
            Class<?> mirageClass = getMirageClass(true);
            // Account for the fact that ObjectMirage is not actually the top of the type lattice
            if (getClassName().equals(Object.class.getName())) {
                mirageClass = Object.class;
            }
            try {
                mirageClassMethod = mirageClass.getDeclaredMethod(getName(), mirageParamTypes);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(getName());
            }
            mirageClassMethod.setAccessible(accessible);
        }
        
        @Override
        public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalAccessException, InvocationTargetException {
            if (thread == null) {
                throw new NullPointerException();
            }
            
            ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
            threadHolograph.enterHologramExecution();
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
            } catch (InvocationTargetException e) {
                cleanStackTrace(e);
                throw e;
            } finally {
                threadHolograph.enterHologramExecution();
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
        
        @Override
        public List<ClassMirror> getParameterTypes() {
            return wrapped.getParameterTypes();
        }
        
        @Override
        public ClassMirror getReturnType() {
            return wrapped.getReturnType();
        }

        @Override
        public String getName() {
            return wrapped.getName();
        }

        @Override
        public byte[] getRawAnnotations() {
            return wrapped.getRawAnnotations();
        }

        @Override
        public byte[] getRawParameterAnnotations() {
            return wrapped.getRawParameterAnnotations();
        }

        @Override
        public byte[] getRawAnnotationDefault() {
            return wrapped.getRawAnnotationDefault();
        }
        
        @Override
        public ClassMirror getDeclaringClass() {
            return wrapped.getDeclaringClass();
        }

        @Override
        public int getSlot() {
            return wrapped.getSlot();
        }

        @Override
        public int getModifiers() {
            return wrapped.getModifiers();
        }

        @Override
        public List<ClassMirror> getExceptionTypes() {
            return wrapped.getExceptionTypes();
        }

        @Override
        public String getSignature() {
            return wrapped.getSignature();
        }
    }
    
    private static void cleanStackTrace(Throwable t) {
        StackTraceElement[] stackTrace = t.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            stackTrace[i] = new StackTraceElement(
                    MirageClassGenerator.getOriginalBinaryClassName(stackTrace[i].getClassName()),
                    stackTrace[i].getMethodName(),
                    stackTrace[i].getFileName(),
                    stackTrace[i].getLineNumber());
        }
        t.setStackTrace(stackTrace);
        
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            cleanStackTrace(cause);
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
        public InstanceMirror newInstance(ThreadMirror thread, Object... args)
                throws InstantiationException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
            threadHolograph.enterHologramExecution();
            try {
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
            } catch (InvocationTargetException e) {
                cleanStackTrace(e);
                throw e;
            } finally {
                threadHolograph.exitHologramExecution();
            }
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
            if (wrapped instanceof DefinedClassMirror) {
                throw new IllegalStateException();
            } else {
                bytecodeMirror = getVM().getBytecodeClassMirror(this);
            }
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
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        try {
            return super.getDeclaredMethods(publicOnly);
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getDeclaredMethods(publicOnly);
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
    
    private Boolean initialized = null;
    
    @Override
    public boolean initialized() {
        try {
            return super.initialized();
        } catch (UnsupportedOperationException e) {
            if (initialized == null) {
                if (isArray()) {
                    initialized = true;
                } else {
                    initialized = inferInitialized();
                }
            }
            return initialized;
        }
    }

    private static final Set<String> idempotentClassInits = new HashSet<String>(Arrays.asList(
            Modifier.class.getName(),
            PatternSyntaxException.class.getName(),
            "org.eclipse.osgi.internal.permadmin.EquinoxSecurityManager",
            "java.lang.invoke.MethodHandleNatives",
            "sun.reflect.UnsafeStaticFieldAccessorImpl",
            // Hits illegal native methods anyway - this makes the classes untouchable
            "sun.nio.ch.NativeThread",
            "sun.reflect.ConstantPool",
            "java.lang.Compiler",
            "org.eclipse.swt.internal.cocoa.CGRect",
            // This one would be solved via inter-class analysis
            "org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor"));
    
    private boolean inferInitialized() {
        BytecodeClassMirror bytecodeClassMirror = (BytecodeClassMirror)getBytecodeMirror();
        
        // TODO-RS: Make this pluggable
        if (idempotentClassInits.contains(getClassName())) {
            return false;
        }
        
        // If there is no <clinit> method, then we don't care.
        StaticsInfo classInitInfo = bytecodeClassMirror.classInitInfo();
        if (classInitInfo == null) {
            return true;
        }
        
        // If the class has any instances the initialization must have run.
        if (!getInstances().isEmpty()) {
            return true;
        }
        
        // Examine all the static fields in the class.
        for (BytecodeClassMirror.StaticField bytecodeField : bytecodeClassMirror.getStaticFields().values()) {
            try {
                // Be sure to call the super version of getStaticField, because otherwise
                // we will end up in infinite recursion!
                FieldMirror heapdumpField = super.getStaticField(bytecodeField.getName());
                
                if (!hasDefaultValue(heapdumpField)) {
                    // If there are any static, non-constant fields with non-null values,
                    // then initialization must have occurred since it has to before
                    // any static fields can be set.
                    if (!isConstantField(bytecodeField)) {
                        return true;
                    }
                } else {
                    // If there are any static fields with null/0 values,
                    // and the <clinit> method has the effect of setting non-default values,
                    // then initialization must not have occurred.
                    if (!classInitInfo.isDefault(heapdumpField.getName()).couldBeDefault()) {
                        return false;
                    }
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        // If we can't tell if it's been run but it has no side-effects, just run it.
        if (!classInitInfo.mayHaveSideEffects()) {
            return false;
        }
        
        // If it touched any other classes that haven't yet been initialized, then
        // this class must not be initialized.
        for (String touchedClass : classInitInfo.touchedClasses()) {
            ClassMirror touchedClassMirror = HolographInternalUtils.loadClassMirrorInternal(this, touchedClass.replace('/', '.'));
            if (!touchedClassMirror.initialized()) {
                return false;
            }
        }
        
        throw new UnsupportedOperationException("Unable to infer initialization status of class: " + this);
    }
    
    private boolean isConstantField(BytecodeClassMirror.StaticField field) {
        try {
            return field.getBoxedValue() != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasDefaultValue(FieldMirror field) throws IllegalAccessException {
        Type type = Reflection.typeForClassMirror(field.getType());
        switch (type.getSort()) {
        case Type.BOOLEAN:      return field.getBoolean() == false;
        case Type.BYTE:         return field.getByte() == 0;
        case Type.SHORT:        return field.getShort() == 0;
        case Type.CHAR:         return field.getChar() == 0;
        case Type.INT:          return field.getInt() == 0;
        case Type.LONG:         return field.getLong() == 0;
        case Type.FLOAT:        return field.getFloat() == 0;
        case Type.DOUBLE:       return field.getDouble() == 0;
        default:                return field.get() == null;
        }
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        // Force initialization just as the VM would, in case there is
        // a <clinit> method that needs to be run.
        MirageClassLoader.initializeClassMirror(this);
        
        return super.getStaticField(name);
    }
    
    public byte[] getRawAnnotations() {
        try {
            return super.getRawAnnotations(); 
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getRawAnnotations();
        }
    };
}
