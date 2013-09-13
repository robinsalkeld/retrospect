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
package edu.ubc.mirrors;

import java.io.File;
import java.util.List;


public interface ClassMirror extends InstanceMirror {

    public abstract VirtualMachineMirror getVM();
    
    public abstract String getClassName();
    
    public abstract String getSignature();
    
    public abstract ClassMirrorLoader getLoader();
    
    public abstract byte[] getBytecode();
    
    public abstract boolean isPrimitive();
    
    public boolean isArray();
    
    public ClassMirror getComponentClassMirror();
    
    public ClassMirror getSuperClassMirror();
    
    public boolean isInterface();
    
    public List<ClassMirror> getInterfaceMirrors();
    
    public FieldMirror getDeclaredField(String name);
    
    public List<FieldMirror> getDeclaredFields();
    
    public List<ObjectMirror> getInstances();

    public List<ClassMirror> getSubclassMirrors();
    
    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;

    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
    
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly);
    
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly); 
 
    public int getModifiers();
    
    public InstanceMirror newRawInstance();
    
    public ArrayMirror newArray(int size);
    
    public ArrayMirror newArray(int... dims);
    
    public boolean initialized();
    
    // It would feel more natural to return the parsed annotations (i.e. InstanceMirrors)
    // but this works better since the annotations are parsed and instantiated in normal Java code
    // rather than in the VM itself.
    public byte[] getRawAnnotations();
    
    // This fake "object" must return the vm's Object type from getClassMirror()
    public InstanceMirror getStaticFieldValues();
    
    // For VM implementations that can cache this information effectively.
    public void bytecodeLocated(File originalBytecodeLocation);
    
    public ClassMirror getEnclosingClassMirror();
    public MethodMirror getEnclosingMethodMirror();
}
