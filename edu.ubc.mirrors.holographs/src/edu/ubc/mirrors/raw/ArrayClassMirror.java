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
package edu.ubc.mirrors.raw;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BlankClassMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class ArrayClassMirror extends BlankClassMirror {

    private final ClassMirror elementClassMirror;
    private final int dims;
    private final Type arrayType;
    
    public ArrayClassMirror(int dims, ClassMirror elementClassMirror) {
        this.elementClassMirror = elementClassMirror;
        if (dims < 0) {
            throw new IllegalArgumentException();
        }
        this.dims = dims;
        this.arrayType = Reflection.makeArrayType(dims, Reflection.typeForClassMirror(elementClassMirror));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayClassMirror)) {
            return false;
        }
        
        ArrayClassMirror other = (ArrayClassMirror)obj;
        return elementClassMirror.equals(other.elementClassMirror)
            && dims == other.dims;
    }
    
    @Override
    public int hashCode() {
        return dims * elementClassMirror.hashCode() + 7;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return elementClassMirror.getVM();
    }
    
    public ClassMirror getElementClassMirror() {
        return elementClassMirror;
    }
    
    public Type getArrayType() {
        return arrayType;
    }
    
    @Override
    public FieldMirror getDeclaredField(final String name) {
        return null;
    }

    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        return Collections.emptyList();
    }
    
    @Override
    public String getClassName() {
        return arrayType.getClassName();
    }
    
    @Override
    public String getSignature() {
        return arrayType.getDescriptor();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return elementClassMirror.getLoader();
    }

    @Override
    public byte[] getBytecode() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (dims == 1) {
            return elementClassMirror;
        } else { 
            return elementClassMirror.getVM().getArrayClass(dims - 1, elementClassMirror);
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return elementClassMirror.getVM().findBootstrapClassMirror(Object.class.getName());
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    public static List<ClassMirror> getInterfaceMirrorsForArrays(VirtualMachineMirror vm) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(2);
        result.add(vm.findBootstrapClassMirror(Cloneable.class.getName()));
        result.add(vm.findBootstrapClassMirror(Serializable.class.getName()));
        return result;
    }
    
    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return getInterfaceMirrorsForArrays(elementClassMirror.getVM());
    }
    
    @Override
    public List<FieldMirror> getDeclaredFields() {
        return Collections.emptyList();
    }
    
    @Override
    public List<ObjectMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getName() + ": " + getClassName();
    }

    @Override
    public MethodMirror getDeclaredMethod(String name, String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException(name);
    }

    @Override
    public MethodMirror getMethod(String name, String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException(name);
    }

    @Override
    public ConstructorMirror getConstructor(String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException();
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        return Collections.emptyList();
    }
    
    @Override
    public int getModifiers() {
        return Modifier.FINAL | elementClassMirror.getModifiers();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        // TODO-RS: Semantics are defined perfectly well by java.lang.reflect.Array#newInstance,
        // but the holograms architecture won't call this.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
     // TODO-RS: Semantics are defined perfectly well by java.lang.reflect.Array#newInstance,
        // but the holograms architecture won't call this.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean initialized() {
        return true;
    }

    public int getDimensions() {
        return dims;
    }
    
    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
    }
    
    @Override
    public List<ClassMirror> getSubclassMirrors() {
        // Hopefully don't need to ever implement this
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror getEnclosingClassMirror() {
        return null;
    }
    
    @Override
    public MethodMirror getEnclosingMethodMirror() {
        return null;
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        return Collections.emptyList();
    }
    
    @Override
    public MirrorLocation locationOfLine(int lineNumber) {
        return null;
    }
    
    @Override
    public boolean canLock() {
        return true;
    }
}
