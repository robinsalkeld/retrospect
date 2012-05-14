package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.lang.reflect.Constructor;
import java.util.List;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class ClassStubs {

    public static boolean isInterface(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isInterface();
    }
    
    public static boolean isPrimitive(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isPrimitive();
    }
    
    public static boolean isArray(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isArray();
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
        
        ClassMirror klassMirror = Reflection.classMirrorForName(vm, realName, resolve, loaderMirror);
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
            
            Reflection.setField(inst, "clazz", c.getDeclaringClass());
            Reflection.setField(inst, "slot", c.getSlot());
            Reflection.setField(inst, "modifiers", c.getModifiers());
            Reflection.setField(inst, "parameterTypes", Reflection.toArray(classClass, c.getParameterTypes()));
            Reflection.setField(inst, "exceptionTypes", Reflection.toArray(classClass, c.getExceptionTypes()));
            Reflection.setField(inst, "annotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(c.getAnnotations())));
            Reflection.setField(inst, "parameterAnnotations", Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(c.getParameterAnnotations())));
            Reflection.setField(inst, "signature", Reflection.makeString(vm, c.getSignature()));
            
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
}
