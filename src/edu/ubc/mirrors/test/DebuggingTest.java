package edu.ubc.mirrors.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.tools.jdi.SocketAttachingConnector;

import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class DebuggingTest {

    public static void main(String[] args) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, IllegalConnectorArgumentsException, InterruptedException {
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
        ((IntegerArgument)connectorArgs.get("port")).setValue(8998);
        VirtualMachine vm = c.attach(connectorArgs);
        
        VirtualMachineMirror vmMirror = new JDIVirtualMachineMirror(vm);
        VirtualMachineHolograph vmHolograph = new VirtualMachineHolograph(vmMirror,
                Reflection.getBootstrapPath(),
                Reflection.getStandardMappedFiles());
    }
    
}
