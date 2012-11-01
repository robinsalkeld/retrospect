package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class UnsafeStubs extends NativeStubs {

    public UnsafeStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage getObject(Mirage unsafe, Mirage object, int offset) {
        return getObjectVolatile(unsafe, object, offset);
    }
    
    public Mirage getObject(Mirage unsafe, Mirage object, long offset) {
        return getObjectVolatile(unsafe, object, offset);
    }
    
    public Mirage getObjectVolatile(Mirage unsafe, Mirage object, long offset) {
        ObjectArrayMirror array = (ObjectArrayMirror)object.getMirror();
        // TODO-RS: Need to be much more careful about this!
        ObjectMirror element = array.get((int)((offset - 16) / 4));
        return ObjectMirage.make(element);
    }
    
    public int getInt(Mirage unsafe, Mirage object, long offset) {
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
    
    public void putInt(Mirage unsafe, Mirage object, long offset, int value) {
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
    
    public void putOrderedObject(Mirage unsafe, Mirage object, long offset, Mirage element) throws IllegalAccessException, NoSuchFieldException {
        ObjectMirror mirror = object.getMirror();
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            array.set((int)((offset - 16) / 4), ObjectMirage.getMirror(element));
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            FieldMirror field = fieldForOffset(instance, offset);
            field.set(instance, ObjectMirage.getMirror(element));
        } else {
            throw new InternalError();
        }
    }
    
    public long objectFieldOffset(Mirage unsafe, Mirage field) {
        InstanceMirror fieldMirror = (InstanceMirror)field.getMirror();
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
    
    public boolean compareAndSwapObject(Mirage unsafe, Mirage object, long offset, Mirage oldValue, Mirage newValue) throws IllegalAccessException, NoSuchFieldException {
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
            FieldMirror field = fieldForOffset(instance, offset);

            ObjectMirror current = field.get(instance);
            if (current == ObjectMirage.getMirror(oldValue)) {
                field.set(instance, newValue.getMirror());
                return true;
            } else {
                return false;
            }
        } else {
            throw new InternalError();
        }
    }
    
    public long getLong(Mirage unsafe, long address) {
        // TODO-RS: Need to figure this one out...
        return 0;
    }
    public void putLong(Mirage unsafe, long address, long value) {
        // TODO-RS: Need to figure this one out...
    }
    
    public Mirage defineClass(Mirage unsafe, Mirage internalName, Mirage b, int off, int len,
            Mirage classLoader, Mirage pd) {

        String realInternalName = Reflection.getRealStringForMirror((InstanceMirror)internalName.getMirror());
        String realClassName = realInternalName.replace('/', '.');
        Mirage className = ObjectMirage.make(Reflection.makeString(getVM(), realClassName));
        
        return ClassLoaderStubs.defineClass(classLoader, className, b, off, len, pd, null);
    }
    
    public Mirage allocateInstance(Mirage unsafe, Mirage klass) {
        ClassMirror classMirror = (ClassMirror)klass.getMirror();
        InstanceMirror result = classMirror.newRawInstance();
        return ObjectMirage.make(result);
    }
}
