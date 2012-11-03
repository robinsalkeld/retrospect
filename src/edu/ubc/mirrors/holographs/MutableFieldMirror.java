package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;

public class MutableFieldMirror extends WrappingFieldMirror {

    private final ClassHolograph klass;
    private final FieldMirror immutableFieldMirror;
    
    private FieldMirror bytecodeField;
    
    public MutableFieldMirror(ClassHolograph klass, FieldMirror immutableFieldMirror) {
        super(klass.getVM(), immutableFieldMirror);
        this.klass = klass;
        this.immutableFieldMirror = immutableFieldMirror;
    }
    
    @Override
    public String getName() {
        return immutableFieldMirror.getName();
    }
    
    @Override
    public ClassMirror getType() {
        return immutableFieldMirror.getType();
    }
    
    @Override
    public int getModifiers() {
        try {
            return immutableFieldMirror.getModifiers();
        } catch (UnsupportedOperationException e) {
            return getBytecodeField().getModifiers();
        }
    }
    
    private FieldMirror getBytecodeField() {
        if (bytecodeField == null) {
            try {
                bytecodeField = klass.getBytecodeMirror().getDeclaredField(wrapped.getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return bytecodeField;
    }
    
    @Override
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStatic(wrapped) : ((InstanceHolograph)obj).get(wrapped);
    }

    @Override
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticBoolean(wrapped) : ((InstanceHolograph)obj).getBoolean(wrapped);
    }

    @Override
    public byte getByte(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticByte(wrapped) : ((InstanceHolograph)obj).getByte(wrapped);
    }

    @Override
    public char getChar(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticChar(wrapped) : ((InstanceHolograph)obj).getChar(wrapped);
    }

    @Override
    public short getShort(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticShort(wrapped) : ((InstanceHolograph)obj).getShort(wrapped);
    }

    @Override
    public int getInt(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticInt(wrapped) : ((InstanceHolograph)obj).getInt(wrapped);
    }

    @Override
    public long getLong(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticLong(wrapped) : ((InstanceHolograph)obj).getLong(wrapped);
    }

    @Override
    public float getFloat(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticFloat(wrapped) : ((InstanceHolograph)obj).getFloat(wrapped);
    }

    @Override
    public double getDouble(InstanceMirror obj) throws IllegalAccessException {
        return obj == null ? klass.getStaticDouble(wrapped) : ((InstanceHolograph)obj).getDouble(wrapped);
    }

    @Override
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        if (obj == null) {
            klass.setStatic(wrapped, o);
        } else {
            ((InstanceHolograph)obj).set(wrapped, o);
        }
    }

    @Override
    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticBoolean(wrapped, b);
        } else {
            ((InstanceHolograph)obj).setBoolean(wrapped, b);
        }
    }

    @Override
    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticByte(wrapped, b);
        } else {
            ((InstanceHolograph)obj).setByte(wrapped, b);
        }
    }

    @Override
    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticChar(wrapped, c);
        } else {
            ((InstanceHolograph)obj).setChar(wrapped, c);
        }
    }

    @Override
    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticShort(wrapped, s);
        } else {
            ((InstanceHolograph)obj).setShort(wrapped, s);
        }
    }

    @Override
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticInt(wrapped, i);
        } else {
            ((InstanceHolograph)obj).setInt(wrapped, i);
        }
    }

    @Override
    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticLong(wrapped, l);
        } else {
            ((InstanceHolograph)obj).setLong(wrapped, l);
        }
    }

    @Override
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticFloat(wrapped, f);
        } else {
            ((InstanceHolograph)obj).setFloat(wrapped, f);
        }
    }

    @Override
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
        if (obj == null) {
            klass.setStaticDouble(wrapped, d);
        } else {
            ((InstanceHolograph)obj).setDouble(wrapped, d);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + immutableFieldMirror;
    }

}
