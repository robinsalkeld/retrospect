package edu.ubc.mirrors.jhat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IArray;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirage;

public class HeapDumpArrayElementMirror implements FieldMirror {
    
    private final IArray array;
    private final int index;
    
    public HeapDumpArrayElementMirror(IArray array, int index) {
        this.array = array;
        this.index = index;
    }
    
    public Class<?> getType() {
        // TODO Auto-generated method stub
        return null;
    }
    public Object get() throws IllegalAccessException {
        long address = ((IObjectArray)array).getReferenceArray()[index];
        IObject object;
        try {
            object = array.getSnapshot().getObject(array.getSnapshot().mapAddressToId(address));
        } catch (SnapshotException e) {
            throw new InternalError();
        }
        return ObjectMirage.<Object>make(new HeapDumpObjectMirror(object));
    }
    public boolean getBoolean() throws IllegalAccessException {
        return (Boolean)((IPrimitiveArray)array).getValueAt(index);
    }
    public byte getByte() throws IllegalAccessException {
        return (Byte)((IPrimitiveArray)array).getValueAt(index);
    }
    public char getChar() throws IllegalAccessException {
        return (Character)((IPrimitiveArray)array).getValueAt(index);
    }
    public short getShort() throws IllegalAccessException {
        return (Short)((IPrimitiveArray)array).getValueAt(index);
    }
    public int getInt() throws IllegalAccessException {
        return (Integer)((IPrimitiveArray)array).getValueAt(index);
    }
    public long getLong() throws IllegalAccessException {
        return (Long)((IPrimitiveArray)array).getValueAt(index);
    }
    public float getFloat() throws IllegalAccessException {
        return (Float)((IPrimitiveArray)array).getValueAt(index);
    }
    public double getDouble() throws IllegalAccessException {
        return (Double)((IPrimitiveArray)array).getValueAt(index);
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
