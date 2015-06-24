/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holograms.Hologram;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;

public class InstanceHologram {

    private static FieldMirror getFieldMirror(ClassMirror klass, String name) {
        try {
            return Reflection.findField(klass, name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
    
    private static InstanceMirror getInstanceMirror(Hologram m, ClassMirror klass) {
        return (m == null) ? klass.getStaticFieldValues() : (InstanceMirror)m.getMirror();
    }
    
    private static Hologram BACKTRACE = new Hologram() {
        @Override
        public ObjectMirror getMirror() {
            return null;
        }
    };
    
    public static Hologram getField(Hologram m, ClassMirror klass, String name) throws Throwable {
        try {
            if (klass.getClassName().equals(Throwable.class.getName()) && name.equals("backtrace")) {
                return BACKTRACE;
            }
             
            FieldMirror field = getFieldMirror(klass, name);
            InstanceMirror target = getInstanceMirror(m, klass);
            ObjectMirror result = (ObjectMirror)((ClassHolograph)klass).getVM().eventRequestManager().handleFieldGet(target, field);

            return ObjectHologram.make(result);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (MirrorInvocationTargetException e) {
            throw (Throwable)ObjectHologram.make(e.getTargetException());
        }
    }
    
    public static boolean getBooleanField(Hologram m, ClassMirror klass, String name) throws Throwable {
        try {
            FieldMirror field = getFieldMirror(klass, name);
            InstanceMirror target = getInstanceMirror(m, klass);
            return (Boolean)((ClassHolograph)klass).getVM().eventRequestManager().handleFieldGet(target, field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (MirrorInvocationTargetException e) {
            throw (Throwable)ObjectHologram.make(e.getTargetException());
        }
    }
    
    public static byte getByteField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getByte(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }

    public static char getCharField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getChar(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static short getShortField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getShort(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static int getIntField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getInt(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static long getLongField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getLong(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static float getFloatField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getFloat(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static double getDoubleField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getDouble(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static void setField(Hologram m, Hologram o, ClassMirror klass, String name) throws Throwable {
        try {
            FieldMirror field = getFieldMirror(klass, name);
            InstanceMirror target = getInstanceMirror(m, klass);
            ((ClassHolograph)klass).getVM().eventRequestManager().handleFieldSet(target, field, ObjectHologram.getMirror(o));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       } catch (MirrorInvocationTargetException e) {
           throw (Throwable)ObjectHologram.make(e.getTargetException());
       }
   }
   
   public static void setBooleanField(Hologram m, boolean b, ClassMirror klass, String name) throws Throwable {
        try {
            FieldMirror field = getFieldMirror(klass, name);
            InstanceMirror target = getInstanceMirror(m, klass);
            ((ClassHolograph)klass).getVM().eventRequestManager().handleFieldSet(target, field, b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (MirrorInvocationTargetException e) {
            throw (Throwable)ObjectHologram.make(e.getTargetException());
        }
    }

    public static void setByteField(Hologram m, byte b, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setByte(getFieldMirror(klass, name), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setCharField(Hologram m, char c, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setChar(getFieldMirror(klass, name), c);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setShortField(Hologram m, short s, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setShort(getFieldMirror(klass, name), s);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setIntField(Hologram m, int i, ClassMirror klass, String name) throws Throwable {
        try {
            FieldMirror field = getFieldMirror(klass, name);
            InstanceMirror target = getInstanceMirror(m, klass);
            ((ClassHolograph)klass).getVM().eventRequestManager().handleFieldSet(target, field, i);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (MirrorInvocationTargetException e) {
            throw (Throwable)ObjectHologram.make(e.getTargetException());
        }
    }
    public static void setLongField(Hologram m, long l, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setLong(getFieldMirror(klass, name), l);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setFloatField(Hologram m, float f, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setFloat(getFieldMirror(klass, name), f);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setDoubleField(Hologram m, double d, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setDouble(getFieldMirror(klass, name), d);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
