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
package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.RawAnnotationsWriter;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.ConstantPoolReader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class ClassStubs extends NativeStubs {

    public ClassStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public InstanceMirror getName0(ClassMirror klass) {
        String name = klass.getClassName();
        if (name == null) {
            throw new NullPointerException();
        }

        // See JLS 20.3.2
        if (klass.isArray()) {
            name = Reflection.typeForClassMirror(klass).getDescriptor().replace('/', '.');
        }
        
        return Reflection.makeString(getVM(), name);
    }
    
    @StubMethod
    public boolean isInterface(ClassMirror klass) {
        return klass.isInterface();
    }
    
    @StubMethod
    public boolean isPrimitive(ClassMirror klass) {
        return klass.isPrimitive();
    }
    
    @StubMethod
    public boolean isArray(ClassMirror klass) {
        return klass.isArray();
    }
    
    @StubMethod
    public ClassMirror getComponentType(ClassMirror klass) {
        return klass.getComponentClassMirror();
    }
    
    @StubMethod
    public ClassMirrorLoader getClassLoader0(ClassMirror klass) {
        return klass.getLoader();
    }
    
    @StubMethod
    public ClassMirror forName0(InstanceMirror name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException, MirrorInvocationTargetException {
        String realName = Reflection.getRealStringForMirror(name);
        // This method expects strings like "[Lfoo.Bar;" for array classes
        Type type = Type.getObjectType(realName.replace('.', '/'));
        return Reflection.classMirrorForType(getVM(), ThreadHolograph.currentThreadMirror(), type, resolve, loader);
    }
    
    @StubMethod
    public ObjectArrayMirror getDeclaredConstructors0(ClassMirror classMirror, boolean publicOnly) {
        List<ConstructorMirror> constructors = classMirror.getDeclaredConstructors(publicOnly);
        ClassMirror classClass = getVM().findBootstrapClassMirror(Class.class.getName());
        ClassMirror constructorClass = getVM().findBootstrapClassMirror(Constructor.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)constructorClass.newArray(constructors.size());
        int i = 0;
        for (ConstructorMirror c : constructors) {
            InstanceMirror inst = constructorClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", c.getDeclaringClass());
            HolographInternalUtils.setField(inst, "slot", c.getSlot());
            HolographInternalUtils.setField(inst, "modifiers", c.getModifiers());
            HolographInternalUtils.setField(inst, "parameterTypes", Reflection.toArray(classClass, c.getParameterTypes()));
            HolographInternalUtils.setField(inst, "exceptionTypes", Reflection.toArray(classClass, c.getExceptionTypes()));
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(c.getRawAnnotations())));
            HolographInternalUtils.setField(inst, "parameterAnnotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(c.getRawParameterAnnotations())));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(getVM(), c.getSignature()));
            
            result.set(i++, inst);
        }
        return result;
    }
    
    @StubMethod
    public ObjectArrayMirror getDeclaredMethods0(ClassMirror classMirror, boolean publicOnly) {
        List<MethodMirror> methods = classMirror.getDeclaredMethods(publicOnly);
        ClassMirror classClass = getVM().findBootstrapClassMirror(Class.class.getName());
        ClassMirror methodClass = getVM().findBootstrapClassMirror(Method.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)methodClass.newArray(methods.size());
        int i = 0;
        for (MethodMirror m : methods) {
            InstanceMirror inst = methodClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", m.getDeclaringClass());
            HolographInternalUtils.setField(inst, "name", getVM().getInternedString(m.getName()));
            HolographInternalUtils.setField(inst, "slot", m.getSlot());
            HolographInternalUtils.setField(inst, "modifiers", m.getModifiers());
            HolographInternalUtils.setField(inst, "parameterTypes", Reflection.toArray(classClass, m.getParameterTypes()));
            HolographInternalUtils.setField(inst, "returnType", m.getReturnType());
            HolographInternalUtils.setField(inst, "exceptionTypes", Reflection.toArray(classClass, m.getExceptionTypes()));
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawAnnotations())));
            HolographInternalUtils.setField(inst, "parameterAnnotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawParameterAnnotations())));
            HolographInternalUtils.setField(inst, "annotationDefault", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawAnnotationDefault())));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(getVM(), m.getSignature()));
            
            result.set(i++, inst);
        }
        return result;
    }
    
    @StubMethod
    public ObjectArrayMirror getDeclaredFields0(ClassMirror classMirror, boolean publicOnly) {
        List<FieldMirror> fields = classMirror.getDeclaredFields();
        ClassMirror constructorClass = getVM().findBootstrapClassMirror(Field.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)constructorClass.newArray(fields.size());
        int i = 0;
        for (FieldMirror field : fields) {
            InstanceMirror inst = constructorClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", classMirror);
            // The name must be interned according to spec
            HolographInternalUtils.setField(inst, "name", getVM().getInternedString(field.getName()));
            HolographInternalUtils.setField(inst, "type", field.getType());
            HolographInternalUtils.setField(inst, "slot", i);
            HolographInternalUtils.setField(inst, "modifiers", field.getModifiers());
            // TODO-RS: field annotations/signatures
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(new byte[0])));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(getVM(), ""));
            
            result.set(i++, inst);
        }
        return result;
    }
    
    @StubMethod
    public int getModifiers(ClassMirror classMirror) {
        return classMirror.getModifiers();
    }
    
    @StubMethod
    public ClassMirror getSuperclass(ClassMirror classMirror) {
        return classMirror.getSuperClassMirror();
    }
    
    @StubMethod
    public ObjectArrayMirror getInterfaces(ClassMirror classMirror) {
        VirtualMachineMirror vm = getVM();
        
        List<ClassMirror> interfaces = classMirror.getInterfaceMirrors();
        return  Reflection.toArray(vm.findBootstrapClassMirror(Class.class.getName()), interfaces);
    }
    
    @StubMethod
    public boolean isInstance(ClassMirror classMirror, ObjectMirror o) {
        return Reflection.isInstance(classMirror, o);
    }
    
    @StubMethod
    public boolean isAssignableFrom(ClassMirror thiz, ClassMirror other) {
        return Reflection.isAssignableFrom(thiz, other);
    }
    
    @StubMethod
    public void setSigners(ClassMirror thiz, ObjectArrayMirror signers) {
        // TODO-RS
    }
    
    @StubMethod
    public boolean desiredAssertionStatus0(ClassMirror klass) {
        // TODO-RS
        return false;
    }
    
    @StubMethod
    public ByteArrayMirror getRawAnnotations(ClassMirror classMirror) {
        byte[] bytes = RawAnnotationsWriter.getRawBytes(classMirror.getAnnotations());
        return (ByteArrayMirror)Reflection.copyArray(classMirror.getVM(), new NativeByteArrayMirror(bytes));
    }
    
    @StubMethod
    public InstanceMirror getConstantPool(ClassMirror classMirror) {
        ConstantPoolReader reader = new ConstantPoolReader(classMirror);
        InstanceMirror result = classMirror.getVM().findBootstrapClassMirror("sun.reflect.ConstantPool").newRawInstance();
        HolographInternalUtils.setField(result, "constantPoolOop", reader);
        return result;
    }
    
    @StubMethod
    public ClassMirror getPrimitiveClass(InstanceMirror name) {
        VirtualMachineMirror vm = getVM();
        
        String realName = Reflection.getRealStringForMirror(name);
	return vm.getPrimitiveClass(realName);
    }
    
    @StubMethod
    public ClassMirror getDeclaringClass(ClassMirror klass) {
        return klass.getEnclosingClassMirror();
    }
    
    @StubMethod
    public ObjectArrayMirror getEnclosingMethod0(ClassMirror klass) {
        MethodMirror enclosingMethod = klass.getEnclosingMethodMirror();
        if (enclosingMethod == null) {
            return null;
        } else {
            Type methodType = Reflection.getMethodType(enclosingMethod);
            String methodDesc = methodType.getDescriptor();
            
            return (ObjectArrayMirror)Reflection.toArray(getVM().findBootstrapClassMirror(Object.class.getName()), 
                    enclosingMethod.getDeclaringClass(), 
                    Reflection.makeString(getVM(), enclosingMethod.getName()), 
                    Reflection.makeString(getVM(), methodDesc));
        }
    }
}
