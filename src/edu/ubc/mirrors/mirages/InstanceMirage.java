package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;

public class InstanceMirage {

    private static FieldMirror getFieldMirror(Mirage m, String name) {
        InstanceMirror mirror = (InstanceMirror)m.getMirror();
        try {
            return mirror.getMemberField(name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
    
    public static Mirage getField(Mirage m, String name) {
         try {
            return ObjectMirage.make(getFieldMirror(m, name).get());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    
    public static boolean getBooleanField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getBoolean();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static byte getByteField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getByte();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }

    public static char getCharField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getChar();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static short getShortField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getShort();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static int getIntField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getInt();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static long getLongField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getLong();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static float getFloatField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getFloat();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static double getDoubleField(Mirage m, String name) {
        try {
           return getFieldMirror(m, name).getDouble();
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static void setField(Mirage m, Mirage o, String name) {
        try {
           getFieldMirror(m, name).set(ObjectMirage.getMirror(o));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
   }
   
   public static void setBooleanField(Mirage m, boolean b, String name) {
        try {
            getFieldMirror(m, name).setBoolean(b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setByteField(Mirage m, byte b, String name) {
        try {
            getFieldMirror(m, name).setByte(b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setCharField(Mirage m, char c, String name) {
        try {
            getFieldMirror(m, name).setChar(c);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setShortField(Mirage m, short s, String name) {
        try {
            getFieldMirror(m, name).setShort(s);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setIntField(Mirage m, int i, String name) {
        try {
            getFieldMirror(m, name).setInt(i);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setLongField(Mirage m, long l, String name) {
        try {
            getFieldMirror(m, name).setLong(l);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setFloatField(Mirage m, float f, String name) {
        try {
            getFieldMirror(m, name).setFloat(f);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setDoubleField(Mirage m, double d, String name) {
        try {
            getFieldMirror(m, name).setDouble(d);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
