package edu.ubc.mirrors.holographs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import edu.ubc.mirrors.MirrorInvocationTargetException;
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
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class ClassHolograph extends WrappingClassMirror {

    private InstanceMirror memberFieldsDelegate;
    private InstanceMirror staticFieldValues;
    
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
    
    private final VirtualMachineHolograph vm;
    
    protected ClassHolograph(VirtualMachineHolograph vm, ClassMirror wrapped) {
        super(vm, wrapped);
        this.vm = vm;
        memberFieldsDelegate = new InstanceHolograph(vm, wrapped);
        staticFieldValues = (InstanceMirror)vm.getWrappedMirror(wrapped.getStaticFieldValues());
        if (!vm.canBeModified() || wrapped instanceof DefinedClassMirror) {
            // Allow mutations on existing objects only if the underlying VM can't be resumed
            memberFieldsDelegate = new MutableInstanceMirror(memberFieldsDelegate);
            staticFieldValues = new MutableInstanceMirror(staticFieldValues);
        }
        
        if (wrapped.getClassName().equals("org.eclipse.ui.internal.Workbench")) {
            int bp =4;
            bp++;
        }
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
    
    public static MirrorInvocationTargetException causeAsMirrorInvocationTargetException(Throwable t) {
        cleanStackTrace(t);
        Throwable cause = t.getCause();
        if (cause instanceof Mirage) {
            return new MirrorInvocationTargetException((InstanceMirror)((Mirage)cause).getMirror());
        } else {
            throw (InternalError)new InternalError().initCause(cause);
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
	    return HolographInternalUtils.classMirrorForType(getVM(), ThreadHolograph.currentThreadMirror(), componentType, false, getLoader());
        }
    }
    
    @Override
    public List<FieldMirror> getDeclaredFields() {
        try {
            return super.getDeclaredFields();
        } catch (UnsupportedOperationException e) {
            List<FieldMirror> bytecodeFields = getBytecodeMirror().getDeclaredFields();
            List<FieldMirror> result = new ArrayList<FieldMirror>(bytecodeFields.size());
            for (FieldMirror bytecodeField : bytecodeFields) {
                result.add(vm.getFieldMirror(bytecodeField));
            }
            return result;
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
        InstanceMirror result;
        if (Reflection.isAssignableFrom(getVM().findBootstrapClassMirror(ClassLoader.class.getName()), this)) {
            result = new FieldMapClassMirrorLoader(wrapped);
        } else if (Reflection.isAssignableFrom(getVM().findBootstrapClassMirror(Thread.class.getName()), this)) {
            result = new FieldMapThreadMirror(wrapped);
        } else {
            result = new FieldMapMirror(wrapped);
        }
        return (InstanceMirror)vm.getWrappedMirror(result);
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
    private boolean initializedResolved = false;
    
    @Override
    public boolean initialized() {
        try {
            return super.initialized();
        } catch (UnsupportedOperationException e) {
            resolveInitialized();
            if (initialized == null) {
                throw new InternalError("Unable to infer initialization status of class: " + this);
            }
            return initialized;
        }
    }

    Boolean resolveInitialized() {
        try {
            return super.initialized();
        } catch (UnsupportedOperationException e) {
            // Fall through and infer
        }
        
        if (!initializedResolved) {
            initialized = inferInitialized();
            initializedResolved = true;
            
            if (initialized != null) {
                setInitialized(initialized);
            }
        }
        return initialized;
    }
    
    private void setInitialized(boolean initialized) {
        if (initializedResolved) {
            if (this.initialized != initialized) {
                throw new InternalError("Inconsistent initialization inference on class " + getClassName() + ": " + this.initialized + " != " + initialized);
            }
            return;
        }
        
        this.initialized = initialized;
        this.initializedResolved = true;
        
        if (initialized) {
            // Propogate
            BytecodeClassMirror bytecodeClassMirror = (BytecodeClassMirror)getBytecodeMirror();
            StaticsInfo classInitInfo = bytecodeClassMirror.classInitInfo();
            for (String touchedClass : classInitInfo.touchedClasses()) {
                ClassMirror touchedClassMirror = HolographInternalUtils.loadClassMirrorInternal(this, touchedClass.replace('/', '.'));
                if (touchedClassMirror instanceof ClassHolograph) {
                    ((ClassHolograph)touchedClassMirror).setInitialized(true);
                }
            }
        }
    }
    
    private static final Set<String> idempotentClassInits = new HashSet<String>(Arrays.asList(
            Modifier.class.getName(),
//            PatternSyntaxException.class.getName(),
            URLClassLoader.class.getName(),
            SecureClassLoader.class.getName(),
//            "org.eclipse.core.internal.runtime.CompatibilityHelper",
//            "com.ibm.icu.impl.ICUService",
//            "org.eclipse.ui.internal.misc.Policy",
            "org.eclipse.equinox.weaving.hooks.AbstractWeavingHook"
//            "org.eclipse.osgi.internal.permadmin.EquinoxSecurityManager",
//            "java.lang.invoke.MethodHandleNatives",
//            "sun.reflect.UnsafeStaticFieldAccessorImpl",
//            // Hits illegal native methods anyway - this makes the classes untouchable
//            "sun.nio.ch.NativeThread",
//            "sun.reflect.ConstantPool",
//            "java.lang.Compiler",
//            "org.eclipse.swt.internal.cocoa.CGRect",
//            // This one would be solved via inter-class analysis
//            "org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor"
            ));
    
    private Boolean inferInitialized() {
        if (isArray()) {
            return true;
        }
     
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
        InstanceMirror bytecodeValues = bytecodeClassMirror.getStaticFieldValues();
        InstanceMirror wrappedValues = getStaticFieldValues();
        for (FieldMirror bytecodeField : bytecodeClassMirror.getDeclaredFields()) {
            if (Modifier.isStatic(bytecodeField.getModifiers())) {
                try {
                    // Be sure to call the super version of getStaticField, because otherwise
                    // we will end up in infinite recursion!
                    FieldMirror wrappedField = super.getDeclaredField(bytecodeField.getName());

                    if (!hasDefaultValue(wrappedValues, wrappedField)) {
                        // If there are any static, non-constant fields with non-null values,
                        // then initialization must have occurred since it has to before
                        // any static fields can be set.
                        if (hasDefaultValue(bytecodeValues, bytecodeField)) {
                            return true;
                        }
                    } else {
                        // If there are any static fields with null/0 values,
                        // and the <clinit> method has the effect of setting non-default values,
                        // then initialization must not have occurred.
                        if (!classInitInfo.isDefault(bytecodeField.getName()).couldBeDefault()) {
                            return false;
                        }
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        // If we can't tell if it's been run but it has no side-effects, just run it.
        if (!classInitInfo.mayHaveSideEffects()) {
            return false;
        }
        
        // If it touched any other classes that haven't yet been initialized, then
        // this class must not be initialized.
        // TODO-RS: Apply the contra-positive too: if another class has been initialized and touches this class,
        // this class must be initialized.
        for (String touchedClass : classInitInfo.touchedClasses()) {
            ClassHolograph touchedClassMirror = (ClassHolograph)HolographInternalUtils.loadClassMirrorInternal(this, touchedClass.replace('/', '.'));
            Boolean touchedInitialized = touchedClassMirror.resolveInitialized();
            if (touchedInitialized != null && touchedInitialized.booleanValue() == false) {
                return false;
            }
        }
        
        return null;
    }
    
    /**
     * @param field Static field
     * @return
     * @throws IllegalAccessException
     */
    private static boolean hasDefaultValue(InstanceMirror obj, FieldMirror field) throws IllegalAccessException {
        Type type = Reflection.typeForClassMirror(field.getType());
        switch (type.getSort()) {
        case Type.BOOLEAN:      return obj.getBoolean(field) == false;
        case Type.BYTE:         return obj.getByte(field) == 0;
        case Type.SHORT:        return obj.getShort(field) == 0;
        case Type.CHAR:         return obj.getChar(field) == 0;
        case Type.INT:          return obj.getInt(field) == 0;
        case Type.LONG:         return obj.getLong(field) == 0;
        case Type.FLOAT:        return obj.getFloat(field) == 0;
        case Type.DOUBLE:       return obj.getDouble(field) == 0;
        default:                return obj.get(field) == null;
        }
    }
    
    @Override
    public FieldMirror getDeclaredField(String name) throws NoSuchFieldException {
        // Force initialization just as the VM would, in case there is
        // a <clinit> method that needs to be run.
        MirageClassLoader.initializeClassMirror(this);

        return super.getDeclaredField(name);
    }
    
    public byte[] getRawAnnotations() {
        try {
            return super.getRawAnnotations(); 
        } catch (UnsupportedOperationException e) {
            return getBytecodeMirror().getRawAnnotations();
        }
    }
    
    // Since we'd rather extend WrappingClassMirror than InstanceHolograph, delegate the
    // member field accessors instead.
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.get(field);
    }
    
    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getBoolean(field);
    }
    
    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getByte(field);
    }
    
    public char getChar(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getChar(field);
    }
    
    public short getShort(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getShort(field);
    }
    
    public int getInt(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getInt(field);
    }
    
    public long getLong(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getLong(field);
    }
    
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getFloat(field);
    }
    
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        return memberFieldsDelegate.getDouble(field);
    }
    
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
        memberFieldsDelegate.set(field, o);
    }
    
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        memberFieldsDelegate.setBoolean(field, b);
    }
    
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        memberFieldsDelegate.setByte(field, b);
    }
    
    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        memberFieldsDelegate.setChar(field, c);
    }
    
    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        memberFieldsDelegate.setShort(field, s);
    }
    
    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        memberFieldsDelegate.setInt(field, i);
    }
    
    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        memberFieldsDelegate.setLong(field, l);
    }
    
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        memberFieldsDelegate.setFloat(field, f);
    }
    
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        memberFieldsDelegate.setDouble(field, d);
    }
    
    @Override
    public InstanceMirror getStaticFieldValues() {
        return staticFieldValues;
    }
    
    public void setWrapped(ClassMirror wrapped) {
        this.wrapped = wrapped;
        // TODO-RS: Do the right thing with the fields delegates!!!
    }
}
