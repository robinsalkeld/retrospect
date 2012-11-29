package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class MirrorsReferenceType extends MirrorsMirrorWithModifiers implements ReferenceType {

    protected final ClassMirror wrapped;
    
    public MirrorsReferenceType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public String name() {
        return wrapped.getClassName();
    }

    @Override
    public String signature() {
        return Reflection.typeForClassMirror(wrapped).getDescriptor();
    }

    @Override
    public int compareTo(ReferenceType o) {
        return name().compareTo(o.name());
    }

    @Override
    public int modifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public List<Field> allFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Location> allLineLocations() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> allLineLocations(String arg0, String arg1) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Method> allMethods() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> availableStrata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoaderReference classLoader() {
        return (ClassLoaderReference)vm.wrapMirror(wrapped.getLoader());
    }

    @Override
    public ClassObjectReference classObject() {
        return (ClassObjectReference)vm.wrapMirror(wrapped);
    }

    @Override
    public byte[] constantPool() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int constantPoolCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String defaultStratum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean failedToInitialize() {
        return false;
    }

    @Override
    public Field fieldByName(String name) {
        try {
            return new MirrorsField(vm, wrapped.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public List<Field> fields() {
        List<Field> result = new ArrayList<Field>();
        for (FieldMirror field : wrapped.getDeclaredFields()) {
            result.add(new MirrorsField(vm, field));
        }
        return result;
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public List<ObjectReference> instances(long maxInstances) {
        List<ObjectReference> result = new ArrayList<ObjectReference>((int)maxInstances);
        for (InstanceMirror instance : wrapped.getInstances().subList(0, (int)maxInstances)) {
            result.add((ObjectReference)vm.wrapMirror(instance));
        }
        return result;
    }

    @Override
    public boolean isInitialized() {
        return wrapped.initialized();
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public boolean isVerified() {
        return true;
    }

    @Override
    public List<Location> locationsOfLine(int arg0)
            throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> locationsOfLine(String arg0, String arg1, int arg2)
            throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public int majorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Method> methods() {
        List<Method> result = new ArrayList<Method>();
        for (MethodMirror method : wrapped.getDeclaredMethods(false)) {
            result.add(new MethodMirrorMethod(vm, method));
        }
        for (ConstructorMirror cons : wrapped.getDeclaredConstructors(false)) {
            result.add(new ConstructorMirrorMethod(vm, cons));
        }
        // TODO-RS: No representation of static initializer in mirrors
        return result;
    }

    @Override
    public List<Method> methodsByName(String name) {
        List<Method> result = new ArrayList<Method>();
        if (name.equals("<init>")) {
            for (ConstructorMirror cons : wrapped.getDeclaredConstructors(false)) {
                result.add(new ConstructorMirrorMethod(vm, cons));
            }
        } else {
            for (MethodMirror method : wrapped.getDeclaredMethods(false)) {
                if (method.getName().equals(name)) {
                    result.add(new MethodMirrorMethod(vm, method));
                }
            }
        }
        return result;
    }

    @Override
    public List<Method> methodsByName(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int minorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceType> nestedTypes() {
        return Collections.emptyList();
    }

    @Override
    public String sourceDebugExtension() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public String sourceName() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<String> sourceNames(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<String> sourcePaths(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Field> visibleFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Method> visibleMethods() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value getValue(Field field) {
        return vm.getValue(wrapped.getStaticFieldValues(), field);
    }

    @Override
    public Map<Field, Value> getValues(List<? extends Field> fields) {
        Map<Field, Value> result = new HashMap<Field, Value>();
        for (Field field : fields) {
            result.put(field, getValue(field));
        }
        return result;
    }
    
    public List<InterfaceType> interfaces() {
        List<InterfaceType> result = new ArrayList<InterfaceType>();
        for (ClassMirror i : wrapped.getInterfaceMirrors()) {
            result.add((InterfaceType)vm.typeForClassMirror(i));
        }
        return result;
    }
}
