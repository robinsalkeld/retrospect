package edu.ubc.mirrors.holographs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.fieldmap.FieldMapThreadMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror.StaticsInfo;
import edu.ubc.mirrors.test.Breakpoint;
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
        try {
            return super.getMethod(name, paramTypes);
        } catch (UnsupportedOperationException e) {
            return new MethodHolograph(this, getBytecodeMirror().getMethod(name, paramTypes));
        }
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
	try {
	    return super.getConstructor(paramTypes);
	} catch (UnsupportedOperationException e) {
	    return new ConstructorHolograph(this, getBytecodeMirror().getConstructor(paramTypes));
	}
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
	try {
            return super.getDeclaredConstructors(publicOnly);
        } catch (UnsupportedOperationException e) {
            List<ConstructorMirror> bytecodeCtrs = getBytecodeMirror().getDeclaredConstructors(publicOnly);
            List<ConstructorMirror> result = new ArrayList<ConstructorMirror>(bytecodeCtrs.size());
            for (ConstructorMirror bytecodeCtr : bytecodeCtrs) {
        	result.add(new ConstructorHolograph(this, bytecodeCtr));
            }
            return result;
        }
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
    
    public static void cleanStackTrace(Throwable t) {
	if (!(t instanceof Mirage)) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                stackTrace[i] = new StackTraceElement(
                        MirageClassGenerator.getOriginalBinaryClassName(stackTrace[i].getClassName()),
                        stackTrace[i].getMethodName(),
                        stackTrace[i].getFileName(),
                        stackTrace[i].getLineNumber());
            }
            t.setStackTrace(stackTrace);
	}
        
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            cleanStackTrace(cause);
        }
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
    
    ClassMirror getBytecodeMirror() {
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
    public ClassMirror getComponentClassMirror() {
	try {
	    return super.getComponentClassMirror();
	} catch (UnsupportedOperationException e) {
	    Type type = Reflection.typeForClassMirror(this);
	    Type componentType = MirageClassGenerator.makeArrayType(type.getDimensions() - 1, type.getElementType());
	    try {
		return Reflection.classMirrorForType(getVM(), ThreadHolograph.currentThreadMirror(), componentType, false, getLoader());
	    } catch (ClassNotFoundException e1) {
		throw new NoClassDefFoundError(e1.toString());
	    }
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
            List<MethodMirror> bytecodeMethods = getBytecodeMirror().getDeclaredMethods(publicOnly);
            List<MethodMirror> result = new ArrayList<MethodMirror>(bytecodeMethods.size());
            for (MethodMirror bytecodeMethod : bytecodeMethods) {
        	result.add(new MethodHolograph(this, bytecodeMethod));
            }
            return result;
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
        
        if (getClassName().equals(Collections.class.getName())) {
            Breakpoint.bp();
        }
        
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
