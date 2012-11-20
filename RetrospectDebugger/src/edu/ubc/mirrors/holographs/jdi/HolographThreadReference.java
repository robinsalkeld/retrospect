package edu.ubc.mirrors.holographs.jdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.MonitorInfo;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class HolographThreadReference extends Holograph implements ThreadReference {

    final ThreadReference wrapped;
    
    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#currentContendedMonitor()
     */
    public ObjectReference currentContendedMonitor()
            throws IncompatibleThreadStateException {
        return wrapped.currentContendedMonitor();
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#disableCollection()
     */
    public void disableCollection() {
        wrapped.disableCollection();
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#enableCollection()
     */
    public void enableCollection() {
        wrapped.enableCollection();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#entryCount()
     */
    public int entryCount() throws IncompatibleThreadStateException {
        return wrapped.entryCount();
    }

    /**
     * @param arg0
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#forceEarlyReturn(com.sun.jdi.Value)
     */
    public void forceEarlyReturn(Value arg0) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException {
        wrapped.forceEarlyReturn(arg0);
    }

    /**
     * @param arg0
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#frame(int)
     */
    public StackFrame frame(int arg0) throws IncompatibleThreadStateException {
        return new HolographStackFrame(vm, wrapped.frame(arg0));
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#frameCount()
     */
    public int frameCount() throws IncompatibleThreadStateException {
        return wrapped.frameCount();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#frames()
     */
    public List<StackFrame> frames() throws IncompatibleThreadStateException {
        List<StackFrame> wrappedFrames = wrapped.frames();
        List<StackFrame> result = new ArrayList<StackFrame>();
        for (StackFrame f : wrappedFrames) {
            result.add(new HolographStackFrame(vm, f));
        }
        return result;
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#frames(int, int)
     */
    public List<StackFrame> frames(int arg0, int arg1)
            throws IncompatibleThreadStateException {
        List<StackFrame> wrappedFrames = wrapped.frames(arg0, arg1);
        List<StackFrame> result = new ArrayList<StackFrame>();
        for (StackFrame f : wrappedFrames) {
            result.add(new HolographStackFrame(vm, f));
        }
        return result;
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ObjectReference#getValue(com.sun.jdi.Field)
     */
    public Value getValue(Field arg0) {
        return wrapped.getValue(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ObjectReference#getValues(java.util.List)
     */
    public Map<Field, Value> getValues(List arg0) {
        return wrapped.getValues(arg0);
    }

    /**
     * 
     * @see com.sun.jdi.ThreadReference#interrupt()
     */
    public void interrupt() {
        wrapped.interrupt();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @return
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @see com.sun.jdi.ObjectReference#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
     */
    public Value invokeMethod(ThreadReference arg0, Method arg1,
            List arg2, int arg3) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException,
            InvocationException {
        return wrapped.invokeMethod(arg0, arg1, arg2, arg3);
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#isAtBreakpoint()
     */
    public boolean isAtBreakpoint() {
        return wrapped.isAtBreakpoint();
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#isCollected()
     */
    public boolean isCollected() {
        return wrapped.isCollected();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#isSuspended()
     */
    public boolean isSuspended() {
        return wrapped.isSuspended();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#name()
     */
    public String name() {
        return wrapped.name();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#ownedMonitors()
     */
    public List<ObjectReference> ownedMonitors()
            throws IncompatibleThreadStateException {
        return wrapped.ownedMonitors();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#ownedMonitorsAndFrames()
     */
    public List<MonitorInfo> ownedMonitorsAndFrames()
            throws IncompatibleThreadStateException {
        return wrapped.ownedMonitorsAndFrames();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#owningThread()
     */
    public ThreadReference owningThread()
            throws IncompatibleThreadStateException {
        return wrapped.owningThread();
    }

    /**
     * @param arg0
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ThreadReference#popFrames(com.sun.jdi.StackFrame)
     */
    public void popFrames(StackFrame arg0)
            throws IncompatibleThreadStateException {
        wrapped.popFrames(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#referenceType()
     */
    public ReferenceType referenceType() {
        return wrapped.referenceType();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ObjectReference#referringObjects(long)
     */
    public List<ObjectReference> referringObjects(long arg0) {
        return wrapped.referringObjects(arg0);
    }

    /**
     * 
     * @see com.sun.jdi.ThreadReference#resume()
     */
    public void resume() {
        wrapped.resume();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.ObjectReference#setValue(com.sun.jdi.Field, com.sun.jdi.Value)
     */
    public void setValue(Field arg0, Value arg1) throws InvalidTypeException,
            ClassNotLoadedException {
        wrapped.setValue(arg0, arg1);
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#status()
     */
    public int status() {
        return wrapped.status();
    }

    /**
     * @param arg0
     * @throws InvalidTypeException
     * @see com.sun.jdi.ThreadReference#stop(com.sun.jdi.ObjectReference)
     */
    public void stop(ObjectReference arg0) throws InvalidTypeException {
        wrapped.stop(arg0);
    }

    /**
     * 
     * @see com.sun.jdi.ThreadReference#suspend()
     */
    public void suspend() {
        wrapped.suspend();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#suspendCount()
     */
    public int suspendCount() {
        return wrapped.suspendCount();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadReference#threadGroup()
     */
    public ThreadGroupReference threadGroup() {
        return wrapped.threadGroup();
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.Value#type()
     */
    public Type type() {
        return wrapped.type();
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#uniqueID()
     */
    public long uniqueID() {
        return wrapped.uniqueID();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#waitingThreads()
     */
    public List<ThreadReference> waitingThreads()
            throws IncompatibleThreadStateException {
        return vm.wrapThreads(wrapped.waitingThreads());
    }

    public HolographThreadReference(JDIHolographVirtualMachine vm, ThreadReference wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
