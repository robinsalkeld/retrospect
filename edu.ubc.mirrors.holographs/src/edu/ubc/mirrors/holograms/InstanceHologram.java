package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holograms.Hologram;
import edu.ubc.mirrors.holograms.ObjectHologram;

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
    
    public static Hologram getField(Hologram m, ClassMirror klass, String name) {
         try {
            return ObjectHologram.make(getInstanceMirror(m, klass).get(getFieldMirror(klass, name)));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    
    public static boolean getBooleanField(Hologram m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getBoolean(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
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
    
    public static void setField(Hologram m, Hologram o, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).set(getFieldMirror(klass, name), ObjectHologram.getMirror(o));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
   }
   
   public static void setBooleanField(Hologram m, boolean b, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setBoolean(getFieldMirror(klass, name), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
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
    public static void setIntField(Hologram m, int i, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setInt(getFieldMirror(klass, name), i);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
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