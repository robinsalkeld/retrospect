package edu.ubc.mirrors.jdi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.tools.jdi.SocketAttachingConnector;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.raw.PrimitiveClassMirror;

public class JDIVirtualMachineMirror implements VirtualMachineMirror {

    protected final VirtualMachine jdiVM;

    private final Map<Mirror, ObjectMirror> mirrors = new HashMap<Mirror, ObjectMirror>();
    
    public JDIVirtualMachineMirror(VirtualMachine jdiVM) {
        this.jdiVM = jdiVM;
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
    
    public List<ClassMirror> makeClassMirrorList(List<? extends ReferenceType> refTypes) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(refTypes.size());
        for (ReferenceType refType : refTypes) {
            result.add(makeClassMirror(refType.classObject()));
        }
        return result;
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return new PrimitiveClassMirror(this, name);
    }

    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
	return new JDIMirrorEventRequestManager(this, jdiVM.eventRequestManager());
    }
    
}
