package edu.ubc.mirrors.holographs.jdi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class HolographStackFrame extends Holograph implements StackFrame {

    private final StackFrame wrapped;
    
    /**
     * @return
     * @see com.sun.jdi.StackFrame#getArgumentValues()
     */
    public List<Value> getArgumentValues() {
        return vm.wrapValues(wrapped.getArgumentValues());
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.StackFrame#getValue(com.sun.jdi.LocalVariable)
     */
    public Value getValue(LocalVariable arg0) {
        return vm.wrapValue(wrapped.getValue(arg0));
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.StackFrame#getValues(java.util.List)
     */
    public Map<LocalVariable, Value> getValues(List arg0) {
        Map<LocalVariable, Value> result = new HashMap<LocalVariable, Value>();
        for (Object o : wrapped.getValues(arg0).entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            LocalVariable key = (LocalVariable)entry.getKey();
            Value value = (Value)entry.getValue();
            result.put(key, vm.wrapValue(value));
        }
        return result;
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#location()
     */
    public Location location() {
        return new HolographLocation(vm, wrapped.location());
    }

    /**
     * @param arg0
     * @param arg1
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.StackFrame#setValue(com.sun.jdi.LocalVariable, com.sun.jdi.Value)
     */
    public void setValue(LocalVariable arg0, Value arg1)
            throws InvalidTypeException, ClassNotLoadedException {
        wrapped.setValue(arg0, arg1);
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#thisObject()
     */
    public ObjectReference thisObject() {
        return vm.wrapObjectReference(wrapped.thisObject());
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.StackFrame#visibleVariableByName(java.lang.String)
     */
    public LocalVariable visibleVariableByName(String arg0)
            throws AbsentInformationException {
        return wrapped.visibleVariableByName(arg0);
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.StackFrame#visibleVariables()
     */
    public List<LocalVariable> visibleVariables()
            throws AbsentInformationException {
        return wrapped.visibleVariables();
    }

    public HolographStackFrame(JDIHolographVirtualMachine vm, StackFrame frame) {
        super(vm, frame);
        this.wrapped = frame;
    }

}
