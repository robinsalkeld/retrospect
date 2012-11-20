package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;

public class InstanceMirage {

    private static FieldMirror getFieldMirror(ClassMirror klass, String name) {
        try {
            return Reflection.findField(klass, name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
    
    private static InstanceMirror getInstanceMirror(Mirage m, ClassMirror klass) {
        return (m == null) ? klass.getStaticFieldValues() : (InstanceMirror)m.getMirror();
    }
    
    public static Mirage getField(Mirage m, ClassMirror klass, String name) {
         try {
            return ObjectMirage.make(getInstanceMirror(m, klass).get(getFieldMirror(klass, name)));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    
    public static boolean getBooleanField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getBoolean(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static byte getByteField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getByte(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }

    public static char getCharField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getChar(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static short getShortField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getShort(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static int getIntField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getInt(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static long getLongField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getLong(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static float getFloatField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getFloat(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static double getDoubleField(Mirage m, ClassMirror klass, String name) {
        try {
           return getInstanceMirror(m, klass).getDouble(getFieldMirror(klass, name));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static void setField(Mirage m, Mirage o, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).set(getFieldMirror(klass, name), ObjectMirage.getMirror(o));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
   }
   
   public static void setBooleanField(Mirage m, boolean b, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setBoolean(getFieldMirror(klass, name), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setByteField(Mirage m, byte b, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setByte(getFieldMirror(klass, name), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setCharField(Mirage m, char c, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setChar(getFieldMirror(klass, name), c);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setShortField(Mirage m, short s, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setShort(getFieldMirror(klass, name), s);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setIntField(Mirage m, int i, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setInt(getFieldMirror(klass, name), i);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setLongField(Mirage m, long l, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setLong(getFieldMirror(klass, name), l);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setFloatField(Mirage m, float f, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setFloat(getFieldMirror(klass, name), f);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setDoubleField(Mirage m, double d, ClassMirror klass, String name) {
        try {
            getInstanceMirror(m, klass).setDouble(getFieldMirror(klass, name), d);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
