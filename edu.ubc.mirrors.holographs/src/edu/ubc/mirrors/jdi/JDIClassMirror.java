package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
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
        public ClassMirror getClassMirror() {
            return vm.findBootstrapClassMirror(Object.class.getName());
        }
        
        @Override
        public ClassMirror forClassMirror() {
            return JDIClassMirror.this;
        }
    }

    protected final ReferenceType refType;
    
    public JDIClassMirror(JDIVirtualMachineMirror vm, ClassObjectReference t) {
        super(vm, t);
        this.refType = t.reflectedType();
    }

    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }

    @Override
    public String getClassName() {
        String name = refType.name();
        if (name.endsWith("[]")) {
            // TODO-RS: This is probably wrong - need to fix it and 
            // deal with any caller-side issues.
            name = Reflection.arrayClassName(name);
        }
        return name;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return (ClassMirrorLoader)vm.makeMirror(refType.classLoader());
    }

    @Override
    public byte[] getBytecode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRawAnnotations() {
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
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
	List<ClassMirror> requestedTypes = Arrays.asList(paramTypes);
	for (Method method : refType.methodsByName(name)) {
	    MethodMirror mirror = new JDIMethodMirror(vm, method);
	    if (mirror.getParameterTypes().equals(requestedTypes)) {
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
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
	List<ClassMirror> requestedTypes = Arrays.asList(paramTypes);
	for (Method method : refType.methodsByName("<init>")) {
	    ConstructorMirror mirror = new JDIConstructorMirror(vm, method);
	    if (mirror.getParameterTypes().equals(requestedTypes)) {
		return mirror;
	    }
	}
	
	ClassMirror superClassMirror = getSuperClassMirror();
	if (superClassMirror != null) {    
	    try {
		return superClassMirror.getConstructor(paramTypes);
	    } catch (NoSuchMethodException e) {
		// Fall through
	    }
	}
	
        for (ClassMirror interfaceMirror : getInterfaceMirrors()) {
            try {
                return interfaceMirror.getConstructor(paramTypes);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean initialized() {
        return refType.isInitialized();
    }

    @Override
    public InstanceMirror getStaticFieldValues() {
        return new JDIStaticFieldValuesMirror(vm, mirror);
    }

}
