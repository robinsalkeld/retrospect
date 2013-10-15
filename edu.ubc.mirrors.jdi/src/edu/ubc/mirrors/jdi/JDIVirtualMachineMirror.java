/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class JDIVirtualMachineMirror implements VirtualMachineMirror {

    protected final VirtualMachine jdiVM;

    private final Map<Mirror, ObjectMirror> mirrors = new HashMap<Mirror, ObjectMirror>();
    
    private final Map<String, ClassMirror> bootstrapClasses = new HashMap<String, ClassMirror>();
    
    public JDIVirtualMachineMirror(VirtualMachine jdiVM) {
        this.jdiVM = jdiVM;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIVirtualMachineMirror)) {
            return false;
        }
        
        JDIVirtualMachineMirror other = (JDIVirtualMachineMirror)obj;
        return jdiVM == other.jdiVM;
    }
    
    @Override
    public int hashCode() {
        return 11 * jdiVM.hashCode();
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
            
        for (ReferenceType t : jdiVM.classesByName(name)) {
            if (t.classLoader() == null) {
                result = makeClassMirror(t.classObject());
                bootstrapClasses.put(name, result);
                return result;
            }
        }
        
        return null;
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : jdiVM.allClasses()) {
            classes.add(((ClassMirror)makeMirror(t.classObject())));
        }
        return classes;
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : jdiVM.classesByName(name)) {
            if (includeSubclasses) {
        	collectSubclasses(t, classes);
            } else {
        	classes.add(((ClassMirror)makeMirror(t.classObject())));
            }
        }
        return classes;
    }

    private void collectSubclasses(ReferenceType t, List<ClassMirror> classes) {
	classes.add(((ClassMirror)makeMirror(t.classObject())));
	if (t instanceof ClassType) {
	    ClassType classType = (ClassType)t;
	    for (ReferenceType subclass : classType.subclasses()) {
		collectSubclasses(subclass, classes);
	    }
	} else if (t instanceof InterfaceType) {
	    InterfaceType interfaceType = (InterfaceType)t;
	    for (ReferenceType implementor : interfaceType.implementors()) {
		collectSubclasses(implementor, classes);
	    }
	    for (ReferenceType extender : interfaceType.subinterfaces()) {
		collectSubclasses(extender, classes);
	    }
	}
    }

    @Override
    public List<ThreadMirror> getThreads() {
        List<ThreadMirror> threads = new ArrayList<ThreadMirror>();
        for (ThreadReference t : jdiVM.allThreads()) {
            threads.add(((ThreadMirror)makeMirror(t)));
        }
        return threads;
    }

    public ObjectMirror makeMirror(ObjectReference t) {
        if (t == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(t);
        if (result != null) {
            return result;
        }
        
        if (t instanceof ClassObjectReference) {
            result = new JDIClassMirror(this, (ClassObjectReference)t);
        } else if (t instanceof ClassLoaderReference) {
            result = new JDIClassLoaderMirror(this, (ClassLoaderReference)t);
        } else if (t instanceof ThreadReference) {
            result = new JDIThreadMirror(this, (ThreadReference)t);
        } else if (t instanceof ArrayReference) {
            result = new JDIArrayMirror(this, (ArrayReference)t);
        } else if (t instanceof ObjectReference) {
            result = new JDIInstanceMirror(this, (ObjectReference)t);
        } else {
            throw new IllegalArgumentException();
        }
        
        mirrors.put(t, result);
        
        return result;
    }

    public ClassMirror makeClassMirror(ObjectReference r) {
        return (ClassMirror)makeMirror(r);
    }
    
    public ClassMirror makeClassMirror(Type t) {
        if (t == null) {
            return null;
        } else if (t instanceof ReferenceType) {
            return (ClassMirror)makeMirror(((ReferenceType)t).classObject());
        } else {
            return getPrimitiveClass(t.name());
        }
    }
    
    public List<ClassMirror> makeClassMirrorList(List<? extends Type> types) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(types.size());
        for (Type type : types) {
            result.add(makeClassMirror(type));
        }
        return result;
    }
    
    private final Map<String, ClassMirror> primitiveClasses = 
	  new HashMap<String, ClassMirror>();
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
	ClassMirror result = primitiveClasses.get(name);
	if (result != null) {
	    return result;
	}
	
	// Unfortunately we need to run code to get at these.
	// The method should be completely side-effect free though.
	ReferenceType classType = jdiVM.classesByName(Class.class.getName()).get(0);
	Method method = classType.methodsByName("getPrimitiveClass").get(0);
	ThreadHolograph currentThread = ThreadHolograph.currentThreadMirror();
	ThreadReference threadRef = ((JDIThreadMirror)currentThread.getWrapped()).thread;
	ClassObjectReference cor;
	try {
	    cor = (ClassObjectReference)classType.classObject().invokeMethod(threadRef, method, Collections.singletonList(jdiVM.mirrorOf(name)), ObjectReference.INVOKE_SINGLE_THREADED);
	} catch (InvalidTypeException e) {
	    throw new InternalError(e.getMessage());
	} catch (ClassNotLoadedException e) {
	    // Should never happen - Class must be loaded by the time we get a VMStartEvent
	    throw new RuntimeException(e);
	} catch (IncompatibleThreadStateException e) {
	    // Should never happen
	    throw new RuntimeException(e);
	} catch (InvocationException e) {
	    throw new RuntimeException(e);
	}
	result = new JDIClassMirror(this, cor);
	primitiveClasses.put(name, result);
	return result;
    }

    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
	String name = elementClass.getClassName();
	for (int d = 0; d < dimensions; d++) {
	    name += "[]";
	}
	ClassMirror result = findBootstrapClassMirror(name);
	if (result != null) {
	    return result;
	}
	
        return new ArrayClassMirror(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
	return new JDIMirrorEventRequestManager(this, jdiVM.eventRequestManager());
    }
    
    @Override
    public MirrorEventQueue eventQueue() {
        return new JDIMirrorEventQueue(this, jdiVM.eventQueue());
    }
    
    @Override
    public void suspend() {
	jdiVM.suspend(); 
    }
    
    @Override
    public void resume() {
	jdiVM.resume();
    }

    public Object wrapValue(Value value) {
	if (value instanceof BooleanValue) {
	    return ((BooleanValue)value).booleanValue();
	} else if (value instanceof ByteValue) {
	    return ((ByteValue)value).byteValue();
	} else if (value instanceof CharValue) {
	    return ((CharValue)value).charValue();
	} else if (value instanceof ShortValue) {
	    return ((ShortValue)value).shortValue();
	} else if (value instanceof IntegerValue) {
	    return ((IntegerValue)value).intValue();
	} else if (value instanceof LongValue) {
	    return ((LongValue)value).longValue();
	} else if (value instanceof FloatValue) {
	    return ((FloatValue)value).floatValue();
	} else if (value instanceof DoubleValue) {
	    return ((DoubleValue)value).doubleValue();
	} else {
	    return makeMirror((ObjectReference)value);
	}
    }
    
    public Value toValue(Object object) {
        if (object instanceof Boolean) {
            return jdiVM.mirrorOf(((Boolean)object).booleanValue());
        } else if (object instanceof Byte) {
            return jdiVM.mirrorOf(((Byte)object).byteValue());
        } else if (object instanceof Character) {
            return jdiVM.mirrorOf(((Character)object).charValue());
        } else if (object instanceof Short) {
            return jdiVM.mirrorOf(((Short)object).shortValue());
        } else if (object instanceof Integer) {
            return jdiVM.mirrorOf(((Integer)object).intValue());
        } else if (object instanceof Long) {
            return jdiVM.mirrorOf(((Long)object).longValue());
        } else if (object instanceof Float) {
            return jdiVM.mirrorOf(((Float)object).floatValue());
        } else if (object instanceof Double) {
            return jdiVM.mirrorOf(((Float)object).doubleValue());
        } else {
            return ((JDIObjectMirror)object).mirror;
        }
    }
    
    @Override
    public boolean canBeModified() {
        return false; //jdiVM.canBeModified();
    }
    
    @Override
    public boolean canGetBytecodes() {
        // TODO-RS: If I refactored the API to just expose bytecode for individual methods,
        // this could be true.
        return false;
    }
    
    @Override
    public boolean hasClassInitialization() {
        return true;
    }
    
    int identityHashCode(ObjectReference object) {
        // Unfortunately we need to run code to get at these.
        // The method should be completely side-effect free though.
        ReferenceType objectType = jdiVM.classesByName(Object.class.getName()).get(0);
        Method method = objectType.methodsByName("hashCode").get(0);
        ThreadHolograph currentThread = ThreadHolograph.currentThreadMirror();
        ThreadReference threadRef = ((JDIThreadMirror)currentThread.getWrapped()).thread;
        try {
            Value result = object.invokeMethod(threadRef, method, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            return ((IntegerValue)result).intValue();
        } catch (InvalidTypeException e) {
            throw new InternalError(e.getMessage());
        } catch (ClassNotLoadedException e) {
            // Should never happen
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            // Should never happen
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public InstanceMirror getInternedString(String s) {
        throw new UnsupportedOperationException();
    }
}
