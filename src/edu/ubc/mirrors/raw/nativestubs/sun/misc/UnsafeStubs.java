package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

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
}
