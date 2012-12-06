package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.MethodMirror;

public class WrappingFrameMirror implements FrameMirror {

    public WrappingFrameMirror(WrappingVirtualMachine vm, FrameMirror wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }
    private final WrappingVirtualMachine vm;
    private final FrameMirror wrapped;
    
    @Override
    public ClassMirror declaringClass() {
        return (ClassMirror)vm.wrapMirror(wrapped.declaringClass());
    }
    @Override
    public String methodName() {
        return wrapped.methodName();
    }
    @Override
    public MethodMirror method() {
	return vm.wrapMethod(wrapped.method());
    }
    @Override
    public String fileName() {
	return wrapped.fileName();
    }
    @Override
    public int lineNumber() {
	return wrapped.lineNumber();
    }
    
    
}
