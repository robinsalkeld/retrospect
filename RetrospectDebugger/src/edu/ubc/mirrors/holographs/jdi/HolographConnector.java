package edu.ubc.mirrors.holographs.jdi;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdi.Bootstrap;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.Transport;

public class HolographConnector implements AttachingConnector {

    private final AttachingConnector wrappedConnector;

    public HolographConnector(AttachingConnector wrappedConnector) {
        this.wrappedConnector = wrappedConnector;
    }
    
    public HolographConnector() { 
        AttachingConnector connector= null;
        for (Object ac : Bootstrap.virtualMachineManager().attachingConnectors()) {
                AttachingConnector lc= (AttachingConnector) ac;
                if (lc.name().equals("com.sun.jdi.SocketAttach")) { //$NON-NLS-1$
                        connector= lc;
                        break;
                }
        }
        if (connector == null) {
                throw new RuntimeException();
        }
        this.wrappedConnector = connector;
    }
    
    /**
     * @param arg0
     * @return
     * @throws IOException
     * @throws IllegalConnectorArgumentsException
     * @see com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)
     */
    public VirtualMachine attach(Map arg0)
            throws IOException, IllegalConnectorArgumentsException {
        System.out.println("Attaching to holograph VM...");
        return new JDIHolographVirtualMachine(wrappedConnector.attach(arg0));
    }

    /**
     * @return
     * @see com.sun.jdi.connect.Connector#defaultArguments()
     */
    public Map defaultArguments() {
        return wrappedConnector.defaultArguments();
    }

    /**
     * @return
     * @see com.sun.jdi.connect.Connector#description()
     */
    public String description() {
        return wrappedConnector.description();
    }

    /**
     * @return
     * @see com.sun.jdi.connect.Connector#name()
     */
    public String name() {
        return wrappedConnector.name() + "Holograph";
    }

    /**
     * @return
     * @see com.sun.jdi.connect.Connector#transport()
     */
    public Transport transport() {
        return wrappedConnector.transport();
    }
    
}
