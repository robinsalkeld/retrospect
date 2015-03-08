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
package edu.ubc.mirrors.jdi;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class JDIClassMirror extends JDIInstanceMirror implements ClassMirror {

    private final class JDIStaticFieldValuesMirror extends JDIInstanceMirror implements StaticFieldValuesMirror {
        private JDIStaticFieldValuesMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
            super(vm, t);
        }

        @Override
        protected Value getValue(FieldMirror field) {
            Field jdiField = ((JDIFieldMirror)field).field;
            assert jdiField.isStatic();
            return jdiField.declaringType().getValue(jdiField);
        }

        @Override
        protected void setValue(FieldMirror field, Value value) {
            Field jdiField = ((JDIFieldMirror)field).field;
            assert jdiField.isStatic();
            try {
                ((ClassType)jdiField.declaringType()).setValue(jdiField, value);
            } catch (InvalidTypeException e) {
                throw new RuntimeException(e);
            } catch (ClassNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public ClassMirror getClassMirror() {
            return vm.findBootstrapClassMirror(Object.class.getName());
        }
        
        @Override
        public ClassMirror forClassMirror() {
            return JDIClassMirror.this;
        }
    }

    protected final ReferenceType refType;
    
    private ClassMirrorLoader loader;
    private boolean loaderFetched = false;
    
    private boolean initialized = false;
    
    public JDIClassMirror(JDIVirtualMachineMirror vm, ClassObjectReference t) {
        super(vm, t);
        this.refType = t.reflectedType();
    }

    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
        ClassType classClass = (ClassType)vm.jdiVM.classesByName(Class.class.getName()).get(0);
        Method getAnnotationsMethod = classClass.methodsByName("getAnnotations", "()[Ljava/lang/annotation/Annotation;").get(0);
        ArrayReference annotsArray = (ArrayReference)JDIVirtualMachineMirror.safeInvoke(refType.classObject(), threadRef, getAnnotationsMethod);
        return (List<AnnotationMirror>)vm.wrapAnnotationValue(threadRef, annotsArray);
    }
    
    @Override
    public String getClassName() {
        return refType.name();
    }
    
    @Override
    public String getSignature() {
        return refType.signature();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        if (!loaderFetched) {
            loader = (ClassMirrorLoader)vm.makeMirror(refType.classLoader());
            loaderFetched = true;
        }
        return loader;
    }

    @Override
    public byte[] getBytecode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimitive() {
	// Well ain't I just too clever :)
	return refType.signature().length() == 1;
    }

    @Override
    public boolean isArray() {
        return refType instanceof ArrayType;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (refType instanceof ArrayType) {
            try {
		return vm.makeClassMirror(((ArrayType)refType).componentType());
	    } catch (ClassNotLoadedException e) {
		throw new UnsupportedOperationException();
	    }
        } else {
            return null;
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        if (refType instanceof ClassType) {
            return vm.makeClassMirror(((ClassType)refType).superclass());
        } else if (refType instanceof InterfaceType) {
            return null;
        } else if (refType instanceof ArrayType) {
            return vm.findBootstrapClassMirror(Object.class.getName());
        } else {
            throw new IllegalStateException("Unrecognized ReferenceType class: " + refType);
        }
    }

    @Override
    public boolean isInterface() {
        return refType instanceof InterfaceType;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        if (refType instanceof ClassType) {
            return vm.makeClassMirrorList(((ClassType)refType).interfaces());
        } else if (refType instanceof InterfaceType) {
            return vm.makeClassMirrorList(((InterfaceType)refType).superinterfaces());
        } else if (refType instanceof ArrayType) {
            return ArrayClassMirror.getInterfaceMirrorsForArrays(vm);
        } else {
            throw new IllegalStateException("Unrecognized ReferenceType class: " + refType);
        }
    }

    @Override
    public List<FieldMirror> getDeclaredFields() {
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        for (Field field : refType.fields()) {
            result.add(new JDIFieldMirror(vm, field));
        }
        return result;
    }

    @Override
    public FieldMirror getDeclaredField(String name) {
        if (refType instanceof ClassType) {
            Field field = ((ClassType)refType).fieldByName(name);
            if (field != null) {
                return new JDIFieldMirror(vm, field);
            }
        }
        
        return null;
    }
    
    @Override
    public List<ObjectMirror> getInstances() {
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        for (ObjectReference or : refType.instances(0)) {
            result.add(vm.makeMirror(or));
        }
        return result;
    }

    @Override
    public MethodMirror getDeclaredMethod(String name, String... paramTypes) throws SecurityException, NoSuchMethodException {
        List<String> requestedTypes = Arrays.asList(paramTypes);
        for (Method method : refType.methodsByName(name)) {
            MethodMirror mirror = new JDIMethodMirror(vm, method);
            if (mirror.getParameterTypeNames().equals(requestedTypes)) {
                return mirror;
            }
        }
        throw new NoSuchMethodException(name);
    }
    
    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        List<MethodMirror> result = new ArrayList<MethodMirror>();
        for (Method method : refType.methods()) {
            if (method.name().startsWith("<")) {
                continue;
            }
            if (!publicOnly || Modifier.isPublic(method.modifiers())) {
                result.add(new JDIMethodMirror(vm, method));
            }
        }
        return result;
    }
    
    @Override
    public MethodMirror getMethod(String name, String... paramTypes) throws SecurityException, NoSuchMethodException {
        List<String> requestedTypes = Arrays.asList(paramTypes);
	for (Method method : refType.methodsByName(name)) {
	    MethodMirror mirror = new JDIMethodMirror(vm, method);
	    if (mirror.getParameterTypeNames().equals(requestedTypes)) {
		return mirror;
	    }
	}
	
	ClassMirror superClassMirror = getSuperClassMirror();
	if (superClassMirror != null) {    
	    try {
		return superClassMirror.getMethod(name, paramTypes);
	    } catch (NoSuchMethodException e) {
		// Fall through
	    }
	}
	
        for (ClassMirror interfaceMirror : getInterfaceMirrors()) {
            try {
                return interfaceMirror.getMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
	
	throw new NoSuchMethodException(name);
    }
    
    @Override
    public ConstructorMirror getConstructor(String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
	List<String> requestedTypeNames = Arrays.asList(paramTypeNames);
	for (Method method : refType.methodsByName("<init>")) {
	    ConstructorMirror mirror = new JDIConstructorMirror(vm, method);
	    if (mirror.getParameterTypeNames().equals(requestedTypeNames)) {
		return mirror;
	    }
	}
	
	ClassMirror superClassMirror = getSuperClassMirror();
	if (superClassMirror != null) {    
	    try {
		return superClassMirror.getConstructor(paramTypeNames);
	    } catch (NoSuchMethodException e) {
		// Fall through
	    }
	}
	
        for (ClassMirror interfaceMirror : getInterfaceMirrors()) {
            try {
                return interfaceMirror.getConstructor(paramTypeNames);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
	
	throw new NoSuchMethodException();
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getModifiers() {
        return refType.modifiers();
    }

    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int size) {
        ArrayType targetType = (ArrayType)vm.jdiVM.classesByName(getClassName() + "[]").get(0);
        return (ArrayMirror)vm.makeMirror(targetType.newInstance(size));
    }

    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean initialized() {
        // Once initialized, a class stays initialized
        if (!initialized) {
            initialized = refType.isInitialized();
        }
        return initialized;
    }

    @Override
    public StaticFieldValuesMirror getStaticFieldValues() {
        return new JDIStaticFieldValuesMirror(vm, mirror);
    }

    public ReferenceType getReferenceType() {
        return refType;
    }
    
    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
    }
    
    @Override
    public List<ClassMirror> getSubclassMirrors() {
        if (refType instanceof ClassType) {
            return vm.makeClassMirrorList(((ClassType)refType).subclasses());
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public ClassMirror getEnclosingClassMirror() {
        //TODO-RS
        return null;
    }
    
    @Override
    public MethodMirror getEnclosingMethodMirror() {
        //TODO-RS
        return null;
    }
    
    @Override
    public String toString() {
        return super.toString() + '"' + getClassName() + '"';
    }
    
    public FieldMirror createField(int modifiers, ClassMirror type, String name) {
        throw new UnsupportedOperationException();
    }
}
