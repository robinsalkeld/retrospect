package edu.ubc.mirrors.holographs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeConstructorMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class ClassHolograph extends WrappingClassMirror {

    public static ThreadLocal<ThreadMirror> currentThreadMirror = new ThreadLocal<ThreadMirror>();
    
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
        // Just to check that the method exists.
        super.getMethod(name, paramTypes);
        return new MirageMethod(name, paramTypes);
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
        
        Class<?> mirageClass = getMirageClass(this, true);
        Constructor<?> constructor = mirageClass.getDeclaredConstructor(mirageParamTypes);
        return new MirageConstructor(constructor);
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        Class<?> mirageClass = getMirageClass(this, true);
        Constructor<?>[] constructors = publicOnly ? mirageClass.getConstructors() : mirageClass.getDeclaredConstructors();
        List<ConstructorMirror> result = new ArrayList<ConstructorMirror>();
        for (Constructor<?> constructor : constructors) {
            result.add(new MirageConstructor(constructor));
        }
        return result;
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
    
    public MirageClassLoader getMirageClassLoader() {
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
    
    private class MirageMethod implements MethodMirror {

        private final String name;
        private final ClassMirror[] paramTypes;
        
        private Method mirageClassMethod;
        private boolean accessible = false;
        
        public MirageMethod(String name, ClassMirror[] paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes;
        }
        
        private void resolveMethod() {
            Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                mirageParamTypes[i] = getMirageClass(paramTypes[i], false);
            }
            Class<?> mirageClass = getMirageClass(true);
            try {
                mirageClassMethod = mirageClass.getDeclaredMethod(name, mirageParamTypes);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(name);
            }
            mirageClassMethod.setAccessible(accessible);
        }
        
        @Override
        public Object invoke(ThreadMirror thread, InstanceMirror obj, Object ... args) throws IllegalAccessException, InvocationTargetException {
            ThreadMirror original = currentThreadMirror.get();
            currentThreadMirror.set(thread);
            try {
                resolveMethod();
                
                Object[] mirageArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    mirageArgs[i] = makeMirage(args[i]);
                }
                Object mirageObj = makeMirage(obj);
                Object result = mirageClassMethod.invoke(mirageObj, mirageArgs);
                // Account for the fact that toString() has to return a real String here
                if (result instanceof String) {
                    return Reflection.makeString(vm, (String)result);
                } else {
                    return unwrapMirage(result);
                }
            } finally {
                currentThreadMirror.set(original);
            }
        }
        
        @Override
        public void setAccessible(boolean flag) {
            if (mirageClassMethod != null) {
                mirageClassMethod.setAccessible(flag);
            } else {
                accessible = flag;
            }
        }
    }
    
    private class MirageConstructor extends NativeConstructorMirror implements ConstructorMirror {

        private final Constructor<?> mirageClassConstructor;
        
        public MirageConstructor(Constructor<?> mirageClassConstructor) {
            super(mirageClassConstructor);
            this.mirageClassConstructor = mirageClassConstructor;
            this.mirageClassConstructor.setAccessible(true);
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
        public ClassMirror getDeclaringClass() {
            return ClassHolograph.this;
        }
        
        @Override
        public List<ClassMirror> getParameterTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            Class<?>[] mirageParamTypes = mirageClassConstructor.getParameterTypes();
            // Skip the extra mirror argument
            for (int i = 0; i < mirageParamTypes.length - 1; i++) {
                result.add(getOriginalClassMirror(mirageParamTypes[i]));
            }
            return result;
        }
        
        @Override
        public List<ClassMirror> getExceptionTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            Class<?>[] mirageParamTypes = mirageClassConstructor.getExceptionTypes();
            for (Class<?> mirageParamType : mirageParamTypes) {
                result.add(getOriginalClassMirror(mirageParamType));
            }
            return result;
        }
        
        // TODO-RS: May need to translate annotations back to original classes too...
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
    
    public static ClassMirror getOriginalClassMirror(Class<?> mirageValue) {
        MirageClassLoader loader = (MirageClassLoader)mirageValue.getClassLoader();
        return loader.loadOriginalClassMirror(MirageClassGenerator.getOriginalBinaryClassName(mirageValue.getName()));
    }

    public static Object unwrapMirage(Object mirage) {
        if (mirage instanceof Mirage) {
            return ((Mirage)mirage).getMirror();
        } else {
            return mirage;
        }
    }
}
