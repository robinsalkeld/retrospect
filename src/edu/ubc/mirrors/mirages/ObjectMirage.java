package edu.ubc.mirrors.mirages;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeObjectMirror;

/**
 * Note that this class is only instantiated directly to represent arrays. Otherwise
 * it is subclassed with a mirage version of the original class in order to support
 * dynamic method dispatch (which arrays don't).
 * 
 * @author Robin Salkeld
 *
 * @param <T>
 */
public class ObjectMirage<T> {

    protected final InstanceMirror<?> mirror;
    
    /**
     * Constructor for translated new statements - instantiates a new native mirror.
     */
    protected ObjectMirage() {
        Class<?> nativeClass = getNativeClass(getClass());
        this.mirror = new FieldMapMirror(nativeClass);
    }
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    public ObjectMirage(InstanceMirror<T> mirror) {
        this.mirror = mirror;
        
    }
    
    
    
    // TODO: Can't do this until we sort out how to automatically specify ClassMirrorLoaders

//    private static Map<ClassLoader, MirageClassLoader> mirageClassLoaders = new HashMap<ClassLoader, MirageClassLoader>();
//    
//    public static MirageClassLoader getMirageClassLoader(ClassLoader originalLoader) {
//        MirageClassLoader mirageLoader = mirageClassLoaders.get(originalLoader);
//        if (mirageLoader == null) {
//            mirageLoader = new MirageClassLoader(originalLoader);
//            mirageClassLoaders.put(originalLoader, mirageLoader);
//        }
//        return mirageLoader;
//    }
//    
//    public static Class<?> getMirageClass(Class<?> original) throws ClassNotFoundException {
//        MirageClassLoader mirageLoader = getMirageClassLoader(original.getClassLoader());
//        return mirageLoader.loadClass(MirageClassGenerator.getMirageBinaryClassName(original.getName()));
//    }
    
    public static Class<?> getNativeClass(Class<?> mirageClass) {
        final String originalClassName = MirageClassGenerator.getOriginalClassName(mirageClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return mirageClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    // TODO: Can't do this until we sort out how to automatically specify ClassMirrorLoaders
//    public static Class<?> defineMirageClass(String className, ClassLoader originalLoader, ClassReader originalReader) {
//        MirageClassLoader mirageLoader = getMirageClassLoader(originalLoader);
//        return mirageLoader.defineMirageClass(className, originalReader);
//    }
    
    public static Class<?> getOriginalClass(Class<?> mirageClass) throws ClassNotFoundException {
        ClassLoader originalLoader = ((MirageClassLoader)mirageClass.getClassLoader()).getOriginalLoader();
        String originalClassName = MirageClassGenerator.getOriginalClassName(mirageClass.getName());
        return originalLoader.loadClass(originalClassName);
    }
    
    private static void throwInternalError(Throwable t) {
        InternalError ie = new InternalError();
        ie.initCause(t);
        throw ie;
    }
    
    public static void invokeMirageMainMethod(Class<?> originalClass, String[] args) {
        try {
            ClassLoader originalLoader = originalClass.getClassLoader();
            ClassMirrorLoader mirrorLoader = new NativeClassMirrorLoader(originalLoader);
            MirageClassLoader loader = new MirageClassLoader(originalLoader, mirrorLoader);
            final Class<?> mirageClass = loader.loadClass(originalClass.getName());
            final Class<?> mirageStringArray = Class.forName("[Ljava.lang.String;", true, mirageClass.getClassLoader());
            mirageClass.getDeclaredMethod("main", mirageStringArray).invoke(null, (Object)args);
        } catch (ClassNotFoundException e) {
            throwInternalError(e);
        } catch (SecurityException e) {
            throwInternalError(e);
        } catch (IllegalAccessException e) {
            throwInternalError(e);
        } catch (InvocationTargetException e) {
            throwInternalError(e);
        } catch (NoSuchMethodException e) {
            throwInternalError(e);
        }
    }
    
    public static String getRealStringForMirage(ObjectMirage<?> mirage) {
        try {
            InstanceMirror<?> mirror = (InstanceMirror<?>)mirage.mirror;
            char[] value = (char[])mirror.getMemberField("value").get();
            int offset = mirror.getMemberField("offset").getInt();
            int count = mirror.getMemberField("count").getInt();
            return new String(value, offset, count);
        } catch (IllegalAccessException e) {
            throwInternalError(e);
            return null;
        } catch (NoSuchFieldException e) {
            throwInternalError(e);
        }
        
        // Never reached
        return null;
    }
    
    public static Object getMirageStringForReal(String string, Class<?> classLoaderLiteral) {
        ObjectMirror<String> mirror = new NativeObjectMirror<String>(string);
        return ((MirageClassLoader)classLoaderLiteral.getClassLoader()).makeMirage(mirror);
    }
    
    public static FieldMirror getStaticField(Class<?> classLoaderLiteral, String className, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        return loader.classMirrorLoader.loadClassMirror(className).getStaticField(fieldName);
    }
    
    public static <T> T make(ObjectMirror<T> mirror) {
        return ((MirageClassLoader)mirror.getClass().getClassLoader()).makeMirage(mirror);
    }
}
