package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

/**
 * Superclass for all mirage classes.
 * Represents java.lang.Object if used directly.
 * 
 * @author Robin Salkeld
 */
public class ObjectMirage implements Mirage {
    public ObjectMirror mirror;
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    public ObjectMirage(Object mirror) {
        if (getClass().getName().equals("mirage.java.lang.Class") && !(mirror instanceof ClassMirror)) {
            throw new IllegalArgumentException();
        }
        
        this.mirror = (ObjectMirror)mirror;
        register();
    }
    
    /**
     * Constructor for translated new statements.
     */
    public ObjectMirage(InstanceMirror mirror) {
        this((Object)mirror);
    }
    
    private void register() {
        MirageClassLoader loader = ClassHolograph.getMirageClassLoader(mirror.getClassMirror());
        loader.registerMirage(this);
    }
    
    @Override
    public ObjectMirror getMirror() {
        return mirror;
    }
    
    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }
    
    /**
     * Redefined here so we can correct getClass() - otherwise we hit the
     * definition in Object.class which we can't rewrite.
     * @param o
     * @return
     */
    public static String mirageToString(Mirage o) {
        return o.getMirror().getClassMirror().getClassName() + "@" + Integer.toHexString(o.hashCode());
    }
    
    @Override
    public String toString() {
        return mirageToString(this);
    }
    
    public static Class<?> getNativeClass(Class<?> mirageClass) {
        final String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(mirageClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return mirageClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    private static void throwInternalError(Throwable t) {
        InternalError ie = new InternalError();
        ie.initCause(t);
        throw ie;
    }
    
    public static String getRealStringForMirage(ObjectMirage mirage) {
        if (mirage == null) {
            return null;
        }
        
        return Reflection.getRealStringForMirror((InstanceMirror)mirage.mirror);
    }
    
    public static StackTraceElement[] getRealStackTraceForMirage(Mirage mirage) {
        if (mirage == null) {
            return null;
        }
        
        try {
            ObjectArrayMirror arrayMirror = (ObjectArrayMirror)mirage.getMirror();
            StackTraceElement[] result = new StackTraceElement[arrayMirror.length()];
            for (int i = 0; i < result.length; i++) {
                InstanceMirror element = (InstanceMirror)arrayMirror.get(i);
                String declaringClass = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("declaringClass").get());
                String methodName = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("methodName").get());
                String fileName = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("fileName").get());
                int lineNumber = element.getMemberField("lineNumber").getInt();
                
                result[i] = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }
            return result;
        } catch (IllegalAccessException e) {
            throwInternalError(e);
            return null;
        } catch (NoSuchFieldException e) {
            throwInternalError(e);
            return null;
        }
    }
    
    public static InstanceMirror newInstanceMirror(Class<?> classLoaderLiteral, String mirageClassName) {
        String originalClassName = MirageClassGenerator.getOriginalInternalClassName(mirageClassName);
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getObjectType(originalClassName));
        return originalClassMirror.newRawInstance();
    }
    
    public static ArrayMirror newArrayMirror(Class<?> classLoaderLiteral, String elementDescriptor, int length) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getType(elementDescriptor));
        return originalClassMirror.newArray(length);
    }
    
    public static ArrayMirror newArrayMirror(Class<?> classLoaderLiteral, String elementDescriptor, int[] dims) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getType(elementDescriptor));
        return originalClassMirror.newArray(dims);
    }
    
    public static FieldMirror getStaticField(Class<?> classLoaderLiteral, String className, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        String binaryName = className.replace('/', '.');
        ClassHolograph klass = (ClassHolograph)loader.loadOriginalClassMirror(binaryName);
        FieldMirror result = Reflection.getStaticField(klass, fieldName);
        if (className.equals("$Proxy1")) {
            int bp = 4;
            bp++;
        }
        return result;
    }
    
    public static Mirage make(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        MirageClassLoader loader = ClassHolograph.getMirageClassLoader(mirror.getClassMirror());
        return loader.makeMirage(mirror);
    }
    
    public static Mirage makeStringMirage(String s, Class<?> classLoaderLiteral) {
        if (s == null) {
            return null;
        }
        
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        InstanceMirror sMirror = Reflection.makeString(loader.getVM(), s);
        return loader.makeMirage(sMirror);
    }
    
    public static Mirage makeClassMirage(Class<?> c, Class<?> classLoaderLiteral) {
        if (c == null) {
            return null;
        }
        
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(c.getName());
        ClassMirror originalMirror = loader.loadOriginalClassMirror(originalClassName);
        return loader.makeMirage(originalMirror);
    }
    
    public static ObjectMirror getMirror(Object o) {
        if (o == null) {
            return null;
        }
        return ((Mirage)o).getMirror();
    }
    
    public static Mirage cleanAndSetStackTrace(Mirage throwable, StackTraceElement[] nativeTrace) {
        VirtualMachineMirror vm = throwable.getMirror().getClassMirror().getVM();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror intClass = vm.getPrimitiveClass("int");
        ConstructorMirror constructor = Reflection.getConstructor(stackTraceElementClass, stringClass, stringClass, stringClass, intClass);
                
        ObjectArrayMirror correctedTrace = (ObjectArrayMirror)stackTraceElementClass.newArray(nativeTrace.length);
        for (int i = 0; i < nativeTrace.length; i++) {
            StackTraceElement e = nativeTrace[i];
            InstanceMirror className = Reflection.makeString(vm, getOriginalBinaryClassName(e.getClassName()));
            InstanceMirror methodName = Reflection.makeString(vm, e.getMethodName());
            InstanceMirror fieldName = Reflection.makeString(vm, e.getFileName());
            int lineNumber = e.getLineNumber();
            InstanceMirror mapped = Reflection.newInstance(constructor, ClassHolograph.currentThreadMirror.get(), className, methodName, fieldName, lineNumber);
            correctedTrace.set(i, mapped);
        }
        InstanceMirror mirror = (InstanceMirror)throwable.getMirror();
        Reflection.setField(mirror, "stackTrace", correctedTrace);
        return make(correctedTrace);
    }
    
    // This has to be public since the implicit version on array classes is public
    @Override
    public Object clone() throws CloneNotSupportedException {
        return clone(this);
    }
    
    public static Object clone(Mirage mirage) {
        Class<?> classLoaderLiteral = mirage.getClass();
        ObjectMirror mirror = mirage.getMirror();
        if (mirror instanceof InstanceMirror) {
            InstanceMirror instanceMirror = (InstanceMirror)mirror;
            InstanceMirror result = newInstanceMirror(classLoaderLiteral, mirror.getClassMirror().getClassName());
            for (FieldMirror field : instanceMirror.getMemberFields()) {
                ClassMirror fieldType = field.getType();
                String typeName = fieldType.getClassName();
                try {
                    FieldMirror resultField = result.getMemberField(field.getName());
                    if (typeName.equals(Boolean.TYPE.getName())) {
                        resultField.setBoolean(field.getBoolean());
                    } else if (typeName.equals(Byte.TYPE.getName())) {
                        resultField.setByte(field.getByte());
                    } else if (typeName.equals(Character.TYPE.getName())) {
                        resultField.setChar(field.getChar());
                    } else if (typeName.equals(Short.TYPE.getName())) {
                        resultField.setShort(field.getShort());
                    } else if (typeName.equals(Integer.TYPE.getName())) {
                        resultField.setInt(field.getInt());
                    } else if (typeName.equals(Long.TYPE.getName())) {
                        resultField.setLong(field.getLong());
                    } else if (typeName.equals(Float.TYPE.getName())) {
                        resultField.setFloat(field.getFloat());
                    } else if (typeName.equals(Double.TYPE.getName())) {
                        resultField.setDouble(field.getDouble());
                    } else {
                        resultField.set(field.get());
                    }
                } catch (NoSuchFieldException e) {
                    throw new NoSuchFieldError(e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new IllegalAccessError(e.getMessage());
                }
            }
            return make(result);
        } else if (mirror instanceof ArrayMirror) {
            ArrayMirror objectArrayMirror = (ArrayMirror)mirror;
            int length = objectArrayMirror.length();
            
            ArrayMirror result = mirror.getClassMirror().getComponentClassMirror().newArray(length);
            
            SystemStubs.arraycopyMirrors(objectArrayMirror, 0, result, 0, length);
            
            return make(result);
        } else {    
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
    
    public static Throwable throwableAsMirage(VirtualMachineMirror vm, Throwable t) {
        ClassMirror klass = vm.findBootstrapClassMirror(t.getClass().getName());
        InstanceMirror throwableMirror = klass.newRawInstance();
        Reflection.setField(throwableMirror, "detailMessage", Reflection.makeString(vm, t.getMessage()));
        return (Throwable)ObjectMirage.make(throwableMirror);
    }
}
