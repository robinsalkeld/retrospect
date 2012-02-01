package edu.ubc.mirrors.jhat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirage;

public class HeapDumpFieldMirror implements FieldMirror {

    private final Field field;
    
    public HeapDumpFieldMirror(Field field) {
        this.field = field;
    }
    
    public Class<?> getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get() throws IllegalAccessException {
        ObjectReference ref = (ObjectReference)field.getValue();
        IObject object;
        try {
            object = ref.getObject();
        } catch (SnapshotException e) {
            throw new InternalError();
        }
        return ObjectMirage.<Object>make(new HeapDumpObjectMirror(object));
    }

    public boolean getBoolean() throws IllegalAccessException {
        return (Boolean)field.getValue();
    }

    public byte getByte() throws IllegalAccessException {
        return (Byte)field.getValue();
    }

    public char getChar() throws IllegalAccessException {
        return (Character)field.getValue();
    }

    public short getShort() throws IllegalAccessException {
        return (Short)field.getValue();
    }

    public int getInt() throws IllegalAccessException {
        return (Integer)field.getValue();
    }

    public long getLong() throws IllegalAccessException {
        return (Long)field.getValue();
    }

    public float getFloat() throws IllegalAccessException {
        return (Float)field.getValue();
    }

    public double getDouble() throws IllegalAccessException {
        return (Double)field.getValue();
    }

    public void set(Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setBoolean(boolean b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setByte(byte b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setChar(char c) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setShort(short s) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setInt(int i) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setLong(long l) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setFloat(float f) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public void setDouble(double d) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

}
