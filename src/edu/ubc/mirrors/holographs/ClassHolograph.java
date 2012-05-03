package edu.ubc.mirrors.holographs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class ClassHolograph extends WrappingClassMirror {

    protected ClassHolograph(VirtualMachineHolograph vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }
    
    public ClassMirror getWrappedClassMirror() {
        return wrapped;
    }
    
    public Class<?> getNativeStubsClass() {
        return NativeClassMirror.getNativeStubsClass(getClassName());
    }
    
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            mirageParamTypes[i] = getMirageClass(paramTypes[i], false);
        }
        Class<?> mirageClass = getMirageClass(this, false);
        Method method = mirageClass.getDeclaredMethod(name, mirageParamTypes);
        return new MirageMethod(method);
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        // Add the extra implicit mirror parameter
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length + 1];
        for (int i = 0; i < paramTypes.length; i++) {
            mirageParamTypes[i] = getMirageClass(paramTypes[i], false);
        }
        mirageParamTypes[paramTypes.length] = InstanceMirror.class;
        
        Class<?> mirageClass = getMirageClass(this, false);
        Constructor<?> constructor = mirageClass.getDeclaredConstructor(mirageParamTypes);
        return new MirageConstructor(constructor);
    }
    
    @Override
    public VirtualMachineHolograph getVM() {
        return (VirtualMachineHolograph)super.getVM();
    }
    
    public ClassLoaderHolograph getLoader() {
        return (ClassLoaderHolograph)super.getLoader();
    }
    
    public static MirageClassLoader getMirageClassLoader(ClassHolograph classMirror) {
        return getMirageClassLoader(classMirror.getVM(), classMirror.getLoader());
    }
    
    public static MirageClassLoader getMirageClassLoader(VirtualMachineHolograph vm, ClassLoaderHolograph loader) {
        return loader == null ? vm.getMirageClassLoader() : loader.getMirageClassLoader();
    }
    
    private MirageClassLoader getMirageClassLoader() {
        return getMirageClassLoader(getVM(), getLoader());
    }
    
    public static Class<?> getMirageClass(ClassMirror classMirror, boolean impl) {
        if (classMirror instanceof ClassHolograph) {
            return ((ClassHolograph)classMirror).getMirageClass(impl);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public Class<?> getMirageClass(boolean impl) {
        try {
            return getMirageClassLoader().getMirageClass(this, impl);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
    private static class MirageMethod implements MethodMirror {

        private final Method mirageClassMethod;
        
        public MirageMethod(Method method) {
            this.mirageClassMethod = method;
        }
        
        @Override
        public Object invoke(InstanceMirror obj, Object ... args) throws IllegalAccessException, InvocationTargetException {
            Object[] mirageArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = makeMirage(args[i]);
            }
            Object mirageObj = makeMirage(obj);
            Object result = mirageClassMethod.invoke(mirageObj, mirageArgs);
            return unwrapMirage(result);
        }
        
        @Override
        public void setAccessible(boolean flag) {
            mirageClassMethod.setAccessible(flag);
        }
    }
    
    private static class MirageConstructor implements ConstructorMirror {

        private final Constructor<?> mirageClassConstructor;
        
        public MirageConstructor(Constructor<?> mirageClassConstructor) {
            this.mirageClassConstructor = mirageClassConstructor;
        }
        
        @Override
        public InstanceMirror newInstance(Object... args)
                throws InstantiationException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {

            // Add the extra implicit mirror parameter
            Class<?> classLoaderLiteral = mirageClassConstructor.getDeclaringClass();
            InstanceMirror mirror = ObjectMirage.newInstanceMirror(classLoaderLiteral, classLoaderLiteral.getName().replace('/', '.'));
            Object[] mirageArgs = new Object[args.length + 1];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = makeMirage(args[i]);
            }
            mirageArgs[args.length] = mirror;
            Object result = mirageClassConstructor.newInstance(mirageArgs);
            return (InstanceMirror)unwrapMirage(result);
        }
        
        @Override
        public void setAccessible(boolean flag) {
            mirageClassConstructor.setAccessible(flag);
        }
    }
    
    public static Object makeMirage(Object mirror) {
        if (mirror instanceof ObjectMirror) {
            ObjectMirror objectMirror = (ObjectMirror)mirror;
            ClassHolograph classMirror = (ClassHolograph)objectMirror.getClassMirror();
            return getMirageClassLoader(classMirror.getVM(), classMirror.getLoader()).makeMirage(objectMirror);
        } else {
            return mirror;
        }
    }
    
    public static Object unwrapMirage(Object mirage) {
        if (mirage instanceof Mirage) {
            return ((Mirage)mirage).getMirror();
        } else {
            return mirage;
        }
    }
}
