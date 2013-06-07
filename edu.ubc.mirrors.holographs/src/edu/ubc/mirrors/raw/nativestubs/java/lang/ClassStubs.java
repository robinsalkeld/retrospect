package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ConstantPoolReader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class ClassStubs extends NativeStubs {

    public ClassStubs(ClassHolograph klass) {
	super(klass);
    }

    public InstanceMirror getName0(ClassMirror klass) {
        String name = klass.getClassName();
        if (name == null) {
            throw new NullPointerException();
        }
        return Reflection.makeString(getVM(), name);
    }
    
    public boolean isInterface(ClassMirror klass) {
        return klass.isInterface();
    }
    
    public boolean isPrimitive(ClassMirror klass) {
        return klass.isPrimitive();
    }
    
    public boolean isArray(ClassMirror klass) {
        return klass.isArray();
    }
    
    public ClassMirror getComponentType(ClassMirror klass) {
        return klass.getComponentClassMirror();
    }
    
    public ClassMirrorLoader getClassLoader0(ClassMirror klass) {
        return klass.getLoader();
    }
    
    public ClassMirror forName0(InstanceMirror name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException, MirrorInvocationTargetException {
        String realName = Reflection.getRealStringForMirror(name); 
        return Reflection.classMirrorForName(getVM(), ThreadHolograph.currentThreadMirror(), realName, resolve, loader);
    }
    
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
    
    public ObjectArrayMirror getDeclaredMethods0(ClassMirror classMirror, boolean publicOnly) {
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
        return result;
    }
    
    public ObjectArrayMirror getDeclaredFields0(ClassMirror classMirror, boolean publicOnly) {
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
        return result;
    }
    
    public int getModifiers(ClassMirror classMirror) {
        return classMirror.getModifiers();
    }
    
    public ClassMirror getSuperclass(ClassMirror classMirror) {
        return classMirror.getSuperClassMirror();
    }
    
    public ObjectArrayMirror getInterfaces(ClassMirror classMirror) {
        VirtualMachineMirror vm = getVM();
        
        List<ClassMirror> interfaces = classMirror.getInterfaceMirrors();
        return  Reflection.toArray(vm.findBootstrapClassMirror(Class.class.getName()), interfaces);
    }
    
    public boolean isInstance(ClassMirror classMirror, ObjectMirror o) {
        return Reflection.isInstance(classMirror, o);
    }
    
    public boolean isAssignableFrom(ClassMirror thiz, ClassMirror other) {
        return Reflection.isAssignableFrom(thiz, other);
    }
    
    public void setSigners(ClassMirror thiz, ObjectArrayMirror signers) {
        // TODO-RS
    }
    
    public boolean desiredAssertionStatus0(ClassMirror klass) {
        // TODO-RS
        return false;
    }
    
    public ByteArrayMirror getRawAnnotations(ClassMirror classMirror) {
        byte[] bytes = classMirror.getRawAnnotations();
        return (ByteArrayMirror)Reflection.copyArray(classMirror.getVM(), new NativeByteArrayMirror(bytes));
    }
    
    public InstanceMirror getConstantPool(ClassMirror classMirror) {
        ConstantPoolReader reader = new ConstantPoolReader(classMirror);
        InstanceMirror result = classMirror.getVM().findBootstrapClassMirror("sun.reflect.ConstantPool").newRawInstance();
        HolographInternalUtils.setField(result, "constantPoolOop", reader);
        return result;
    }
    
    public ClassMirror getPrimitiveClass(InstanceMirror name) {
        VirtualMachineMirror vm = getVM();
        
        String realName = Reflection.getRealStringForMirror(name);
	return vm.getPrimitiveClass(realName);
    }
}
