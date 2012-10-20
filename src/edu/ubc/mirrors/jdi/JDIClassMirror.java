package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.snapshot.model.IClass;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.test.Breakpoint;

public class JDIClassMirror extends JDIInstanceMirror implements ClassMirror {

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
    public Map<String, ClassMirror> getDeclaredFields() {
        if (refType instanceof ClassType) {
            Map<String, ClassMirror> result = new HashMap<String, ClassMirror>();
            for (Field field : ((ClassType)refType).fields()) {
                try {
                    result.put(field.name(), vm.makeClassMirror(field.type()));
                } catch (ClassNotLoadedException e) {
                    // Re-raise as unsupported so that holographs can take care of this
                    throw new UnsupportedOperationException();
                }
            }
            return result;
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        if (refType instanceof ClassType) {
            Field field = ((ClassType)refType).fieldByName(name);
            if (field != null) {
                return new JDIStaticFieldMirror(vm, refType, field);
            }
        }
        
        throw new NoSuchFieldException(name);
    }

    @Override
    public List<InstanceMirror> getInstances() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        for (ObjectReference or : refType.instances(Integer.MAX_VALUE)) {
            result.add((InstanceMirror)vm.makeMirror(or));
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

}
