package edu.ubc.mirrors.holographs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class ConstructorHolograph implements ConstructorMirror {

    private final ClassHolograph klass;
    private final ConstructorMirror wrapped;
    private ConstructorMirror bytecodeConstructor;
    private Constructor<?> mirageClassConstructor;
    
    public ConstructorHolograph(ClassHolograph klass, ConstructorMirror wrapped) {
	this.klass = klass;
        this.wrapped = wrapped;
    }

    private ConstructorMirror getBytecodeConstructor() {
	if (bytecodeConstructor == null) {
	    try {
		bytecodeConstructor = klass.getBytecodeMirror().getConstructor(wrapped.getParameterTypes().toArray(new ClassMirror[0]));
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
	return bytecodeConstructor;
    }
    
    private void resolveConstructor() {
        List<ClassMirror> paramTypes = getParameterTypes();
        // Add the extra implicit mirror parameter
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.size() + 1];
        for (int i = 0; i < mirageParamTypes.length - 1; i++) {
            mirageParamTypes[i] = ClassHolograph.getMirageClass(paramTypes.get(i), false);
        }
        mirageParamTypes[mirageParamTypes.length - 1] = InstanceMirror.class;
        
        Class<?> mirageClass = ClassHolograph.getMirageClass(klass, true);
        
        try {
            mirageClassConstructor = mirageClass.getDeclaredConstructor(mirageParamTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError();
        }
        mirageClassConstructor.setAccessible(true);
    }
    
    @Override
    public InstanceMirror newInstance(ThreadMirror thread, Object ... args) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (thread == null) {
            throw new NullPointerException();
        }
        
        ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        threadHolograph.enterHologramExecution();
        try {
            resolveConstructor();
            
            // Add the extra implicit mirror parameter
            Class<?> classLoaderLiteral = mirageClassConstructor.getDeclaringClass();
            InstanceMirror mirror = ObjectMirage.newInstanceMirror(classLoaderLiteral, classLoaderLiteral.getName().replace('/', '.'));
            Object[] mirageArgs = new Object[args.length + 1];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = ClassHolograph.makeMirage(args[i]);
            }
            mirageArgs[args.length] = mirror;
            Object result = mirageClassConstructor.newInstance(mirageArgs);
            return (InstanceMirror)ClassHolograph.unwrapMirage(result);
        } catch (InstantiationException e) {
            ClassHolograph.cleanStackTrace(e);
            throw e;
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        return wrapped.getParameterTypes();
    }

    @Override
    public byte[] getRawAnnotations() {
        return wrapped.getRawAnnotations();
    }

    @Override
    public byte[] getRawParameterAnnotations() {
        return wrapped.getRawParameterAnnotations();
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return klass;
    }

    @Override
    public int getSlot() {
        return wrapped.getSlot();
    }

    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
	try {
	    return wrapped.getExceptionTypes();
	} catch (UnsupportedOperationException e) {
	    return getBytecodeConstructor().getExceptionTypes();
	}
    }

    @Override
    public String getSignature() {
        return wrapped.getSignature();
    }
}