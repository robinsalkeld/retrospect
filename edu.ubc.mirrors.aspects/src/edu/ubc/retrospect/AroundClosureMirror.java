package edu.ubc.retrospect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ObjectMirror;

public class AroundClosureMirror implements InstanceMirror {

    private final InstanceMirror wrapped;
    private final MirrorInvocationHandler handler;
    
    public AroundClosureMirror(InstanceMirror wrapped, MirrorInvocationHandler handler) {
        this.wrapped = wrapped;
        this.handler = handler;
    }

    public MirrorInvocationHandler getHandler() {
        return handler;
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
