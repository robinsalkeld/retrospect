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
    
    public static Mirage getField(Mirage m, ClassMirror klass, String name) {
         try {
            return ObjectMirage.make(getFieldMirror(klass, name).get((InstanceMirror)ObjectMirage.getMirror(m)));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    
    public static boolean getBooleanField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getBoolean((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static byte getByteField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getByte((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }

    public static char getCharField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getChar((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static short getShortField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getShort((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static int getIntField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getInt((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static long getLongField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getLong((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static float getFloatField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getFloat((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    public static double getDoubleField(Mirage m, ClassMirror klass, String name) {
        try {
           return getFieldMirror(klass, name).getDouble((InstanceMirror)ObjectMirage.getMirror(m));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
    }
    
    public static void setField(Mirage m, Mirage o, ClassMirror klass, String name) {
        try {
           getFieldMirror(klass, name).set((InstanceMirror)ObjectMirage.getMirror(m), ObjectMirage.getMirror(o));
       } catch (IllegalAccessException e) {
           throw new IllegalAccessError(e.getMessage());
       }
   }
   
   public static void setBooleanField(Mirage m, boolean b, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setBoolean((InstanceMirror)ObjectMirage.getMirror(m), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setByteField(Mirage m, byte b, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setByte((InstanceMirror)ObjectMirage.getMirror(m), b);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setCharField(Mirage m, char c, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setChar((InstanceMirror)ObjectMirage.getMirror(m), c);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setShortField(Mirage m, short s, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setShort((InstanceMirror)ObjectMirage.getMirror(m), s);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setIntField(Mirage m, int i, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setInt((InstanceMirror)ObjectMirage.getMirror(m), i);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setLongField(Mirage m, long l, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setLong((InstanceMirror)ObjectMirage.getMirror(m), l);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setFloatField(Mirage m, float f, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setFloat((InstanceMirror)ObjectMirage.getMirror(m), f);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    public static void setDoubleField(Mirage m, double d, ClassMirror klass, String name) {
        try {
            getFieldMirror(klass, name).setDouble((InstanceMirror)ObjectMirage.getMirror(m), d);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
