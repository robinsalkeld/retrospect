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
package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
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
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;

public class MirrorsReferenceType extends MirrorsMirrorWithModifiers implements ReferenceType {

    protected final ClassMirror wrapped;
    private List<Field> allFields;
    
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
        if (allFields == null) {
            allFields = new ArrayList<Field>();
            addAllFields(vm, wrapped, allFields);
        }
        return allFields;
    }

    private static void addAllFields(MirrorsVirtualMachine vm, ClassMirror klass, List<Field> fields) {
        for (FieldMirror field : klass.getDeclaredFields()) {
            fields.add(new MirrorsField(vm, field));
        }
        if (klass.getSuperClassMirror() != null) {
            addAllFields(vm, klass.getSuperClassMirror(), fields);
        }
        for (ClassMirror i : klass.getInterfaceMirrors()) {
            addAllFields(vm, i, fields);
        }
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
        return "Java";
    }

    @Override
    public boolean failedToInitialize() {
        return false;
    }

    @Override
    public Field fieldByName(String name) {
        return new MirrorsField(vm, wrapped.getDeclaredField(name));
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
        for (ObjectMirror instance : wrapped.getInstances().subList(0, (int)maxInstances)) {
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
        
        for (InterfaceType interfaceType : interfaces()) {
            result.addAll(interfaceType.methodsByName(name));
        }
        
        return result;
    }

    @Override
    public List<Method> methodsByName(String name, String signature) {
        List<Method> result = new ArrayList<Method>();
        Type methodType = Type.getType(signature);
        if (name.equals("<init>")) {
            for (ConstructorMirror cons : wrapped.getDeclaredConstructors(false)) {
                if (methodType.equals(Reflection.getMethodType(cons))) {
                    result.add(new ConstructorMirrorMethod(vm, cons));
                }
            }
        } else {
            for (MethodMirror method : wrapped.getDeclaredMethods(false)) {
                if (method.getName().equals(name) && methodType.equals(Reflection.getMethodType(method))) {
                    result.add(new MethodMirrorMethod(vm, method));
                }
            }
        }
        
        for (InterfaceType interfaceType : interfaces()) {
            result.addAll(interfaceType.methodsByName(name));
        }
        
        return result;
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
        return new ArrayList<Field>(visibleFieldsByName().values());
    }

    protected Map<String, Field> visibleFieldsByName() {
        Map<String, Field> result = new HashMap<String, Field>();
        Set<String> ambiguous = new HashSet<String>();
        
        for (InterfaceType i : interfaces()) {
            mergeVisibleFields(result, ((MirrorsReferenceType)i).visibleFieldsByName(), ambiguous);
        }
        
        if (this instanceof ClassType) {
            MirrorsClassType superclass = (MirrorsClassType)((ClassType)this).superclass();
            if (superclass != null) {
                mergeVisibleFields(result, superclass.visibleFieldsByName(), ambiguous);
            }
        }
        
        for (Field field : fields()) {
            result.put(field.name(), field);
        }
        
        return result;
    }
    
    private void mergeVisibleFields(Map<String, Field> result, Map<String, Field> others, Set<String> ambiguous) {
        for (Map.Entry<String, Field> entry : others.entrySet()) {
            String name = entry.getKey();
            Field f = result.get(name);
            Field otherField = entry.getValue();
            if (!ambiguous.contains(name)) {
                if (f == null) {
                    result.put(name, otherField);
                } else if (!f.equals(otherField)) {
                    // Ambiguous
                    result.remove(name);
                    ambiguous.add(name);
                }
            }
        }
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
        for (Object field : fields) {
            result.put((Field)field, getValue((Field)field));
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
