package edu.ubc.mirrors.sunhat;

import com.sun.tools.hat.internal.model.JavaBoolean;
import com.sun.tools.hat.internal.model.JavaByte;
import com.sun.tools.hat.internal.model.JavaChar;
import com.sun.tools.hat.internal.model.JavaDouble;
import com.sun.tools.hat.internal.model.JavaFloat;
import com.sun.tools.hat.internal.model.JavaInt;
import com.sun.tools.hat.internal.model.JavaLong;
import com.sun.tools.hat.internal.model.JavaShort;
import com.sun.tools.hat.internal.model.JavaThing;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirrorLoader;

public class JHatFieldMirror implements FieldMirror {

    private final JHatClassMirrorLoader loader;
    private final String name;
    private final JavaThing value;
    
    public JHatFieldMirror(JHatClassMirrorLoader loader, String name, JavaThing value) {
        this.loader = loader;
        this.name = name;
        this.value = value;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public ObjectMirror get() throws IllegalAccessException {
        return loader.getMirror(value);
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return Boolean.parseBoolean(((JavaBoolean)value).toString());
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return Byte.parseByte(((JavaByte)value).toString());
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return ((JavaChar)value).toString().charAt(0);
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return Short.parseShort(((JavaShort)value).toString());
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return Integer.parseInt(((JavaInt)value).toString());
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return Long.parseLong(((JavaLong)value).toString());
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return Float.parseFloat(((JavaFloat)value).toString());
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return Double.parseDouble(((JavaDouble)value).toString());
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(boolean b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(byte b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(char c) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(short s) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(int i) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLong(long l) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloat(float f) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(double d) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

}
