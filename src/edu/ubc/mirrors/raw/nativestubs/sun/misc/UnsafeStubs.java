package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class UnsafeStubs {

    public static Mirage getObject(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, int offset) {
        return getObjectVolatile(classLoaderLiteral, unsafe, object, offset);
    }
    
    public static Mirage getObject(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset) {
        return getObjectVolatile(classLoaderLiteral, unsafe, object, offset);
    }
    
    public static Mirage getObjectVolatile(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset) {
        ObjectArrayMirror array = (ObjectArrayMirror)object.getMirror();
        // TODO-RS: Need to be much more careful about this!
        ObjectMirror element = array.get((int)((offset - 16) / 4));
        return ObjectMirage.make(element);
    }
    
    public static int getInt(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset) {
        ArrayMirror array = (ArrayMirror)object.getMirror();
        // TODO-RS: Need to be more careful about offset calculations!
        String className = array.getClassMirror().getClassName();
        if (className.equals("[I")) {
            int index = (int)((offset - 16) / 4);
            return ((IntArrayMirror)array).getInt(index);
        } else if (className.equals("[B")) {
            int index = (int)(offset - 16);
            ByteArrayMirror bam = (ByteArrayMirror)array;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < 4; i++) {
                buffer.put(i, bam.getByte(index + i));
            }
            return buffer.getInt(0);
        } else {
            throw new InternalError();
        }
    }
    
    public static void putInt(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset, int value) {
        ArrayMirror array = (ArrayMirror)object.getMirror();
        // TODO-RS: Need to be much more careful about this!
        String className = array.getClassMirror().getClassName();
        if (className.equals("[I")) {
            int index = (int)((offset - 16) / 4);
            ((IntArrayMirror)array).setInt(index, value);
        } else if (className.equals("[B")) {
            int index = (int)offset - 16;
            ByteArrayMirror bam = (ByteArrayMirror)array;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(0, value);
            for (int i = 0; i < 4; i++) {
                bam.setByte(index + i, buffer.get(i));
            }
        } else {
            throw new InternalError();
        }
    }
    
    public static void putOrderedObject(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset, Mirage element) throws IllegalAccessException, NoSuchFieldException {
        ObjectMirror mirror = object.getMirror();
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            array.set((int)((offset - 16) / 4), ObjectMirage.getMirror(element));
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            // TODO-RS: Fix ClassMirror interface to return ordered list of FieldMirrors instead
            String field = fieldForOffset(instance, offset);
            instance.getMemberField(field).set(ObjectMirage.getMirror(element));
        } else {
            throw new InternalError();
        }
    }
    
    private static String fieldForOffset(InstanceMirror instance, long offset) {
        ClassMirror klass = instance.getClassMirror();
        long fieldOffset = 12;
        for (Map.Entry<String, ClassMirror> entry : klass.getDeclaredFields().entrySet()) {
            ClassMirror fieldType = entry.getValue();
            if (fieldType.isPrimitive()) {
                String name = fieldType.getClassName();
                if (name.equals("int")) {
                    fieldOffset += 4;
                } else {
                    throw new InternalError("Unsupported type: " + name);
                }
            } else {
                fieldOffset += 4;
            }
            
            if (fieldOffset == offset) {
                return entry.getKey();
            } else if (fieldOffset > offset) {
                throw new InternalError("Non-aligned offset???");
            }
        }
        
        throw new InternalError("offset overflow???");
    }
    
    public static boolean compareAndSwapObject(Class<?> classLoaderLiteral, Mirage unsafe, Mirage object, long offset, Mirage oldValue, Mirage newValue) throws IllegalAccessException, NoSuchFieldException {
        ObjectMirror mirror = object.getMirror();
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            int index = (int)((offset - 16) / 4);
            ObjectMirror current = array.get(index);
            if (current == ObjectMirage.getMirror(oldValue)) {
                array.set(index, newValue.getMirror());
                return true;
            } else {
                return false;
            }
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            // TODO-RS: Fix ClassMirror interface to return ordered list of FieldMirrors instead
            String fieldName = fieldForOffset(instance, offset);
            FieldMirror field = instance.getMemberField(fieldName);

            ObjectMirror current = field.get();
            if (current == ObjectMirage.getMirror(oldValue)) {
                field.set(newValue.getMirror());
                return true;
            } else {
                return false;
            }
        } else {
            throw new InternalError();
        }
    }
    
    public static long getLong(Class<?> classLoaderLiteral, Mirage unsafe, long address) {
        // TODO-RS: Need to figure this one out...
        return 0;
    }
    public static void putLong(Class<?> classLoaderLiteral, Mirage unsafe, long address, long value) {
        // TODO-RS: Need to figure this one out...
    }
    
    public static Mirage defineClass(Class<?> classLoaderLiteral, Mirage unsafe, Mirage internalName, Mirage b, int off, int len,
            Mirage classLoader, Mirage pd) {

        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String realInternalName = Reflection.getRealStringForMirror((InstanceMirror)internalName.getMirror());
        String realClassName = realInternalName.replace('/', '.');
        Mirage className = ObjectMirage.make(Reflection.makeString(vm, realClassName));
        
        return ClassLoaderStubs.defineClass1(classLoaderLiteral, classLoader, className, b, off, len, pd, null);
    }
    
    public static Mirage allocateInstance(Class<?> classLoaderLiteral, Mirage unsafe, Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        InstanceMirror result = classMirror.newRawInstance();
        return ObjectMirage.make(result);
    }
}
