package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class UnsafeStubs extends NativeStubs {

    private Map<String, Integer> arrayBaseOffsets;
    
    public UnsafeStubs(ClassHolograph klass) {
	super(klass);
    }

    private void inferArrayBaseOffsets() {
        if (arrayBaseOffsets == null) {
            arrayBaseOffsets = new HashMap<String, Integer>();
            
            try {
                // Java 7 added these static fields at some point
                if (klass.getDeclaredField("ARRAY_BOOLEAN_BASE_OFFSET") != null) {
                    InstanceMirror staticFieldValues = klass.getStaticFieldValues();
                    arrayBaseOffsets.put("[Z", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_BOOLEAN_BASE_OFFSET")));
                    arrayBaseOffsets.put("[B", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_BYTE_BASE_OFFSET")));
                    arrayBaseOffsets.put("[C", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_CHAR_BASE_OFFSET")));
                    arrayBaseOffsets.put("[S", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_SHORT_BASE_OFFSET")));
                    arrayBaseOffsets.put("[I", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_INT_BASE_OFFSET")));
                    arrayBaseOffsets.put("[J", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_LONG_BASE_OFFSET")));
                    arrayBaseOffsets.put("[F", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_FLOAT_BASE_OFFSET")));
                    arrayBaseOffsets.put("[D", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_DOUBLE_BASE_OFFSET")));
                    arrayBaseOffsets.put("[Ljava.lang.Object;", staticFieldValues.getInt(klass.getDeclaredField("ARRAY_OBJECT_BASE_OFFSET")));
                } else {
                    // TODO-RS: Limited special case - do better!
                    ClassMirror byteArrayAccessClass = getVM().findBootstrapClassMirror("sun.security.provider.ByteArrayAccess");
                    arrayBaseOffsets.put("[B", byteArrayAccessClass.getStaticFieldValues().getInt(byteArrayAccessClass.getDeclaredField("byteArrayOfs")));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @StubMethod
    public ObjectMirror getObject(InstanceMirror unsafe, ObjectMirror object, int offset) {
        // TODO-RS: Will be different when concurrent access is supported.
        return getObjectVolatile(unsafe, object, offset);
    }
    
    @StubMethod
    public ObjectMirror getObject(InstanceMirror unsafe, ObjectMirror object, long offset) {
        // TODO-RS: Will be different when concurrent access is supported.
        return getObjectVolatile(unsafe, object, offset);
    }
    
    @StubMethod
    public ObjectMirror getObjectVolatile(InstanceMirror unsafe, ObjectMirror object, long offset) {
        ObjectArrayMirror array = (ObjectArrayMirror)object;
        // TODO-RS: Here (and several other similar places in this class)
        // these offset calculations need to be verified.
        return array.get((int)((offset - arrayBaseOffset(unsafe, array.getClassMirror())) / 4));
    }
    
    @StubMethod
    public int getInt(InstanceMirror unsafe, ObjectMirror object, long offset) {
        ArrayMirror array = (ArrayMirror)object;
        // TODO-RS: Just supporting enough for the common usage of this function
        // in ConcurrentHashMap - to be completed.
        String className = array.getClassMirror().getSignature();
        if (className.equals("[I")) {
            int index = (int)((offset - arrayBaseOffset(unsafe, array.getClassMirror())) / 4);
            return ((IntArrayMirror)array).getInt(index);
        } else if (className.equals("[B")) {
            int index = (int)(offset - arrayBaseOffset(unsafe, array.getClassMirror()));
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
    
    @StubMethod
    public void putInt(InstanceMirror unsafe, ObjectMirror object, long offset, int value) {
        ArrayMirror array = (ArrayMirror)object;
        // TODO-RS: Just supporting enough for the common usage of this function
        // in ConcurrentHashMap - to be completed.
        String signature = array.getClassMirror().getSignature();
        if (signature.equals("[I")) {
            int index = (int)((offset - arrayBaseOffset(unsafe, array.getClassMirror())) / 4);
            ((IntArrayMirror)array).setInt(index, value);
        } else if (signature.equals("[B")) {
            int index = (int)offset - arrayBaseOffset(unsafe, array.getClassMirror());
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
    
    @StubMethod
    public void putObject(InstanceMirror unsafe, ObjectMirror mirror, long offset, ObjectMirror element) throws IllegalAccessException, NoSuchFieldException {
        // TODO-RS: Will be different when concurrent access is supported.
        putOrderedObject(unsafe, mirror, offset, element);
    }
    
    @StubMethod
    public void putOrderedObject(InstanceMirror unsafe, ObjectMirror mirror, long offset, ObjectMirror element) throws IllegalAccessException, NoSuchFieldException {
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            array.set((int)((offset - arrayBaseOffset(unsafe, mirror.getClassMirror())) / 4), element);
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            FieldMirror field = fieldForOffset(instance, offset);
            instance.set(field, element);
        } else {
            throw new InternalError();
        }
    }
    
    private int arrayBaseOffset(InstanceMirror unsafe, ClassMirror klass) {
        inferArrayBaseOffsets();
        String signature = klass.getComponentClassMirror().isPrimitive() ? klass.getSignature() : "[Ljava.lang.Object;";
        return arrayBaseOffsets.get(signature);
    }
    
    @StubMethod
    public long objectFieldBaseOffset() {
        // TODO-RS: Actually figure out dynamically.
        return 12;
    }
    
    @StubMethod
    public ObjectMirror staticFieldBase(InstanceMirror unsafe, InstanceMirror fieldMirror) {
        ClassMirror klass = (ClassMirror)HolographInternalUtils.getField(fieldMirror, "clazz");
        return klass.getStaticFieldValues();
    }
    
    @StubMethod
    public long staticFieldOffset(InstanceMirror unsafe, InstanceMirror fieldMirror) {
        return fieldOffset(unsafe, fieldMirror, true);
    }
    
    @StubMethod
    public long objectFieldOffset(InstanceMirror unsafe, InstanceMirror fieldMirror) {
        return fieldOffset(unsafe, fieldMirror, false);
    }
    
    private long fieldOffset(InstanceMirror unsafe, InstanceMirror fieldMirror, boolean isStatic) {
        ClassMirror klass = (ClassMirror)HolographInternalUtils.getField(fieldMirror, "clazz");
        String fieldName = Reflection.getRealStringForMirror((InstanceMirror)HolographInternalUtils.getField(fieldMirror, "name"));
        long fieldOffset = objectFieldBaseOffset();
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ClassMirror c = klass; c != null; c = c.getSuperClassMirror()) {
            classes.add(c);
        }
        Collections.reverse(classes);
        for (ClassMirror c : classes) {
            List<FieldMirror> orderedFields = c.getDeclaredFields();
            Collections.sort(orderedFields, new FieldSorter());
            for (FieldMirror declaredField : orderedFields) {
                if (Modifier.isStatic(declaredField.getModifiers()) == isStatic) {
                    if (fieldName.equals(declaredField.getName())) {
                        return fieldOffset;
                    }
                    
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
                }
            }
        }
        
        throw new InternalError("wrong field name???");
    }
    
    private FieldMirror fieldForOffset(InstanceMirror instance, long offset) {
        boolean isStatic;
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        if (instance instanceof StaticFieldValuesMirror) {
            ClassMirror klass = ((StaticFieldValuesMirror)instance).forClassMirror();
            isStatic = true;
            classes.add(klass);
        } else {
            isStatic = false;
            for (ClassMirror c = instance.getClassMirror(); c != null; c = c.getSuperClassMirror()) {
                classes.add(c);
            }
            Collections.reverse(classes);
        }
        long fieldOffset = objectFieldBaseOffset();
        for (ClassMirror klass : classes) {
            List<FieldMirror> orderedFields = klass.getDeclaredFields();
            Collections.sort(orderedFields, new FieldSorter());
            for (FieldMirror field : orderedFields) {
                if (Modifier.isStatic(field.getModifiers()) == isStatic) {
                    if (fieldOffset == offset) {
                        return field;
                    } else if (fieldOffset > offset) {
                        throw new InternalError("Non-aligned offset???");
                    }
                    
                    ClassMirror fieldType = field.getType();
                    if (fieldType.isPrimitive()) {
                        String name = fieldType.getClassName();
                        if (name.equals("int")) {
                            fieldOffset += 4;
                        } else if (name.equals("long")) {
                            fieldOffset += 4;
                        } else {
                            throw new InternalError("Unsupported type: " + name);
                        }
                    } else {
                        fieldOffset += 4;
                    }
                }
            }
        }
        throw new InternalError("offset overflow???");
    }
    
    private static class FieldSorter implements Comparator<FieldMirror> {

        private int getTypeOrdinal(FieldMirror field) {
            switch (Reflection.typeForClassMirror(field.getType()).getSort()) {
            case Type.DOUBLE:
            case Type.LONG: 
                return 0;
            case Type.INT:
            case Type.FLOAT: 
                return 1;
            case Type.SHORT:
            case Type.CHAR: 
                return 2;
            case Type.BOOLEAN:
            case Type.BYTE: 
                return 3;
            case Type.OBJECT:
            case Type.ARRAY:
                return 4;
            default:
                throw new IllegalArgumentException();
            }
        }
        
        @Override
        public int compare(FieldMirror first, FieldMirror second) {
            return Integer.compare(getTypeOrdinal(first), getTypeOrdinal(second));
        }
    }
    
    @StubMethod
    public boolean compareAndSwapObject(InstanceMirror unsafe, ObjectMirror mirror, long offset, ObjectMirror oldValue, ObjectMirror newValue) throws IllegalAccessException, NoSuchFieldException {
        if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror array = (ObjectArrayMirror)mirror;
            int index = (int)((offset - arrayBaseOffset(unsafe, array.getClassMirror())) / 4);
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
    
    @StubMethod
    public boolean compareAndSwapLong(InstanceMirror unsafe, ObjectMirror mirror, long offset, long oldValue, long newValue) throws IllegalAccessException, NoSuchFieldException {
        if (mirror instanceof LongArrayMirror) {
            LongArrayMirror array = (LongArrayMirror)mirror;
            // TODO-RS: Need to be much more careful about this!
            int index = (int)((offset - arrayBaseOffset(unsafe, array.getClassMirror())) / 8);
            long current = array.getLong(index);
            if (current == oldValue) {
                array.setLong(index, newValue);
                return true;
            } else {
                return false;
            }
        } else if (mirror instanceof InstanceMirror) {
            InstanceMirror instance = (InstanceMirror)mirror;
            FieldMirror field = fieldForOffset(instance, offset);

            long current = instance.getLong(field);
            if (current == oldValue) {
                instance.setLong(field, newValue);
                return true;
            } else {
                return false;
            }
        } else {
            throw new InternalError();
        }
    }
    
    @StubMethod
    public ClassMirror defineClass(InstanceMirror unsafe, InstanceMirror internalName, ByteArrayMirror b, int off, int len,
            ClassMirrorLoader classLoader, InstanceMirror pd) {

        String realInternalName = Reflection.getRealStringForMirror(internalName);
        String realClassName = realInternalName.replace('/', '.');
        InstanceMirror className = Reflection.makeString(getVM(), realClassName);
        
        return ClassLoaderStubs.defineClass(classLoader, className, b, off, len, pd, null, true);
    }
    
    @StubMethod
    public ObjectMirror allocateInstance(InstanceMirror unsafe, ClassMirror classMirror) {
        return classMirror.newRawInstance();
    }
    
    @StubMethod
    public void ensureClassInitialized(InstanceMirror unsafe, ClassMirror classMirror) {
        ((ClassHolograph)classMirror).ensureInitialized();
    }
}
