package edu.ubc.mirrors.holographs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingMethodMirror;

public class MethodHolograph extends WrappingMethodMirror {

    private final ClassHolograph klass;
    private final MethodMirror wrapped;
    private MethodMirror bytecodeMethod;
    private Method mirageClassMethod;
    private boolean accessible = false;
    
    public MethodHolograph(ClassHolograph klass, MethodMirror wrapped) {
        super(klass.getVM(), wrapped);
	this.klass = klass;
        this.wrapped = wrapped;
    }

    private MethodMirror getBytecodeMethod() {
	if (bytecodeMethod == null) {
	    try {
		bytecodeMethod = klass.getBytecodeMirror().getMethod(wrapped.getName(), wrapped.getParameterTypes().toArray(new ClassMirror[0]));
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
	return bytecodeMethod;
    }
    
    private void resolveMethod() {
        List<ClassMirror> paramTypes = getParameterTypes();
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.size()];
        for (int i = 0; i < mirageParamTypes.length; i++) {
            mirageParamTypes[i] = ClassHolograph.getMirageClass(paramTypes.get(i), false);
        }
        Class<?> mirageClass = klass.getMirageClass(true);
        // Account for the fact that ObjectMirage is not actually the top of the type lattice
        if (klass.getClassName().equals(Object.class.getName())) {
            mirageClass = Object.class;
        }
        try {
            mirageClassMethod = mirageClass.getDeclaredMethod(getName(), mirageParamTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(getName());
        }
        mirageClassMethod.setAccessible(accessible);
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalAccessException, InvocationTargetException {
        if (thread == null) {
            throw new NullPointerException();
        }
        
        ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        threadHolograph.enterHologramExecution();
        try {
            resolveMethod();
            
            Object[] mirageArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = ClassHolograph.makeMirage(args[i]);
            }
            Object mirageObj = ClassHolograph.makeMirage(obj);
            Object result = mirageClassMethod.invoke(mirageObj, mirageArgs);
            // Account for the fact that toString() has to return a real String here
            if (result instanceof String) {
                return Reflection.makeString(klass.getVM(), (String)result);
            } else {
                return ClassHolograph.unwrapMirage(result);
            }
        } catch (InvocationTargetException e) {
            ClassHolograph.cleanStackTrace(e);
            throw e;
        } finally {
            threadHolograph.enterHologramExecution();
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
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        return wrapped.getParameterTypes();
    }
    
    @Override
    public ClassMirror getReturnType() {
        return wrapped.getReturnType();
    }

    @Override
    public String getName() {
        return wrapped.getName();
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
    public byte[] getRawAnnotationDefault() {
        return wrapped.getRawAnnotationDefault();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return wrapped.getDeclaringClass();
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
	    return getBytecodeMethod().getExceptionTypes();
	}
    }

    @Override
    public String getSignature() {
        return wrapped.getSignature();
    }
}