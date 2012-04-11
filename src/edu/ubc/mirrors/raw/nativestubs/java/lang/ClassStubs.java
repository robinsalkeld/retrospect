package edu.ubc.mirrors.raw.nativestubs.java.lang;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

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
    
    private static ClassMirror loadElementClassMirror(VirtualMachineMirror vm, Type elementType, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        if (elementType.equals(Type.BOOLEAN_TYPE)) {
            return new NativeClassMirror(Boolean.TYPE);
        } else if (elementType.equals(Type.BYTE_TYPE)) {
            return new NativeClassMirror(Byte.TYPE);
        } else if (elementType.equals(Type.CHAR_TYPE)) {
            return new NativeClassMirror(Character.TYPE);
        } else if (elementType.equals(Type.SHORT_TYPE)) {
            return new NativeClassMirror(Short.TYPE);
        } else if (elementType.equals(Type.INT_TYPE)) {
            return new NativeClassMirror(Integer.TYPE);
        } else if (elementType.equals(Type.LONG_TYPE)) {
            return new NativeClassMirror(Long.TYPE);
        } else if (elementType.equals(Type.FLOAT_TYPE)) {
            return new NativeClassMirror(Float.TYPE);
        } else if (elementType.equals(Type.DOUBLE_TYPE)) {
            return new NativeClassMirror(Double.TYPE);
        } else {
            return classMirrorForName(vm, elementType.getClassName(), resolve, loader);
        }
    }
    
    public static ClassMirror classMirrorForName(VirtualMachineMirror vm, String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        Type type = Type.getObjectType(name);
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror = loadElementClassMirror(vm, elementType, resolve, loader);
            return new ArrayClassMirror(loader, type.getDimensions(), elementClassMirror);
        } else if (loader == null) {
            return vm.findBootstrapClassMirror(name);
        } else {
            return MirageClassLoader.loadClassMirror(vm, loader, name);
        }
    }
}
