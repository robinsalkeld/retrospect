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
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BlankClassMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class PrimitiveClassMirror extends BlankClassMirror implements ClassMirror {

    private final VirtualMachineMirror vm;
    private final String typeName;
    private final String signature;
    
    public PrimitiveClassMirror(VirtualMachineMirror vm, String typeName, String signature) {
        this.vm = vm;
        this.typeName = typeName;
        this.signature = signature;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PrimitiveClassMirror)) {
            return false;
        }
        
        PrimitiveClassMirror other = (PrimitiveClassMirror)obj;
        return vm.equals(other.vm)
            && typeName.equals(other.typeName);
    }
    
    @Override
    public int hashCode() {
        return typeName.hashCode() * 32 + typeName.hashCode();
    }
    
    @Override
    public FieldMirror getDeclaredField(String name) {
        return null;
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }

    @Override
    public String getClassName() {
        return typeName;
    }

    @Override
    public String getSignature() {
        return signature;
    }
    
    @Override
    public ClassMirrorLoader getLoader() {
        return null;
    }

    @Override
    public byte[] getBytecode() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return null;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return Collections.emptyList();
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
    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException(name);
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new NoSuchMethodException(name);
    }

    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        return Collections.emptyList();
    }

    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        return Collections.emptyList();
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int size) {
        // TODO-RS: Could support this...
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int... dims) {
        // TODO-RS: Could support this...
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean initialized() {
        return true;
    }

    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
    }
    
    @Override
    public List<ClassMirror> getSubclassMirrors() {
        return Collections.emptyList();
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
    public List<AnnotationMirror> getAnnotations() {
        return Collections.emptyList();
    }
}
