package edu.ubc.mirrors.raw.nativestubs.java.lang;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class ThrowableStubs extends NativeStubs {

    public ThrowableStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage fillInStackTrace(Mirage throwable) {
	try {
	    throwable.getClass().getMethod("superFillInStackTrace").invoke(throwable);
	} catch (IllegalAccessException e) {
	    throw new IllegalAccessError(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchMethodException e) {
	    throw new NoSuchMethodError(e.getMessage());
	}
	
	// The native method has the side-effect of clearing the stackTrace field, so make sure the mirror does that too.
	InstanceMirror throwableMirror = (InstanceMirror)throwable.getMirror();
	HolographInternalUtils.setField(throwableMirror, "stackTrace", null);
	
	return throwable;
    }
    
    // Java 1.7 version
    public Mirage fillInStackTrace(Mirage throwable, int dummy) {
	return fillInStackTrace(throwable);
    }
    
    private StackTraceElement[] getNativeStack(Mirage throwable) {
	try {
	    return (StackTraceElement[])throwable.getClass().getMethod("superGetStackTrace").invoke(throwable);
	} catch (IllegalAccessException e) {
	    throw new IllegalAccessError(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchMethodException e) {
	    throw new NoSuchMethodError(e.getMessage());
	}
    }
    
    // TODO-RS: Pretty darn expensive, but caching this correctly is a bit tricky so leave that for later...
    public int getStackTraceDepth(Mirage throwable) {
	return getNativeStack(throwable).length;
    }
    
    // TODO-RS: Pretty darn expensive, but caching this correctly is a bit tricky so leave that for later...
    public Mirage getStackTraceElement(Mirage throwable, int index) {
	VirtualMachineMirror vm = throwable.getMirror().getClassMirror().getVM();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror intClass = vm.getPrimitiveClass("int");
        ConstructorMirror constructor = HolographInternalUtils.getConstructor(stackTraceElementClass, stringClass, stringClass, stringClass, intClass);
        
	StackTraceElement nativeFrame = getNativeStack(throwable)[index];
        InstanceMirror className = Reflection.makeString(vm, getOriginalBinaryClassName(nativeFrame.getClassName()));
        InstanceMirror methodName = Reflection.makeString(vm, nativeFrame.getMethodName());
        InstanceMirror fieldName = Reflection.makeString(vm, nativeFrame.getFileName());
        int lineNumber = nativeFrame.getLineNumber();
        InstanceMirror mapped = HolographInternalUtils.newInstance(constructor, ThreadHolograph.currentThreadMirror(), className, methodName, fieldName, lineNumber);
        return ObjectMirage.make(mapped);
    }
    
}
