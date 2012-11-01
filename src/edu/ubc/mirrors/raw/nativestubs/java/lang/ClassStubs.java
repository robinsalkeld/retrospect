package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ConstantPoolReader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class ClassStubs extends NativeStubs {

    public ClassStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage getName0(Mirage mirage) {
        String name = ((ClassMirror)mirage.getMirror()).getClassName();
        if (name == null) {
            throw new NullPointerException();
        }
        return ObjectMirage.makeStringMirage(name, klass);
    }
    
    public boolean isInterface(Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isInterface();
    }
    
    public boolean isPrimitive(Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isPrimitive();
    }
    
    public boolean isArray(Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isArray();
    }
    
    public Mirage getComponentType(Mirage mirage) {
        ClassMirror result = ((ClassMirror)mirage.getMirror()).getComponentClassMirror();
        return ObjectMirage.make(result);
    }
    
    public Mirage getClassLoader0(Mirage klass) {
        ClassMirror klassMirror = (ClassMirror)klass.getMirror();
        return (Mirage)ObjectMirage.make(klassMirror.getLoader());
    }
    
    public Mirage forName0(Mirage name, boolean resolve, Mirage loader) throws ClassNotFoundException {
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror()); 
        ClassMirrorLoader loaderMirror = loader == null ? null : (ClassMirrorLoader)loader.getMirror();
        ClassMirror klassMirror = Reflection.classMirrorForName(getVM(), ThreadHolograph.currentThreadMirror(), realName, resolve, loaderMirror);
        return (Mirage)ObjectMirage.make(klassMirror);
    }
    
    public Mirage getDeclaredConstructors0(Mirage theClass, boolean publicOnly) {
        ClassMirror classMirror = (ClassMirror)theClass.getMirror();
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
        return ObjectMirage.make(result);
    }
    
    public Mirage getDeclaredMethods0(Mirage klass, boolean publicOnly) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        List<MethodMirror> methods = classMirror.getDeclaredMethods(publicOnly);
        ClassMirror classClass = getVM().findBootstrapClassMirror(Class.class.getName());
        ClassMirror methodClass = getVM().findBootstrapClassMirror(Method.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)methodClass.newArray(methods.size());
        int i = 0;
        for (MethodMirror m : methods) {
            InstanceMirror inst = methodClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", m.getDeclaringClass());
            HolographInternalUtils.setField(inst, "name", StringStubs.internMirror(Reflection.makeString(getVM(), m.getName())));
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
        return ObjectMirage.make(result);
    }
    
    public Mirage getDeclaredFields0(Mirage klass, boolean publicOnly) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        List<FieldMirror> fields = classMirror.getDeclaredFields();
        ClassMirror constructorClass = getVM().findBootstrapClassMirror(Field.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)constructorClass.newArray(fields.size());
        int i = 0;
        for (FieldMirror field : fields) {
            InstanceMirror inst = constructorClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", classMirror);
            // The name must be interned according to spec
            HolographInternalUtils.setField(inst, "name", StringStubs.internMirror(Reflection.makeString(getVM(), field.getName())));
            HolographInternalUtils.setField(inst, "type", field.getType());
            HolographInternalUtils.setField(inst, "slot", i);
            HolographInternalUtils.setField(inst, "modifiers", field.getModifiers());
            // TODO-RS: field annotations/signatures
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(new byte[0])));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(getVM(), ""));
            
            result.set(i++, inst);
        }
        return ObjectMirage.make(result);
    }
    
    public int getModifiers(Mirage klass) {
        return ((ClassMirror)klass.getMirror()).getModifiers();
    }
    
    public Mirage getSuperclass(Mirage klass) {
        return ObjectMirage.make(((ClassMirror)klass.getMirror()).getSuperClassMirror());
    }
    
    public Mirage getInterfaces(Mirage klass) {
        VirtualMachineMirror vm = getVM();
        
        List<ClassMirror> interfaces = ((ClassMirror)klass.getMirror()).getInterfaceMirrors();
        ObjectArrayMirror result = Reflection.toArray(vm.findBootstrapClassMirror(Class.class.getName()), interfaces);
        return ObjectMirage.make(result);
    }
    
    public boolean isInstance(Mirage klass, Mirage o) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        ObjectMirror oMirror = o.getMirror();
        return Reflection.isInstance(classMirror, oMirror);
    }
    
    public boolean isAssignableFrom(Mirage thiz, Mirage other) {
        return Reflection.isAssignableFrom((ClassMirror)thiz.getMirror(), (ClassMirror)other.getMirror());
    }
    
    public void setSigners(Mirage thiz, Mirage signers) {
        // TODO-RS
    }
    
    public boolean desiredAssertionStatus0(Mirage klass) {
        // TODO-RS
        return false;
    }
    
    public Mirage getRawAnnotations(Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        byte[] bytes = classMirror.getRawAnnotations();
        ByteArrayMirror result = (ByteArrayMirror)Reflection.copyArray(classMirror.getVM(), new NativeByteArrayMirror(bytes));
        return ObjectMirage.make(result);
    }
    
    public Mirage getConstantPool(Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        ConstantPoolReader reader = new ConstantPoolReader(classMirror);
        InstanceMirror result = classMirror.getVM().findBootstrapClassMirror("sun.reflect.ConstantPool").newRawInstance();
        HolographInternalUtils.setField(result, "constantPoolOop", reader);
        return ObjectMirage.make(result);
    }
    
    public Mirage getPrimitiveClass(Mirage name) {
        VirtualMachineMirror vm = getVM();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)ObjectMirage.getMirror(name));
	ClassMirror result = vm.getPrimitiveClass(realName);
	return ObjectMirage.make(result);
    }
}
