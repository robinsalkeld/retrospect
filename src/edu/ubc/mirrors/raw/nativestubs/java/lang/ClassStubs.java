package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.Aspect;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ConstantPoolReader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class ClassStubs {

    public static Mirage getName0(Class<?> classLoaderLiteral, Mirage mirage) {
        String name = ((ClassMirror)mirage.getMirror()).getClassName();
        return ObjectMirage.makeStringMirage(name, classLoaderLiteral);
    }
    
    public static boolean isInterface(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isInterface();
    }
    
    public static boolean isPrimitive(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isPrimitive();
    }
    
    public static boolean isArray(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isArray();
    }
    
    public static Mirage getComponentType(Class<?> classLoaderLiteral, Mirage mirage) {
        ClassMirror result = ((ClassMirror)mirage.getMirror()).getComponentClassMirror();
        return ObjectMirage.make(result);
    }
    
    public static Mirage getClassLoader0(Class<?> classLoaderLiteral, Mirage klass) {
        ClassMirror klassMirror = (ClassMirror)klass.getMirror();
        return (Mirage)ObjectMirage.make(klassMirror.getLoader());
    }
    
    public static Mirage forName0(Class<?> classLoaderLiteral, Mirage name, boolean resolve, Mirage loader) throws ClassNotFoundException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror()); 
        ClassMirrorLoader loaderMirror = loader == null ? null : (ClassMirrorLoader)loader.getMirror();
        ClassMirror klassMirror = Reflection.classMirrorForName(vm, ThreadHolograph.currentThreadMirror(), realName, resolve, loaderMirror);
        return (Mirage)ObjectMirage.make(klassMirror);
    }
    
    public static Mirage getDeclaredConstructors0(Class<?> classLoaderLiteral, Mirage klass, boolean publicOnly) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        List<ConstructorMirror> constructors = classMirror.getDeclaredConstructors(publicOnly);
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        ClassMirror constructorClass = vm.findBootstrapClassMirror(Constructor.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)constructorClass.newArray(constructors.size());
        int i = 0;
        for (ConstructorMirror c : constructors) {
            InstanceMirror inst = constructorClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", c.getDeclaringClass());
            HolographInternalUtils.setField(inst, "slot", c.getSlot());
            HolographInternalUtils.setField(inst, "modifiers", c.getModifiers());
            HolographInternalUtils.setField(inst, "parameterTypes", Reflection.toArray(classClass, c.getParameterTypes()));
            HolographInternalUtils.setField(inst, "exceptionTypes", Reflection.toArray(classClass, c.getExceptionTypes()));
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(c.getAnnotations())));
            HolographInternalUtils.setField(inst, "parameterAnnotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(c.getParameterAnnotations())));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(vm, c.getSignature()));
            
            result.set(i++, inst);
        }
        return ObjectMirage.make(result);
    }
    
    public static Mirage getDeclaredMethods0(Class<?> classLoaderLiteral, Mirage klass, boolean publicOnly) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        List<MethodMirror> methods = classMirror.getDeclaredMethods(publicOnly);
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        ClassMirror methodClass = vm.findBootstrapClassMirror(Method.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)methodClass.newArray(methods.size());
        int i = 0;
        for (MethodMirror m : methods) {
            InstanceMirror inst = methodClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", m.getDeclaringClass());
            HolographInternalUtils.setField(inst, "name", StringStubs.internMirror(Reflection.makeString(vm, m.getName())));
            HolographInternalUtils.setField(inst, "slot", m.getSlot());
            HolographInternalUtils.setField(inst, "modifiers", m.getModifiers());
            HolographInternalUtils.setField(inst, "parameterTypes", Reflection.toArray(classClass, m.getParameterTypes()));
            HolographInternalUtils.setField(inst, "returnType", m.getReturnType());
            HolographInternalUtils.setField(inst, "exceptionTypes", Reflection.toArray(classClass, m.getExceptionTypes()));
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawAnnotations())));
            HolographInternalUtils.setField(inst, "parameterAnnotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawParameterAnnotations())));
            HolographInternalUtils.setField(inst, "annotationDefault", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(m.getRawAnnotationDefault())));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(vm, m.getSignature()));
            
            result.set(i++, inst);
        }
        return ObjectMirage.make(result);
    }
    
    public static Mirage getDeclaredFields0(Class<?> classLoaderLiteral, Mirage klass, boolean publicOnly) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        Map<String, ClassMirror> fields = classMirror.getDeclaredFields();
        ClassMirror constructorClass = vm.findBootstrapClassMirror(Field.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)constructorClass.newArray(fields.size());
        int i = 0;
        for (Map.Entry<String, ClassMirror> entry : fields.entrySet()) {
            InstanceMirror inst = constructorClass.newRawInstance();
            
            HolographInternalUtils.setField(inst, "clazz", classMirror);
            // The name must be interned according to spec
            HolographInternalUtils.setField(inst, "name", StringStubs.internMirror(Reflection.makeString(vm, entry.getKey())));
            HolographInternalUtils.setField(inst, "type", entry.getValue());
            // TODO-RS: Might be time to finally fix the fields API...
            HolographInternalUtils.setField(inst, "slot", 0);
            HolographInternalUtils.setField(inst, "modifiers", Modifier.PUBLIC);
            HolographInternalUtils.setField(inst, "annotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(new byte[0])));
            HolographInternalUtils.setField(inst, "signature", Reflection.makeString(vm, ""));
            
            result.set(i++, inst);
        }
        return ObjectMirage.make(result);
    }
    
    public static int getModifiers(Class<?> classLoaderLiteral, Mirage klass) {
        return ((ClassMirror)klass.getMirror()).getModifiers();
    }
    
    public static Mirage getSuperclass(Class<?> classLoaderLiteral, Mirage klass) {
        return ObjectMirage.make(((ClassMirror)klass.getMirror()).getSuperClassMirror());
    }
    
    public static Mirage getInterfaces(Class<?> classLoaderLiteral, Mirage klass) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        List<ClassMirror> interfaces = ((ClassMirror)klass.getMirror()).getInterfaceMirrors();
        ObjectArrayMirror result = Reflection.toArray(vm.findBootstrapClassMirror(Class.class.getName()), interfaces);
        return ObjectMirage.make(result);
    }
    
    public static boolean isInstance(Class<?> classLoaderLiteral, Mirage klass, Mirage o) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        ObjectMirror oMirror = o.getMirror();
        return Reflection.isInstance(classMirror, oMirror);
    }
    
    public static boolean isAssignableFrom(Class<?> classLoaderLiteral, Mirage thiz, Mirage other) {
        return Reflection.isAssignableFrom((ClassMirror)thiz.getMirror(), (ClassMirror)other.getMirror());
    }
    
    public static void setSigners(Class<?> classLoaderLiteral, Mirage thiz, Mirage signers) {
        // TODO-RS
    }
    
    public static boolean desiredAssertionStatus0(Class<?> classLoaderLiteral, Mirage klass) {
        // TODO-RS
        return false;
    }
    
    public static Mirage getRawAnnotations(Class<?> classLoaderLiteral, Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        byte[] bytes = classMirror.getRawAnnotations();
        ByteArrayMirror result = (ByteArrayMirror)Reflection.copyArray(classMirror.getVM(), new NativeByteArrayMirror(bytes));
        return ObjectMirage.make(result);
    }
    
    public static Mirage getConstantPool(Class<?> classLoaderLiteral, Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        ConstantPoolReader reader = new ConstantPoolReader(classMirror);
        InstanceMirror result = classMirror.getVM().findBootstrapClassMirror("sun.reflect.ConstantPool").newRawInstance();
        HolographInternalUtils.setField(result, "constantPoolOop", reader);
        return ObjectMirage.make(result);
    }
}
