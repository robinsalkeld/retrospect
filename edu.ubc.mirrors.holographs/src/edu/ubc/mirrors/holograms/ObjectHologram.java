package edu.ubc.mirrors.holograms;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.getOriginalBinaryClassName;

import java.util.Arrays;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeClassGenerator;

/**
 * Superclass for all hologram classes.
 * Represents java.lang.Object if used directly.
 * 
 * @author Robin Salkeld
 */
public class ObjectHologram implements Hologram {
    public ObjectMirror mirror;
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    public ObjectHologram(Object mirror) {
        if (getClass().getName().equals("hologram.java.lang.Class") && !(mirror instanceof ClassMirror)) {
            throw new IllegalArgumentException();
        }
        
        this.mirror = (ObjectMirror)mirror;
        register();
    }
    
    /**
     * Constructor for translated new statements.
     */
    public ObjectHologram(InstanceMirror mirror) {
        this((Object)mirror);
    }
    
    private void register() {
        HologramClassLoader loader = ClassHolograph.getHologramClassLoader(mirror.getClassMirror());
        loader.registerHologram(this);
    }
    
    @Override
    public ObjectMirror getMirror() {
        return mirror;
    }
    
    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }
    
    public int identityHashCode() {
        return mirror.identityHashCode();
    }
    
    /**
     * Redefined here so we can correct getClass() - otherwise we hit the
     * definition in Object.class which we can't rewrite.
     * @param o
     * @return
     */
    public static String hologramToString(Hologram o) {
        return o.getMirror().getClassMirror().getClassName() + "@" + Integer.toHexString(o.hashCode());
    }
    
    @Override
    public String toString() {
        return hologramToString(this);
    }
    
    public static Class<?> getNativeClass(Class<?> hologramClass) {
        final String originalClassName = HologramClassGenerator.getOriginalBinaryClassName(hologramClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return hologramClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    private static void throwInternalError(Throwable t) {
        InternalError ie = new InternalError();
        ie.initCause(t);
        throw ie;
    }
    
    public static String getRealStringForHologram(ObjectHologram hologram) {
        if (hologram == null) {
            return null;
        }
        
        return Reflection.getRealStringForMirror((InstanceMirror)hologram.mirror);
    }
    
    public static StackTraceElement[] getRealStackTraceForHologram(Hologram hologram) {
        if (hologram == null) {
            return null;
        }
        
        try {
            ObjectArrayMirror arrayMirror = (ObjectArrayMirror)hologram.getMirror();
            ClassMirror steClass = arrayMirror.getClassMirror().getComponentClassMirror();
            StackTraceElement[] result = new StackTraceElement[arrayMirror.length()];
            for (int i = 0; i < result.length; i++) {
                InstanceMirror element = (InstanceMirror)arrayMirror.get(i);
                String declaringClass = Reflection.getRealStringForMirror((InstanceMirror)element.get(steClass.getDeclaredField("declaringClass")));
                String methodName = Reflection.getRealStringForMirror((InstanceMirror)element.get(steClass.getDeclaredField("methodName")));
                String fileName = Reflection.getRealStringForMirror((InstanceMirror)element.get(steClass.getDeclaredField("fileName")));
                int lineNumber = element.getInt(steClass.getDeclaredField("lineNumber"));
                
                result[i] = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }
            return result;
        } catch (IllegalAccessException e) {
            throwInternalError(e);
            return null;
        }
    }
    
    public static Hologram make(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        HologramClassLoader loader = ClassHolograph.getHologramClassLoader(mirror.getClassMirror());
        return loader.makeHologram(mirror);
    }
    
    public static Hologram makeStringHologram(String s, ClassMirror callingClass) {
        if (s == null) {
            return null;
        }
        
        InstanceMirror sMirror = Reflection.makeString(callingClass.getVM(), s);
        return ObjectHologram.make(sMirror);
    }
    
    public static ClassMirror getClassMirrorForHolographicClass(Class<?> klass) {
	if (klass == null) {
            return null;
        }
        
	String name = klass.getName();
	String originalClassName = HologramClassGenerator.getOriginalBinaryClassName(name);
        HologramClassLoader loader = (HologramClassLoader)klass.getClassLoader();
        return loader.loadOriginalClassMirror(originalClassName);
    }
    
    public static ClassMirror getClassMirrorForType(ClassMirror callingClass, String descriptor) {
	Type hologramType = Type.getType(descriptor);
	Type type = HologramClassGenerator.getOriginalType(hologramType);
        return HolographInternalUtils.classMirrorForType(callingClass.getVM(), ThreadHolograph.currentThreadMirror(), type, false, callingClass.getLoader());
    }
    
    public static Hologram makeClassHologram(Class<?> c, ClassMirror callingClass) {
	if (c == null) {
            return null;
        }
        
        HologramClassLoader loader = ClassHolograph.getHologramClassLoader(callingClass);
        String originalClassName = HologramClassGenerator.getOriginalBinaryClassName(c.getName());
        ClassMirror classMirror = loader.loadOriginalClassMirror(originalClassName);
        return ObjectHologram.make(classMirror);
    }
    
    public static ObjectMirror getMirror(Object o) {
        if (o == null) {
            return null;
        }
        return ((Hologram)o).getMirror();
    }
    
    public static Hologram cleanAndSetStackTrace(Hologram throwable, StackTraceElement[] nativeTrace) {
        VirtualMachineMirror vm = throwable.getMirror().getClassMirror().getVM();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror intClass = vm.getPrimitiveClass("int");
        ConstructorMirror constructor = HolographInternalUtils.getConstructor(stackTraceElementClass, stringClass, stringClass, stringClass, intClass);
                
        ObjectArrayMirror correctedTrace = (ObjectArrayMirror)stackTraceElementClass.newArray(nativeTrace.length);
        for (int i = 0; i < nativeTrace.length; i++) {
            StackTraceElement e = nativeTrace[i];
            InstanceMirror className = Reflection.makeString(vm, getOriginalBinaryClassName(e.getClassName()));
            InstanceMirror methodName = Reflection.makeString(vm, e.getMethodName());
            InstanceMirror fieldName = Reflection.makeString(vm, e.getFileName());
            int lineNumber = e.getLineNumber();
            InstanceMirror mapped = HolographInternalUtils.newInstance(constructor, ThreadHolograph.currentThreadMirror(), className, methodName, fieldName, lineNumber);
            correctedTrace.set(i, mapped);
        }
        InstanceMirror mirror = (InstanceMirror)throwable.getMirror();
        HolographInternalUtils.setField(mirror, "stackTrace", correctedTrace);
        return make(correctedTrace);
    }
    
    // This has to be public since the implicit version on array classes is public
    @Override
    public Object clone() throws CloneNotSupportedException {
        return clone(this);
    }
    
    public static Object clone(Hologram hologram) {
        ObjectMirror mirror = hologram.getMirror();
        if (mirror instanceof InstanceMirror) {
            InstanceMirror instanceMirror = (InstanceMirror)mirror;
            InstanceMirror result = mirror.getClassMirror().newRawInstance();
            ClassMirror classMirror = instanceMirror.getClassMirror();
            for (FieldMirror field : Reflection.getAllFields(classMirror)) {
                ClassMirror fieldType = field.getType();
                String typeName = fieldType.getClassName();
                try {
                    if (typeName.equals(Boolean.TYPE.getName())) {
                        result.setBoolean(field, instanceMirror.getBoolean(field));
                    } else if (typeName.equals(Byte.TYPE.getName())) {
                        result.setByte(field, instanceMirror.getByte(field));
                    } else if (typeName.equals(Character.TYPE.getName())) {
                        result.setChar(field, instanceMirror.getChar(field));
                    } else if (typeName.equals(Short.TYPE.getName())) {
                        result.setShort(field, instanceMirror.getShort(field));
                    } else if (typeName.equals(Integer.TYPE.getName())) {
                        result.setInt(field, instanceMirror.getInt(field));
                    } else if (typeName.equals(Long.TYPE.getName())) {
                        result.setLong(field, instanceMirror.getLong(field));
                    } else if (typeName.equals(Float.TYPE.getName())) {
                        result.setFloat(field, instanceMirror.getFloat(field));
                    } else if (typeName.equals(Double.TYPE.getName())) {
                        result.setDouble(field, instanceMirror.getDouble(field));
                    } else {
                        result.set(field, instanceMirror.get(field));
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalAccessError(e.getMessage());
                }
            }
            return make(result);
        } else if (mirror instanceof ArrayMirror) {
            ArrayMirror objectArrayMirror = (ArrayMirror)mirror;
            int length = objectArrayMirror.length();
            
            ArrayMirror result = mirror.getClassMirror().getComponentClassMirror().newArray(length);
            
            Reflection.arraycopy(objectArrayMirror, 0, result, 0, length);
            
            return make(result);
        } else {    
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
    
    public static Throwable throwableAsHologram(VirtualMachineMirror vm, Throwable t) {
        ClassMirror klass = vm.findBootstrapClassMirror(t.getClass().getName());
        InstanceMirror throwableMirror = klass.newRawInstance();
        HolographInternalUtils.setField(throwableMirror, "detailMessage", Reflection.makeString(vm, t.getMessage()));
        return (Throwable)ObjectHologram.make(throwableMirror);
    }
    
    public static Object invokeMethodHandler(ClassMirror klass, String methodName, String methodDesc, InstanceMirror object, Object[] args) throws Throwable {
        Type methodType = Type.getMethodType(methodDesc);
        ClassHolograph classHolograph = (ClassHolograph)klass;
        MethodMirror method = Reflection.getDeclaredMethod(ThreadHolograph.currentThreadMirror(), klass, methodName, methodType);
        
        try {
            return classHolograph.invoke(object, method, args);
        } catch (MirrorInvocationTargetException e) {
            throw (Throwable)ObjectHologram.make(e.getTargetException());
        }
    }
}
