package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.wrapping.WrappingFrameMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class FrameHolograph extends WrappingFrameMirror {

    public FrameHolograph(WrappingVirtualMachine vm, FrameMirror wrapped) {
        super(vm, wrapped);
    }

    @Override
    public MethodMirror method() {
        try {
            return super.method();
        } catch (UnsupportedOperationException e) {
            String methodName = methodName();
            for (MethodMirror method : declaringClass().getDeclaredMethods(false)) {
                // TODO-RS: Need to figure out a way to disambiguate, probably using line numbers?
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            throw new InternalError("Can't locate method: " + declaringClass().getClassName() + "#" + methodName);
        }
    }
}
