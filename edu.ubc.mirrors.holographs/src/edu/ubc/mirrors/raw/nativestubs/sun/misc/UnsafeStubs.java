package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class UnsafeStubs extends NativeStubs {

    public UnsafeStubs(ClassHolograph klass) {
	super(klass);
    }

    public ObjectMirror getObject(InstanceMirror unsafe, ObjectMirror object, int offset) {
        // TODO-RS: Will be different when concurrent access is supported.
        return getObjectVolatile(unsafe, object, offset);
    }
    
    public ObjectMirror getObject(InstanceMirror unsafe, ObjectMirror object, long offset) {
        // TODO-RS: Will be different when concurrent access is supported.
        return getObjectVolatile(unsafe, object, offset);
    }
    
    public ObjectMirror getObjectVolatile(InstanceMirror unsafe, ObjectMirror object, long offset) {
        ObjectArrayMirror array = (ObjectArrayMirror)object;
        // TODO-RS: Here (and several other similar places in this class)
        // these offset calculations need to be verified.
        return array.get((int)((offset - arrayBaseOffsetByClassName(array.getClassMirror().getClassName())) / 4));
    }
    
    public int getInt(InstanceMirror unsafe, ObjectMirror object, long offset) {
        ArrayMirror array = (ArrayMirror)object;
        // TODO-RS: Just supporting enough for the common usage of this function
        // in ConcurrentHashMap - to be completed.
        String className = array.getClassMirror().getClassName();
        if (className.equals("[I")) {
            int index = (int)((offset - arrayBaseOffsetByClassName(className)) / 4);
            return ((IntArrayMirror)array).getInt(index);
        } else if (className.equals("[B")) {
            int index = (int)(offset - arrayBaseOffsetByClassName(className));
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
    
    public void putInt(InstanceMirror unsafe, ObjectMirror object, long offset, int value) {
        ArrayMirror array = (ArrayMirror)object;
        // TODO-RS: Just supporting enough for the common usage of this function
        // in ConcurrentHashMap - to be completed.
        String className = array.getClassMirror().getClassName();
        if (className.equals("[I")) {
            int index = (int)((offset - arrayBaseOffsetByClassName(className)) / 4);
            ((IntArrayMirror)array).setInt(index, value);
        } else if (className.equals("[B")) {
            int index = (int)offset - arrayBaseOffsetByClassName(className);
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
    
    public void putOrderedObject(InstanceMirror unsafe, ObjectMirror mirror, long offset, ObjectMirror element) throws IllegalAccessException, NoSuchFieldException {
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            array.set((int)((offset - arrayBaseOffsetByClassName(mirror.getClassMirror().getClassName())) / 4), element);
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            FieldMirror field = fieldForOffset(instance, offset);
            instance.set(field, element);
        } else {
            throw new InternalError();
        }
    }
    
    public int arrayBaseOffset(InstanceMirror unsafe, ClassMirror klass) {
        return arrayBaseOffsetByClassName(klass.getClassName());
    }
    
    public int arrayBaseOffsetByClassName(String name) {
        // TODO-RS: Actually figure out dynamically.
        return 12;
    }
    public long objectFieldOffset(InstanceMirror unsafe, InstanceMirror fieldMirror) {
        ClassMirror klass = (ClassMirror)HolographInternalUtils.getField(fieldMirror, "clazz");
        String fieldName = Reflection.getRealStringForMirror((InstanceMirror)HolographInternalUtils.getField(fieldMirror, "name"));
        long fieldOffset = 12;
        for (FieldMirror declaredField : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                ClassMirror fieldType = declaredField.getType();
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
                
                if (fieldName.equals(declaredField.getName())) {
                    return fieldOffset;
                }
            }
        }
        
        throw new InternalError("wrong field name???");
    }
    
    private FieldMirror fieldForOffset(InstanceMirror instance, long offset) {
        ClassMirror klass = instance.getClassMirror();
        long fieldOffset = 12;
        for (FieldMirror field : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                ClassMirror fieldType = field.getType();
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
                    return field;
                } else if (fieldOffset > offset) {
                    throw new InternalError("Non-aligned offset???");
                }
            }
        }
        
        throw new InternalError("offset overflow???");
    }
    
    public boolean compareAndSwapObject(InstanceMirror unsafe, ObjectMirror mirror, long offset, ObjectMirror oldValue, ObjectMirror newValue) throws IllegalAccessException, NoSuchFieldException {
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            int index = (int)((offset - arrayBaseOffsetByClassName(array.getClassMirror().getClassName())) / 4);
            ObjectMirror current = array.get(index);
            if (current == oldValue) {
                array.set(index, newValue);
                return true;
            } else {
                return false;
            }
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            FieldMirror field = fieldForOffset(instance, offset);

            ObjectMirror current = instance.get(field);
            if (current == oldValue) {
                instance.set(field, newValue);
                return true;
            } else {
                return false;
            }
        } else {
            throw new InternalError();
        }
    }
    
    public long getLong(InstanceMirror unsafe, long address) {
        // TODO-RS: Need to figure this one out...
        return 0;
    }
    public void putLong(InstanceMirror unsafe, long address, long value) {
        // TODO-RS: Need to figure this one out...
    }
    
    public ClassMirror defineClass(InstanceMirror unsafe, InstanceMirror internalName, ByteArrayMirror b, int off, int len,
            ClassMirrorLoader classLoader, InstanceMirror pd) {

        String realInternalName = Reflection.getRealStringForMirror(internalName);
        String realClassName = realInternalName.replace('/', '.');
        InstanceMirror className = Reflection.makeString(getVM(), realClassName);
        
        return ClassLoaderStubs.defineClass(classLoader, className, b, off, len, pd, null);
    }
    
    public ObjectMirror allocateInstance(InstanceMirror unsafe, ClassMirror classMirror) {
        return classMirror.newRawInstance();
    }
}
