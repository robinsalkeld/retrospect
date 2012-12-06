package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Reflection;

public class MirrorsVirtualMachine implements VirtualMachine {

    protected final VirtualMachineMirror vm;
    
    private final Map<ObjectMirror, ObjectReference> mirrors = 
            new HashMap<ObjectMirror, ObjectReference>();

    private final Map<String, Type> primitiveTypes = new HashMap<String, Type>();
    
    public MirrorsVirtualMachine(VirtualMachineMirror vm) {
        this.vm = vm;
    }

    public static JDIDebugTarget makeDebugTarget(ThreadMirror thread, final VirtualMachine jdiVM, final ILaunch launch) {
        try {
            return Reflection.withThread(thread, new Callable<JDIDebugTarget>() {
                public JDIDebugTarget call() throws Exception {
                    JDIDebugTarget debugTarget = (JDIDebugTarget)JDIDebugModel.newDebugTarget(launch, jdiVM, jdiVM.name(), null, true, true);
                    if (launch != null) {
                        launch.addDebugTarget(debugTarget);
                    }
                    return debugTarget;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public ObjectReference wrapMirror(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        ObjectReference result = mirrors.get(mirror);
        
        if (mirror instanceof ThreadMirror) {
            result = new MirrorsThreadReference(this, (ThreadMirror)mirror);
        } else if (mirror instanceof ClassLoader) {
            result = new MirrorsClassLoaderReference(this, (ClassMirrorLoader)mirror);
        } else if (mirror instanceof ClassMirror) {
            result = new MirrorsClassObjectReference(this, (ClassMirror)mirror);
        } else if (mirror instanceof InstanceMirror) {
            String className = mirror.getClassMirror().getClassName();
            if (className.equals(ThreadGroup.class.getName())) {
                result = new MirrorsThreadGroupReference(this, (InstanceMirror)mirror);
            } else if (className.equals(String.class.getName())) {
                result = new MirrorsStringReference(this, (InstanceMirror)mirror);
            } else {
                result = new MirrorsObjectReference(this, (InstanceMirror)mirror);
            }
        } else if (mirror instanceof ArrayMirror) {
            result = new MirrorsArrayReference(this, (ArrayMirror)mirror);
        } else {
            throw new IllegalArgumentException("Unrecognized mirror class: " + mirror);
        }
        
        mirrors.put(mirror, result);
        return result;
    }
    
    public Type typeForClassMirror(final ClassMirror classMirror) {
        if (classMirror == null) {
            return null;
        }
        
        if (classMirror.isPrimitive()) {
            String className = classMirror.getClassName();
            Type result = primitiveTypes.get(className);
            if (result != null) {
                return result;
            }
            
            if (className.equals("boolean")) {
                result = new MirrorsBooleanType(this, classMirror);
            } else if (className.equals("byte")) {
                result = new MirrorsByteType(this, classMirror);
            } else if (className.equals("char")) {
                result = new MirrorsCharType(this, classMirror);
            } else if (className.equals("short")) {
                result = new MirrorsShortType(this, classMirror);
            } else if (className.equals("int")) {
                result = new MirrorsIntegerType(this, classMirror);
            } else if (className.equals("long")) {
                result = new MirrorsLongType(this, classMirror);
            } else if (className.equals("float")) {
                result = new MirrorsFloatType(this, classMirror);
            } else if (className.equals("double")) {
                result = new MirrorsDoubleType(this, classMirror);
            } else if (className.equals("void")) {
                result = new MirrorsVoidType(this, classMirror);
            }
            
            primitiveTypes.put(className, result);
            return result;
        } else {
            if (ThreadHolograph.currentThreadMirror.get() == null) {
                return Reflection.withThread(vm.getThreads().get(0), new Callable<Type>() {
                    public Type call() throws Exception {
                        return ((ClassObjectReference)wrapMirror(classMirror)).reflectedType();
                    }
                });
            } else {
                return ((ClassObjectReference)wrapMirror(classMirror)).reflectedType();
            }
        }
    }

    @Override
    public VirtualMachine virtualMachine() {
        return this;
    }

    @Override
    public List<ReferenceType> allClasses() {
//        List<ReferenceType> result = new ArrayList<ReferenceType>();
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ThreadReference> allThreads() {
        List<ThreadReference> result = new ArrayList<ThreadReference>();
        for (ThreadMirror thread : vm.getThreads()) {
            result.add((ThreadReference)wrapMirror(thread));
        }
        return result;
    }

    @Override
    public boolean canAddMethod() {
        return false;
    }

    @Override
    public boolean canBeModified() {
        return vm.canBeModified();
    }

    @Override
    public boolean canForceEarlyReturn() {
        return false;
    }

    @Override
    public boolean canGetBytecodes() {
        return false;
    }

    @Override
    public boolean canGetClassFileVersion() {
        return false;
    }

    @Override
    public boolean canGetConstantPool() {
        return false;
    }

    @Override
    public boolean canGetCurrentContendedMonitor() {
        return false;
    }

    @Override
    public boolean canGetInstanceInfo() {
        return false;
    }

    @Override
    public boolean canGetMethodReturnValues() {
        return false;
    }

    @Override
    public boolean canGetMonitorFrameInfo() {
        return false;
    }

    @Override
    public boolean canGetMonitorInfo() {
        return false;
    }

    @Override
    public boolean canGetOwnedMonitorInfo() {
        return false;
    }

    @Override
    public boolean canGetSourceDebugExtension() {
        return false;
    }

    @Override
    public boolean canGetSyntheticAttribute() {
        return false;
    }

    @Override
    public boolean canPopFrames() {
        return false;
    }

    @Override
    public boolean canRedefineClasses() {
        return false;
    }

    @Override
    public boolean canRequestMonitorEvents() {
        return false;
    }

    @Override
    public boolean canRequestVMDeathEvent() {
        return false;
    }

    @Override
    public boolean canUnrestrictedlyRedefineClasses() {
        return false;
    }

    @Override
    public boolean canUseInstanceFilters() {
        return false;
    }

    @Override
    public boolean canUseSourceNameFilters() {
        return false;
    }

    @Override
    public boolean canWatchFieldAccess() {
        return true;
    }

    @Override
    public boolean canWatchFieldModification() {
        return true;
    }

    @Override
    public List<ReferenceType> classesByName(String name) {
        List<ReferenceType> result = new ArrayList<ReferenceType>();
        for (ClassMirror klass : vm.findAllClasses(name, false)) {
            result.add((ReferenceType)typeForClassMirror(klass));
        }
        return result;
    }

    @Override
    public String description() {
        return "edu.ubc.mirrors VirtualMachine";
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public EventQueue eventQueue() {
        return new MirrorsEventQueue(this, vm.eventQueue());
    }

    @Override
    public EventRequestManager eventRequestManager() {
        return new MirrorsEventRequestManager(this, vm.eventRequestManager());
    }

    @Override
    public void exit(int arg0) {
        // Nothing to do?
    }

    @Override
    public String getDefaultStratum() {
        return "java";
    }

    @Override
    public long[] instanceCounts(List types) {
        long[] result = new long[types.size()];
        for (int i = 0; i < result.length; i++) {
            ClassMirror klass = ((MirrorsReferenceType)types.get(i)).wrapped;
            result[i] = klass.getInstances().size();
        }
        return result;
    }

    @Override
    public BooleanValue mirrorOf(boolean value) {
        return new MirrorsBooleanValue(this, value);
    }

    @Override
    public ByteValue mirrorOf(byte value) {
        return new MirrorsByteValue(this, value);
    }

    @Override
    public CharValue mirrorOf(char value) {
        return new MirrorsCharValue(this, value);
    }

    @Override
    public ShortValue mirrorOf(short value) {
        return new MirrorsShortValue(this, value);
    }

    @Override
    public IntegerValue mirrorOf(int value) {
        return new MirrorsIntegerValue(this, value);
    }

    @Override
    public LongValue mirrorOf(long value) {
        return new MirrorsLongValue(this, value);
    }

    @Override
    public FloatValue mirrorOf(float value) {
        return new MirrorsFloatValue(this, value);
    }

    @Override
    public DoubleValue mirrorOf(double value) {
        return new MirrorsDoubleValue(this, value);
    }

    @Override
    public StringReference mirrorOf(String value) {
        return new MirrorsStringReference(this, Reflection.makeString(vm, value));
    }

    @Override
    public VoidValue mirrorOfVoid() {
        return new MirrorsVoidValue(this);
    }

    @Override
    public String name() {
        return "Mirrors VirtualMachine";
    }

    @Override
    public Process process() {
        return null;
    }

    @Override
    public void redefineClasses(Map bytecodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
        vm.resume();
    }

    @Override
    public void setDebugTraceMode(int arg0) {
        // Implementations are allowed to ignore this
    }

    @Override
    public void setDefaultStratum(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void suspend() {
        vm.suspend();
    }

    @Override
    public List<ThreadGroupReference> topLevelThreadGroups() {
        // TODO-RS: What about empty groups?
        Set<ThreadGroupReference> result = new HashSet<ThreadGroupReference>();
        for (ThreadReference thread : allThreads()) {
            ThreadGroupReference group = thread.threadGroup();
            if (group.parent() == null) {
                result.add(group);
            }
        }
        return new ArrayList<ThreadGroupReference>(result);
    }

    @Override
    public String version() {
        throw new UnsupportedOperationException();
    }


    public Value getValue(InstanceMirror instance, Field field) {
        FieldMirror fieldMirror = ((MirrorsField)field).wrapped;
        org.objectweb.asm.Type fieldType = Reflection.typeForClassMirror(fieldMirror.getType());
        try {
            switch (fieldType.getSort()) {
            case org.objectweb.asm.Type.BOOLEAN: return mirrorOf(instance.getBoolean(fieldMirror));
            case org.objectweb.asm.Type.BYTE: return mirrorOf(instance.getByte(fieldMirror));
            case org.objectweb.asm.Type.CHAR: return mirrorOf(instance.getChar(fieldMirror));
            case org.objectweb.asm.Type.SHORT: return mirrorOf(instance.getShort(fieldMirror));
            case org.objectweb.asm.Type.INT: return mirrorOf(instance.getInt(fieldMirror));
            case org.objectweb.asm.Type.LONG: return mirrorOf(instance.getLong(fieldMirror));
            case org.objectweb.asm.Type.FLOAT: return mirrorOf(instance.getFloat(fieldMirror));
            case org.objectweb.asm.Type.DOUBLE: return mirrorOf(instance.getDouble(fieldMirror));
            default: return wrapMirror(instance.get(fieldMirror));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setValue(InstanceMirror instance, Field field, Value newValue) {
        FieldMirror fieldMirror = ((MirrorsField)field).wrapped;
        org.objectweb.asm.Type fieldType = Reflection.typeForClassMirror(fieldMirror.getType());
        try {
            switch (fieldType.getSort()) {
            case org.objectweb.asm.Type.BOOLEAN: 
                instance.setBoolean(fieldMirror, ((PrimitiveValue)newValue).booleanValue());
                return;
            case org.objectweb.asm.Type.BYTE: 
                instance.setByte(fieldMirror, ((PrimitiveValue)newValue).byteValue());
                return;
            case org.objectweb.asm.Type.CHAR: 
                instance.setChar(fieldMirror, ((PrimitiveValue)newValue).charValue());
                return;
            case org.objectweb.asm.Type.SHORT: 
                instance.setShort(fieldMirror, ((PrimitiveValue)newValue).shortValue());
                return;
            case org.objectweb.asm.Type.INT: 
                instance.setInt(fieldMirror, ((PrimitiveValue)newValue).intValue());
                return;
            case org.objectweb.asm.Type.LONG: 
                instance.setLong(fieldMirror, ((PrimitiveValue)newValue).longValue());
                return;
            case org.objectweb.asm.Type.FLOAT: 
                instance.setFloat(fieldMirror, ((PrimitiveValue)newValue).floatValue());
                return;
            case org.objectweb.asm.Type.DOUBLE: 
                instance.setDouble(fieldMirror, ((PrimitiveValue)newValue).doubleValue());
                return;
            default: 
                instance.set(fieldMirror, ((MirrorsObjectReference)newValue).wrapped);
                return;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Value valueForObject(Object o) {
        if (o instanceof Boolean) {
            return mirrorOf(((Boolean)o).booleanValue());
        } else if (o instanceof Byte) {
            return mirrorOf(((Byte)o).byteValue());
        } else if (o instanceof Character) {
            return mirrorOf(((Character)o).charValue());
        } else if (o instanceof Short) {
            return mirrorOf(((Short)o).shortValue());
        } else if (o instanceof Integer) {
            return mirrorOf(((Integer)o).intValue());
        } else if (o instanceof Long) {
            return mirrorOf(((Long)o).longValue());
        } else if (o instanceof Float) {
            return mirrorOf(((Float)o).floatValue());
        } else if (o instanceof Double) {
            return mirrorOf(((Double)o).doubleValue());
        } else {
            return wrapMirror((ObjectMirror)o);
        }
    }
    
    public Object[] objectsForValues(List values) {
        Object[] objects = new Object[values.size()];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = objectForValue((Value)values.get(i));
        }
        return objects;
    }
    
    public Object objectForValue(Value v) {
        if (v instanceof BooleanValue) {
            return ((BooleanValue)v).booleanValue();
        } else if (v instanceof ByteValue) {
            return ((ByteValue)v).byteValue();
        } else if (v instanceof CharValue) {
            return ((CharValue)v).charValue();
        } else if (v instanceof ShortValue) {
            return ((ShortValue)v).shortValue();
        } else if (v instanceof IntegerValue) {
            return ((IntegerValue)v).intValue();
        } else if (v instanceof LongValue) {
            return ((LongValue)v).longValue();
        } else if (v instanceof FloatValue) {
            return ((LongValue)v).floatValue();
        } else if (v instanceof DoubleValue) {
            return ((DoubleValue)v).doubleValue();
        } else {
            return ((MirrorsObjectReference)v).wrapped;
        }
    }
}
