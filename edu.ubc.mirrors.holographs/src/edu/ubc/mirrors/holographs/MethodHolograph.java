package edu.ubc.mirrors.holographs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class MethodHolograph implements MethodMirror {

    private final ClassHolograph klass;
    private final MethodMirror wrapped;
    private MethodMirror bytecodeMethod;
    private Method hologramClassMethod;
    private boolean accessible = false;
    
    public MethodHolograph(ClassHolograph klass, MethodMirror wrapped) {
	this.klass = klass;
        this.wrapped = wrapped;
    }

    private MethodMirror getBytecodeMethod() {
	if (bytecodeMethod == null) {
	    try {
		bytecodeMethod = klass.getBytecodeMirror().getDeclaredMethod(wrapped.getName(), wrapped.getParameterTypes().toArray(new ClassMirror[0]));
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
	return bytecodeMethod;
    }
    
    private void resolveMethod() {
        List<ClassMirror> paramTypes = getParameterTypes();
        Class<?>[] hologramParamTypes = new Class<?>[paramTypes.size()];
        for (int i = 0; i < hologramParamTypes.length; i++) {
            hologramParamTypes[i] = ClassHolograph.getHologramClass(paramTypes.get(i), false);
        }
        Class<?> hologramClass = klass.getHologramClass(true);
        // Account for the fact that ObjectHologram is not actually the top of the type lattice
        if (klass.getClassName().equals(Object.class.getName())) {
            hologramClass = Object.class;
        }
        try {
            hologramClassMethod = hologramClass.getDeclaredMethod(getName(), hologramParamTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(getName());
        }
        hologramClassMethod.setAccessible(accessible);
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        if (thread == null) {
            throw new NullPointerException();
        }
        
        ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        threadHolograph.enterHologramExecution();
        try {
            resolveMethod();
            
            Object[] hologramArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                hologramArgs[i] = ClassHolograph.makeHologram(args[i]);
            }
            Object hologramObj = ClassHolograph.makeHologram(obj);
            try {
                Object result = hologramClassMethod.invoke(hologramObj, hologramArgs);
                // Account for the fact that toString() has to return a real String here
                if (result instanceof String) {
                    return Reflection.makeString(klass.getVM(), (String)result);
                } else {
                    return ClassHolograph.unwrapHologram(result);
                }
            
            } catch (InvocationTargetException e) {
                throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
            }
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        if (hologramClassMethod != null) {
            hologramClassMethod.setAccessible(flag);
        } else {
            accessible = flag;
        }
    }
    
    @Override
    public List<String> getParameterTypeNames() {
        return wrapped.getParameterTypeNames();
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        return wrapped.getParameterTypes();
    }
    
    @Override
    public String getReturnTypeName() {
        return wrapped.getReturnTypeName();
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
    public List<String> getExceptionTypeNames() {
        if (klass.hasBytecode()) {
            return wrapped.getExceptionTypeNames();
        } else {
            return getBytecodeMethod().getExceptionTypeNames();
        }
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        if (klass.hasBytecode()) {
            return wrapped.getExceptionTypes();
	} else {
	    return getBytecodeMethod().getExceptionTypes();
	}
    }

    @Override
    public String getSignature() {
        return wrapped.getSignature();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}