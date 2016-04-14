package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

/** A canary object to react to when a holographic object is no longer reachable
 * through holographic references. When this happens, we check if the object is
 * reachable within the wrapped VM, and if not the object is fully, holographically
 * collectable.
 */ 
class GCCanary implements InstanceMirror {
    
    private final InstanceHolograph wrapped;
    
    public GCCanary(InstanceHolograph wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    protected void finalize() throws Throwable {
        wrapped.notHolographicallyReachable();
    }

    @Override
    public ClassMirror getClassMirror() {
        return wrapped.getClassMirror();
    }

    @Override
    public int identityHashCode() {
        return wrapped.identityHashCode();
    }

    @Override
    public void allowCollection(boolean flag) {
        wrapped.allowCollection(flag);
    }

    @Override
    public boolean isCollected() {
        return wrapped.isCollected();
    }

    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        return wrapped.get(field);
    }

    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        return wrapped.getBoolean(field);
    }

    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        return wrapped.getByte(field);
    }

    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        return wrapped.getChar(field);
    }

    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        return wrapped.getShort(field);
    }

    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        return wrapped.getInt(field);
    }

    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        return wrapped.getLong(field);
    }

    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        return wrapped.getFloat(field);
    }

    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        return wrapped.getDouble(field);
    }

    @Override
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
        wrapped.set(field, o);
    }

    @Override
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        wrapped.setBoolean(field, b);
    }

    @Override
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        wrapped.setByte(field, b);
    }

    @Override
    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        wrapped.setChar(field, c);
    }

    @Override
    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        wrapped.setShort(field, s);
    }

    @Override
    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        wrapped.setInt(field, i);
    }

    @Override
    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        wrapped.setLong(field, l);
    }

    @Override
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        wrapped.setFloat(field, f);
    }

    @Override
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        wrapped.setDouble(field, d);
    }
}