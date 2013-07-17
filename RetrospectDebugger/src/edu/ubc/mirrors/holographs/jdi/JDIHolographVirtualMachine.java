package edu.ubc.mirrors.holographs.jdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holographs.HolographDebuggingThread;

public class JDIHolographVirtualMachine implements VirtualMachine, org.eclipse.jdi.hcr.VirtualMachine, org.eclipse.jdi.VirtualMachine {

    final VirtualMachine wrappedVM;
    private final org.eclipse.jdi.VirtualMachine wrappedEclipseVM;
    private final org.eclipse.jdi.hcr.VirtualMachine wrappedHCRVM;

    private ThreadReference debuggingThread;
    
    /**
     * @return
     * @see org.eclipse.jdi.hcr.VirtualMachine#canDoReturn()
     */
    public boolean canDoReturn() {
        return wrappedHCRVM.canDoReturn();
    }

    /**
     * @return
     * @see org.eclipse.jdi.hcr.VirtualMachine#canReenterOnExit()
     */
    public boolean canReenterOnExit() {
        return wrappedHCRVM.canReenterOnExit();
    }

    /**
     * @return
     * @see org.eclipse.jdi.hcr.VirtualMachine#canReloadClasses()
     */
    public boolean canReloadClasses() {
        return wrappedHCRVM.canReloadClasses();
    }

    /**
     * @param arg1
     * @return
     * @see org.eclipse.jdi.hcr.VirtualMachine#classesHaveChanged(java.lang.String[])
     */
    public int classesHaveChanged(String[] arg1) {
        return wrappedHCRVM.classesHaveChanged(arg1);
    }

    public JDIHolographVirtualMachine(VirtualMachine wrappedVM) {
        this.wrappedVM = wrappedVM;
        this.wrappedEclipseVM = (org.eclipse.jdi.VirtualMachine)wrappedVM;
        this.wrappedHCRVM = (org.eclipse.jdi.hcr.VirtualMachine)wrappedVM;
    }
    
    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#allClasses()
     */
    public List<ReferenceType> allClasses() {
        return wrapTypes(wrappedVM.allClasses());
    }

    public List<ReferenceType> wrapTypes(List<ReferenceType> wrappedClasses) {
        List<ReferenceType> result = new ArrayList<ReferenceType>();
        for (ReferenceType rt : wrappedClasses) {
            result.add(wrapReferenceType(rt));
        }
        return result;
    }
    
    public Type wrapType(Type wrapped) {
        if (wrapped instanceof ReferenceType) {
            return new HolographReferenceType(this, (ReferenceType)wrapped);
        } else {
            return wrapped;
        }
    }
    
    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#allThreads()
     */
    public List<ThreadReference> allThreads() {
        List<ThreadReference> result = new ArrayList<ThreadReference>();
        for (Object t : wrappedVM.allThreads()) {
            ThreadReference thread = (ThreadReference)t;
            if (!thread.equals(debuggingThread)) {
                result.add(wrapThread(thread));
            }
        }
        return result;
    }

    public ThreadReference wrapThread(ThreadReference wrapped) {
        return new HolographThreadReference(this, wrapped);
    }
    
    public List<ThreadReference> wrapThreads(List<ThreadReference> wrapped) {
        List<ThreadReference> result = new ArrayList<ThreadReference>();
        for (ThreadReference t : wrapped) {
            result.add(wrapThread(t));
        }
        return result;
    }
    
    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canAddMethod()
     */
    public boolean canAddMethod() {
        return wrappedVM.canAddMethod();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canBeModified()
     */
    public boolean canBeModified() {
        return wrappedVM.canBeModified();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canForceEarlyReturn()
     */
    public boolean canForceEarlyReturn() {
        return wrappedVM.canForceEarlyReturn();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetBytecodes()
     */
    public boolean canGetBytecodes() {
        return wrappedVM.canGetBytecodes();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetClassFileVersion()
     */
    public boolean canGetClassFileVersion() {
        return wrappedVM.canGetClassFileVersion();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetConstantPool()
     */
    public boolean canGetConstantPool() {
        return wrappedVM.canGetConstantPool();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetCurrentContendedMonitor()
     */
    public boolean canGetCurrentContendedMonitor() {
        return wrappedVM.canGetCurrentContendedMonitor();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetInstanceInfo()
     */
    public boolean canGetInstanceInfo() {
        return wrappedVM.canGetInstanceInfo();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetMethodReturnValues()
     */
    public boolean canGetMethodReturnValues() {
        return wrappedVM.canGetMethodReturnValues();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetMonitorFrameInfo()
     */
    public boolean canGetMonitorFrameInfo() {
        return wrappedVM.canGetMonitorFrameInfo();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetMonitorInfo()
     */
    public boolean canGetMonitorInfo() {
        return wrappedVM.canGetMonitorInfo();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetOwnedMonitorInfo()
     */
    public boolean canGetOwnedMonitorInfo() {
        return wrappedVM.canGetOwnedMonitorInfo();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetSourceDebugExtension()
     */
    public boolean canGetSourceDebugExtension() {
        return wrappedVM.canGetSourceDebugExtension();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canGetSyntheticAttribute()
     */
    public boolean canGetSyntheticAttribute() {
        return wrappedVM.canGetSyntheticAttribute();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canPopFrames()
     */
    public boolean canPopFrames() {
        return wrappedVM.canPopFrames();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canRedefineClasses()
     */
    public boolean canRedefineClasses() {
        return wrappedVM.canRedefineClasses();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canRequestMonitorEvents()
     */
    public boolean canRequestMonitorEvents() {
        return wrappedVM.canRequestMonitorEvents();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canRequestVMDeathEvent()
     */
    public boolean canRequestVMDeathEvent() {
        return wrappedVM.canRequestVMDeathEvent();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canUnrestrictedlyRedefineClasses()
     */
    public boolean canUnrestrictedlyRedefineClasses() {
        return wrappedVM.canUnrestrictedlyRedefineClasses();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canUseInstanceFilters()
     */
    public boolean canUseInstanceFilters() {
        return wrappedVM.canUseInstanceFilters();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canUseSourceNameFilters()
     */
    public boolean canUseSourceNameFilters() {
        return wrappedVM.canUseSourceNameFilters();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canWatchFieldAccess()
     */
    public boolean canWatchFieldAccess() {
        return wrappedVM.canWatchFieldAccess();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#canWatchFieldModification()
     */
    public boolean canWatchFieldModification() {
        return wrappedVM.canWatchFieldModification();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#classesByName(java.lang.String)
     */
    public List<ReferenceType> classesByName(String arg0) {
        return wrapTypes(wrappedVM.classesByName(HologramClassGenerator.getHologramBinaryClassName(arg0, true)));
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#description()
     */
    public String description() {
        return wrappedVM.description();
    }

    /**
     * 
     * @see com.sun.jdi.VirtualMachine#dispose()
     */
    public void dispose() {
        wrappedVM.dispose();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#eventQueue()
     */
    public EventQueue eventQueue() {
        return new HolographEventQueue(this, wrappedVM.eventQueue());
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#eventRequestManager()
     */
    public EventRequestManager eventRequestManager() {
        return new HolographEventRequestManager(this, wrappedVM.eventRequestManager());
    }

    /**
     * @param arg0
     * @see com.sun.jdi.VirtualMachine#exit(int)
     */
    public void exit(int arg0) {
        wrappedVM.exit(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#getDefaultStratum()
     */
    public String getDefaultStratum() {
        return wrappedVM.getDefaultStratum();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#instanceCounts(java.util.List)
     */
    public long[] instanceCounts(List arg0) {
        return wrappedVM.instanceCounts(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(boolean)
     */
    public BooleanValue mirrorOf(boolean arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(byte)
     */
    public ByteValue mirrorOf(byte arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(char)
     */
    public CharValue mirrorOf(char arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(double)
     */
    public DoubleValue mirrorOf(double arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(float)
     */
    public FloatValue mirrorOf(float arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(int)
     */
    public IntegerValue mirrorOf(int arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(long)
     */
    public LongValue mirrorOf(long arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(short)
     */
    public ShortValue mirrorOf(short arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOf(java.lang.String)
     */
    public StringReference mirrorOf(String arg0) {
        return wrappedVM.mirrorOf(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#mirrorOfVoid()
     */
    public VoidValue mirrorOfVoid() {
        return wrappedVM.mirrorOfVoid();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#name()
     */
    public String name() {
        return wrappedVM.name();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#process()
     */
    public Process process() {
        return wrappedVM.process();
    }

    /**
     * @param arg0
     * @see com.sun.jdi.VirtualMachine#redefineClasses(java.util.Map)
     */
    public void redefineClasses(Map arg0) {
        wrappedVM.redefineClasses(arg0);
    }

    /**
     * 
     * @see com.sun.jdi.VirtualMachine#resume()
     */
    public void resume() {
        wrappedVM.resume();
    }

    /**
     * @param arg0
     * @see com.sun.jdi.VirtualMachine#setDebugTraceMode(int)
     */
    public void setDebugTraceMode(int arg0) {
        wrappedVM.setDebugTraceMode(arg0);
    }

    /**
     * @param arg0
     * @see com.sun.jdi.VirtualMachine#setDefaultStratum(java.lang.String)
     */
    public void setDefaultStratum(String arg0) {
        wrappedVM.setDefaultStratum(arg0);
    }

    /**
     * 
     * @see com.sun.jdi.VirtualMachine#suspend()
     */
    public void suspend() {
        wrappedVM.suspend();
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrappedVM.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#topLevelThreadGroups()
     */
    public List<ThreadGroupReference> topLevelThreadGroups() {
        return wrapThreadGroups(wrappedVM.topLevelThreadGroups());
    }

    public List<ThreadGroupReference> wrapThreadGroups(List<ThreadGroupReference> wrappedGroups) {
        List<ThreadGroupReference> result = new ArrayList<ThreadGroupReference>();
        for (ThreadGroupReference rt : wrappedGroups) {
            result.add(wrapThreadGroup(rt));
        }
        return result;
    }
    
    /**
     * @return
     * @see com.sun.jdi.VirtualMachine#version()
     */
    public String version() {
        return wrappedVM.version();
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#virtualMachine()
     */
    public VirtualMachine virtualMachine() {
        return this;
    }
    
    public ReferenceType unwrapReferenceType(ReferenceType wrapped) {
        return ((HolographReferenceType)wrapped).wrapped;
    }
    
    public ThreadReference unwrapThreadReference(ThreadReference wrapped) {
        return ((HolographThreadReference)wrapped).wrapped;
    }

    public ThreadGroupReference wrapThreadGroup(ThreadGroupReference tg) {
        return new HolographThreadGroupReference(this, tg);
    }

    public EventRequest unwrapEventRequest(EventRequest wrapper) {
        if (wrapper instanceof HolographEventRequest) {
            return ((HolographEventRequest)wrapper).wrapped;
        } else {
            return wrapper;
        }
    }

    public ReferenceType wrapReferenceType(ReferenceType wrapped) {
        if (wrapped instanceof ClassType) {
            return new HolographClassType(this, (ClassType)wrapped);
        } else {
            return new HolographReferenceType(this, wrapped);
        }
    }
    
    public Location wrapLocation(Location wrapped) {
        return new HolographLocation(this, wrapped);
    }
    
    public List<Location> wrapLocations(List<Location> wrapped) {
        List<Location> result = new ArrayList<Location>();
        for (Location l : wrapped) {
            result.add(wrapLocation(l));
        }
        return result;
    }

    public ObjectReference wrapObjectReference(ObjectReference thisObject) {
        if (thisObject == null) {
            return null;
        }
        
        if (thisObject instanceof StringReference) {
            return thisObject;
        } else if (thisObject.referenceType().name().equals("hologram.java.lang.String")) {
            return new HolographStringReference(this, thisObject);
        } else {
            return new HolographObjectReference(this, thisObject);
        }
    }

    public ThreadReference unwrapThread(ThreadReference arg1) {
        return ((HolographThreadReference)arg1).wrapped;
    }

    public List<ObjectReference> unwrapObjectReferences(List arg3) {
        List<ObjectReference> result = new ArrayList<ObjectReference>(arg3.size());
        for (Object o : arg3) {
            result.add(unwrapObjectReference((ObjectReference)o));
        }
        return result;
    }

    private ObjectReference unwrapObjectReference(ObjectReference o) {
        return ((HolographObjectReference)o).wrapped;
    }

    public Value wrapValue(Value value) {
        if (value instanceof ObjectReference) {
            return wrapObjectReference((ObjectReference)value);
        } else {
            return value;
        }
    }
    
    public List<Value> wrapValues(List values) {
        List<Value> result = new ArrayList<Value>(values.size());
        for (Object value : values) {
            result.add(wrapValue((Value)value));
        }
        return result;
    }
    
    public Event wrapEvent(Event wrapped) {
        if (wrapped instanceof ClassPrepareEvent) {
            return new HolographClassPrepareEvent(this, (ClassPrepareEvent)wrapped);
        } else if (wrapped instanceof ThreadStartEvent) {
            return new HolographThreadStartEvent(this, (ThreadStartEvent)wrapped);
        } else if (wrapped instanceof BreakpointEvent) {
            return new HolographBreakpointEvent(this, (BreakpointEvent)wrapped);
        } else if (wrapped instanceof StepEvent) {
            return new HolographStepEvent(this, (StepEvent)wrapped);
        } else {
            return wrapped;
        }
    }
    
    public Event unwrapEvent(Event wrapper) {
        if (wrapper instanceof HolographEvent) {
            return ((HolographEvent)wrapper).wrapped;
        } else {
            return wrapper;
        }
    }

    public EventSet wrapEventSet(EventSet es) {
        return es == null ? null : new HolographEventSet(this, es);
    }

    public Method unwrapMethod(Method arg1) {
        return ((HolographMethod)arg1).wrapped;
    }

    @Override
    public void setRequestTimeout(int timeout) {
        wrappedEclipseVM.setRequestTimeout(timeout);
    }

    @Override
    public int getRequestTimeout() {
        return wrappedEclipseVM.getRequestTimeout();
    }
    
    public ThreadReference getDebuggingThread() {
        if (debuggingThread == null) {
            for (Object t : wrappedVM.allThreads()) {
                ThreadReference thread = (ThreadReference)t;
                if (thread.name().equals("HolographDebuggingThread")) {
                    debuggingThread = thread;
                    break;
                }
            }
            if (debuggingThread == null) {
                throw new IllegalStateException();
            }
            if (!debuggingThread.isAtBreakpoint()) {
                ReferenceType vmHolographType = (ReferenceType)wrappedVM.classesByName(HolographDebuggingThread.class.getName()).get(0);
                try {
                    BreakpointRequest request = eventRequestManager().createBreakpointRequest((Location)vmHolographType.locationsOfLine(10).get(0));
                    request.enable();
                } catch (AbsentInformationException e) {
                    throw new InternalException();
                }
                while (!debuggingThread.isAtBreakpoint());
            }
        }
        
        return debuggingThread;
    }
    
    public Value invokeMethodHandle(ObjectReference target, MethodHandle methodHandle, Value ... args) {
        // TODO-RS: Handle overloading
        ReferenceType targetClass = (ReferenceType)wrappedVM.classesByName(methodHandle.getMethod().owner.replace("/", ".")).get(0);
        Method method = (Method)targetClass.methodsByName(methodHandle.getMethod().name).get(0);
        if (methodHandle.getMethod().getOpcode() == Opcodes.INVOKESTATIC) {
            return invokeMethod((ClassType)targetClass, method, args);
        } else {
            return invokeMethod(target, method, args);
        }
    }
    
    public synchronized Value invokeMethod(ObjectReference target, Method method, Value ... args) {
        try {
            return target.invokeMethod(getDebuggingThread(), method, Arrays.asList(args), 0);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public synchronized Value invokeMethod(ClassType type, Method method, Value ... args) {
        try {
            return type.invokeMethod(getDebuggingThread(), method, Arrays.asList(args), 0);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            throw new RuntimeException(e);
        }
    }
}

    