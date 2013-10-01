/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.holographs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.EventDispatch.EventCallback;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapClassMirrorLoader;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.fieldmap.FieldMapThreadMirror;
import edu.ubc.mirrors.holograms.Hologram;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror.StaticsInfo;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class ClassHolograph extends WrappingClassMirror implements MirrorInvocationHandler {

    private InstanceMirror memberFieldsDelegate;
    private StaticFieldValuesMirror staticFieldValues;
    
    private ClassMirror bytecodeMirror;
    
    public static class MethodPattern implements Comparable<MethodPattern> {
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
        
        public boolean matches(String owner, MethodMirror method) {
            return classNamePattern.matcher(owner).matches() && methodNamePattern.matcher(method.getName()).matches();
        }
        
        public String getCategory() {
            return category;
        }
        
        @Override
        public int compareTo(MethodPattern o) {
            int result = category.compareTo(o.category);
            if (result != 0) return result;
            result = classNamePattern.toString().compareTo(o.classNamePattern.toString());
            if (result != 0) return result;
            return methodNamePattern.toString().compareTo(o.methodNamePattern.toString());
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
    
    static final List<MirrorInvocationHandlerProvider> invocationHandlerProviders;
    static {
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint("edu.ubc.mirrors.holographs.nativeStubsProvider");
        invocationHandlerProviders = new ArrayList<MirrorInvocationHandlerProvider>();
        for (IExtension ext : extPoint.getExtensions()) {
            for (IConfigurationElement config : ext.getConfigurationElements()) {
                try {
                    invocationHandlerProviders.add((MirrorInvocationHandlerProvider)config.createExecutableExtension("impl"));
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    static final List<ClassMirrorInitializedProvider> initializedProviders;
    static {
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint("edu.ubc.mirrors.holographs.initializedProvider");
        initializedProviders = new ArrayList<ClassMirrorInitializedProvider>();
        for (IExtension ext : extPoint.getExtensions()) {
            for (IConfigurationElement config : ext.getConfigurationElements()) {
                try {
                    initializedProviders.add((ClassMirrorInitializedProvider)config.createExecutableExtension("impl"));
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    protected ClassHolograph(VirtualMachineHolograph vm, ClassMirror wrapped) {
        super(vm, wrapped);
        this.vm = vm;
        sychronizeWithWrapped();
    }
    
    private void sychronizeWithWrapped() {
        memberFieldsDelegate = new InstanceHolograph(vm, wrapped);
        staticFieldValues = (StaticFieldValuesMirror)vm.getWrappedMirror(wrapped.getStaticFieldValues());
        if (!vm.canBeModified() || wrapped instanceof DefinedClassMirror) {
            // Allow mutations on existing objects only if the underlying VM can't be resumed
            memberFieldsDelegate = new MutableInstanceMirror(memberFieldsDelegate);
            staticFieldValues = new MutableStaticFieldValuesMirror(staticFieldValues);
        }
    }
    
    public ClassMirror getWrappedClassMirror() {
        return wrapped;
    }
    
    public static MirrorInvocationHandler getMethodHandler(MethodMirror method) {
        MirrorInvocationHandler handler;
        for (MirrorInvocationHandlerProvider provider : invocationHandlerProviders) {
            handler = provider.getInvocationHandler(method);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
    
    // TODO-RS: Temporary for evaluation
    static final Set<String> unsupportedNativeMethods = new TreeSet<String>();
    
    public Object invoke(InstanceMirror object, MethodMirror method, Object[] args) throws MirrorInvocationTargetException {
        MirrorInvocationHandler handler = getMethodHandler(method);
        if (handler == null) {
            String methodSig = getClassName() + "#" + method.getName() + Reflection.getMethodType(method);
            unsupportedNativeMethods.add(methodSig);
            throw new InternalError("Unsupported native method: " + methodSig);
        }
        return handler.invoke(object, method, args);
    }
    
    public static String getIllegalNativeMethodMessage(String owner, MethodMirror method) {
        for (MethodPattern pattern : illegalMethodPatterns) {
            if (pattern.matches(owner, method)) {
                return pattern.category;
            }
        }
        return null;
    }
    
    public static String getMissingNativeMethodMessage(String owner, MethodMirror method) {
        for (MethodPattern pattern : missingMethodPatterns) {
            if (pattern.matches(owner, method)) {
                return pattern.category;
            }
        }
        return null;
    }
        
    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        if (hasBytecode()) {
            return super.getMethod(name, paramTypes);
        } else {
            return new MethodHolograph(this, getBytecodeMirror().getDeclaredMethod(name, paramTypes));
        }
    }
    
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        if (hasBytecode()) {
            return super.getMethod(name, paramTypes);
        } else {
            return new MethodHolograph(this, getBytecodeMirror().getMethod(name, paramTypes));
        }
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        if (hasBytecode()) {
	    return super.getConstructor(paramTypes);
	} else {
	    return new ConstructorHolograph(this, getBytecodeMirror().getConstructor(paramTypes));
	}
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        if (hasBytecode()) {
            return super.getDeclaredConstructors(publicOnly);
        } else {
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
    
    public static HologramClassLoader getHologramClassLoader(ClassMirror classMirror) {
        return getHologramClassLoader(classMirror.getVM(), classMirror.getLoader());
    }
    
    public static HologramClassLoader getHologramClassLoader(VirtualMachineMirror vm, ClassMirrorLoader loader) {
        return loader == null ? 
                ((VirtualMachineHolograph)vm).getHologramClassLoader() : 
                    ((ClassLoaderHolograph)loader).getHologramClassLoader();
    }
    
    public HologramClassLoader getHologramClassLoader() {
        return getHologramClassLoader(getVM(), getLoader());
    }
    
    public static Class<?> getHologramClass(ClassMirror classMirror, boolean impl) {
        try {
            return getHologramClassLoader(classMirror).getHologramClass(classMirror, impl);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Class<?> getHologramClass(boolean impl) {
        return getHologramClass(this, impl);
    }
    
    public static void cleanStackTrace(Throwable t) {
	if (!(t instanceof Hologram)) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                stackTrace[i] = new StackTraceElement(
                        HologramClassGenerator.getOriginalBinaryClassName(stackTrace[i].getClassName()),
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
        if (cause instanceof Hologram) {
            return new MirrorInvocationTargetException((InstanceMirror)((Hologram)cause).getMirror());
        } else {
            throw (InternalError)new InternalError(cause.getMessage()).initCause(cause);
        }
    }
    
    public static Object makeHologram(Object mirror) {
        if (mirror instanceof ObjectMirror) {
            ObjectMirror objectMirror = (ObjectMirror)mirror;
            return getHologramClassLoader(objectMirror.getClassMirror()).makeHologram(objectMirror);
        } else {
            return mirror;
        }
    }
    
    public static ClassMirror getOriginalClassMirror(Class<?> hologramValue) {
        HologramClassLoader loader = (HologramClassLoader)hologramValue.getClassLoader();
        return loader.loadOriginalClassMirror(HologramClassGenerator.getOriginalBinaryClassName(hologramValue.getName()));
    }

    public static Object unwrapHologram(Object hologram) {
        if (hologram instanceof Hologram) {
            return ((Hologram)hologram).getMirror();
        } else {
            return hologram;
        }
    }
    
    ClassMirror getBytecodeMirror() {
        if (bytecodeMirror == null) {
            if (wrapped instanceof DefinedClassMirror) {
                throw new IllegalStateException();
            } else {
                bytecodeMirror = getVM().getBytecodeClassMirror(this);
                validateBytecodeClass();
            }
        }
        return bytecodeMirror;
    }

    /*
     * Checks that the class defined by the bytecode mirror matches the definition
     * of the class from the heap dump. This will catch a lot of version mismatches.
     * At the same time, make sure the fields are ordered according to the bytecode.
     */ 
    private void validateBytecodeClass() {
        Map<String, FieldMirror> expectedFields = new HashMap<String, FieldMirror>();
        for (FieldMirror f : super.getDeclaredFields()) {
            expectedFields.put(f.getName(), f);
        }
        
        declaredFields = new ArrayList<FieldMirror>(expectedFields.size());
        for (FieldMirror bytecodeField : bytecodeMirror.getDeclaredFields()) {
            String fieldName = bytecodeField.getName();
            FieldMirror expectedField = expectedFields.remove(fieldName);
            if (expectedField == null) {
                // Ignore Throwable.backtrace - it's hidden in many formats
                if (getClassName().equals(Throwable.class.getName()) && fieldName.equals("backtrace")) {
                    continue;
                }
                throw new IllegalStateException("Unexpected field found in bytecode class (" + getClassName() + "): " + bytecodeField);
            }
            // Just use type name - checking actual types across VMs is too complicated and
            // causes too many side effects to boot.
            if (!expectedField.getTypeName().equals(bytecodeField.getTypeName())) {
                throw new IllegalStateException("Type mismatch for field " + expectedField.getName() + ": expected " + expectedField.getTypeName() + " but was " + bytecodeField.getTypeName());
            }
            if (expectedField.getModifiers() != bytecodeField.getModifiers()) {
                throw new IllegalStateException("Modifiers mismatch for field " + expectedField.getName() + ": expected " + expectedField.getModifiers() + " but was " + bytecodeField.getModifiers());
            }
            
            declaredFields.add(expectedField);
        }
        
        if (!expectedFields.isEmpty()) {
            throw new IllegalStateException("Bytecode class missing fields: " + expectedFields);
        }
    }

    @Override
    public byte[] getBytecode() {
        if (hasBytecode()) {
            return super.getBytecode();
        } else {
            return getBytecodeMirror().getBytecode();
        }
    }

    boolean hasBytecode() {
        return vm.getWrappedVM().canGetBytecodes() || wrapped instanceof DefinedClassMirror || bytecodeMirror == this;
    }
    
    boolean actuallyHasInitialization() {
        return vm.getWrappedVM().hasClassInitialization() || wrapped instanceof DefinedClassMirror || bytecodeMirror == this;
    }
    
    @Override
    public boolean isInterface() {
        if (hasBytecode()) {
            return super.isInterface();
        } else {
            return getBytecodeMirror().isInterface();
        }
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        if (hasBytecode()) {
            return super.getInterfaceMirrors();
        } else {
            return getBytecodeMirror().getInterfaceMirrors();
        }
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (hasBytecode()) {
	    return super.getComponentClassMirror();
	} else {
	    Type type = Reflection.typeForClassMirror(this);
	    Type componentType = Reflection.makeArrayType(type.getDimensions() - 1, type.getElementType());
	    return HolographInternalUtils.classMirrorForType(getVM(), ThreadHolograph.currentThreadMirror(), componentType, false, getLoader());
        }
    }
    
    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        if (hasBytecode()) {
            return super.getDeclaredMethods(publicOnly);
        } else {
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
    
    public void ensureInitialized() {
        if (!initialized()) {
            Class<?> hologramClass = getHologramClass(true);
            try {
                // Reading a non-constant field forces class initialization
                hologramClass.getField("classMirror").get(null);
            } catch (IllegalAccessException e) {
                throw new InternalError();
            } catch (NoSuchFieldException e) {
                // Ignore - not a dynamically generated class
            }
            initialized = Boolean.TRUE;
        }
    }
    
    private Boolean initialized = null;
    private boolean initializedResolved = false;
    
    // TODO-RS: Temporary for evaluation
    static final Set<String> inferInitFailures = new TreeSet<String>();
    
    @Override
    public boolean initialized() {
        if (actuallyHasInitialization()) {
            return super.initialized();
        } else {
            resolveInitialized();
            if (initialized == null) {
                inferInitFailures.add(getClassName());
                throw new InternalError("Unable to infer initialization status of class: " + this);
            }
            return initialized;
        }
    }

    Boolean resolveInitialized() {
        if (actuallyHasInitialization()) {
            return super.initialized();
        } else {
            if (!initializedResolved) {
                initialized = inferInitialized();
                initializedResolved = true;
                
                if (initialized != null) {
                    setInitialized(initialized);
                }
            }
            return initialized;
        }
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
    
    private Boolean inferInitialized() {
        if (isArray()) {
            return true;
        }
     
        // Check the plugins first
        for (ClassMirrorInitializedProvider provider : initializedProviders) {
            Boolean result = provider.isInitialized(this);
            if (result != null) {
                return result;
            }
        }
        
        BytecodeClassMirror bytecodeClassMirror = (BytecodeClassMirror)getBytecodeMirror();
        
        // If there is no <clinit> method, then we don't care.
        StaticsInfo classInitInfo = bytecodeClassMirror.classInitInfo();
        if (classInitInfo == null) {
            return true;
        }
        
        // If the class (or any subclass) has any instances the initialization must have run.
        // TODO-RS: Ideally this would be a combination of checking direct instances and
        // recursing on subclasses, but we'd have to be careful about infinite recursion!
        if (hasInstances(this)) {
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
            ClassHolograph touchedClassMirror = (ClassHolograph)HolographInternalUtils.loadClassMirrorInternal(this, Type.getObjectType(touchedClass).getClassName());
            Boolean touchedInitialized = touchedClassMirror.resolveInitialized();
            if (touchedInitialized != null && touchedInitialized.booleanValue() == false) {
                return false;
            }
        }
        
        return null;
    }
    
    private static boolean hasInstances(ClassMirror klass) {
        if (!klass.getInstances().isEmpty()) {
            return true;
        }
        for (ClassMirror subclass : klass.getSubclassMirrors()) {
            if (hasInstances(subclass)) {
                return true;
            }
        }
        return false;
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
    public FieldMirror getDeclaredField(String name) {
        // Force initialization just as the VM would, in case there is
        // a <clinit> method that needs to be run.
        ensureInitialized();

        return super.getDeclaredField(name);
    }
    
    private List<FieldMirror> declaredFields;
    
    @Override
    public List<FieldMirror> getDeclaredFields() {
        if (wrapped instanceof DefinedClassMirror) {
            return super.getDeclaredFields();
        } else {
            // Ensure the correctly ordered list is initialized.
            getBytecodeMirror();
            return declaredFields;
        }
    }
    
    public byte[] getRawAnnotations() {
        if (hasBytecode()) {
            return super.getRawAnnotations(); 
        } else {
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
        super.setWrapped(wrapped);
        this.declaredFields = null;
        sychronizeWithWrapped();
    }
    
    @Override
    public ClassMirror getEnclosingClassMirror() {
        if (hasBytecode()) {
            return super.getEnclosingClassMirror();
        } else {
            return getBytecodeMirror().getEnclosingClassMirror();
        }
    }
    
    @Override
    public MethodMirror getEnclosingMethodMirror() {
        if (hasBytecode()) {
            return super.getEnclosingMethodMirror();
        } else {
            return getBytecodeMirror().getEnclosingMethodMirror();
        }
    }
    
    public void registerPrepareCallback() {
        // Set up a callback so that if the wrapped VM defines this same class later on,
        // we can replace the holograph version with the "real" one. 
        // TODO-RS: Make sure this is sound by checking the side-effects
        // in the class initialization method!!!
        VirtualMachineHolograph vm = getVM();
        if (vm.canBeModified()) {
            ClassMirrorPrepareRequest request = vm.eventRequestManager().createClassMirrorPrepareRequest();
            request.addClassFilter(getClassName());
            vm.dispatch().addCallback(request, new EventCallback() {
                @Override
                public void handle(MirrorEvent event) {
                    ClassMirrorPrepareEvent prepareEvent = (ClassMirrorPrepareEvent)event;
                    ClassHolograph prepared = (ClassHolograph)prepareEvent.classMirror();
                    // The name will match, but we have to check the class loader manually.
                    ClassMirrorLoader preparedLoader = prepared.getLoader();
                    ClassLoaderHolograph holographLoader = getLoader();
                    if (preparedLoader == null ? holographLoader == null : preparedLoader.equals(holographLoader)) {
                        setWrapped(prepared.getWrappedClassMirror());
                    }
                }
            });
            request.enable();
        }
    }
}
