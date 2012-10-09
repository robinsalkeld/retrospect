package edu.ubc.mirrors.jdi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.BooleanArgument;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.Connector.StringArgument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.tools.jdi.SocketAttachingConnector;
import com.sun.tools.jdi.SunCommandLineLauncher;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
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
    
    public static VirtualMachine commandLineLaunch(String mainAndArgs, String vmArgs) throws VMStartException, IOException {
	VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        SunCommandLineLauncher c = null;
        for (Connector connector : connectors) {
            if (connector instanceof SunCommandLineLauncher) {
                c = (SunCommandLineLauncher)connector;
                break;
            }
        }
        
        Map<String, ? extends Argument> connectorArgs = c.defaultArguments();
        ((StringArgument)connectorArgs.get("main")).setValue(mainAndArgs);
        ((StringArgument)connectorArgs.get("options")).setValue(vmArgs);
        ((BooleanArgument)connectorArgs.get("suspend")).setValue(true);
        try {
	    return c.launch(connectorArgs);
	} catch (IllegalConnectorArgumentsException e) {
	    throw new RuntimeException(e);
	}
    }
    
    public static VirtualMachine connectOnPort(int port) throws IOException, IllegalConnectorArgumentsException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        SocketAttachingConnector c = null;
        for (Connector connector : connectors) {
            if (connector instanceof SocketAttachingConnector) {
                c = (SocketAttachingConnector)connector;
                break;
            }
        }
        
        Map connectorArgs = c.defaultArguments();
        ((IntegerArgument)connectorArgs.get("port")).setValue(port);
        return c.attach(connectorArgs);
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        for (ReferenceType t : jdiVM.classesByName(name)) {
            if (t.classLoader() == null) {
                return makeClassMirror(t.classObject());
            }
        }
        return null;
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : jdiVM.classesByName(name)) {
            classes.add(((ClassMirror)makeMirror(t.classObject())));
        }
        return classes;
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
}
