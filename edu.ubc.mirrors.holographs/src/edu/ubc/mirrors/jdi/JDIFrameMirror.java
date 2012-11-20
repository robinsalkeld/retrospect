package edu.ubc.mirrors.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.StackFrame;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;

public class JDIFrameMirror extends JDIMirror implements FrameMirror {

    private final StackFrame frame;
    
    public JDIFrameMirror(JDIVirtualMachineMirror vm, StackFrame frame) {
	super(vm, frame);
	this.frame = frame;
    }

    @Override
    public ClassMirror declaringClass() {
	return vm.makeClassMirror(frame.location().declaringType());
    }

    @Override
    public String methodName() {
	return frame.location().method().name();
    }

    @Override
    public String fileName() {
	try {
            return frame.location().sourceName();
        } catch (AbsentInformationException e) {
            return "<Source Unknown>";
        }
    }

    @Override
    public int lineNumber() {
	return frame.location().lineNumber();
    }

}
