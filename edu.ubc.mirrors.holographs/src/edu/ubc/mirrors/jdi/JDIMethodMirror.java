package edu.ubc.mirrors.jdi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirror extends JDIMethodOrConstructorMirror implements MethodMirror {

    public JDIMethodMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	if (method.name().startsWith("<")) {
	    throw new IllegalArgumentException();
	}
    }

    public String getName() {
	return method.name();
    }
    
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
	    throws IllegalArgumentException, IllegalAccessException,
	    MirrorInvocationTargetException {

        ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
        ObjectReference objRef = ((JDIObjectMirror)obj).mirror;
        
        List<Value> argValues = new ArrayList<Value>(args.length);
        for (Object arg : args) {
            argValues.add(vm.toValue(arg));
        }
        
        try {
            Value result = objRef.invokeMethod(threadRef, method, argValues, ObjectReference.INVOKE_SINGLE_THREADED);
            return vm.wrapValue(result);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            // TODO-RS: Use MirrorInvocationTargetException?
            throw new RuntimeException(e);
        }
    }
}
