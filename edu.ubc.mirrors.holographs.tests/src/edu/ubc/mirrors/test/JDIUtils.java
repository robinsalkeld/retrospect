package edu.ubc.mirrors.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.Bootstrap;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.BooleanArgument;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.Connector.StringArgument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;

public class JDIUtils {
    public static VirtualMachine commandLineLaunch(String mainAndArgs, String vmArgs, boolean suspend) throws VMStartException, IOException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        LaunchingConnector c = null;
        for (Connector connector : connectors) {
            if (connector instanceof LaunchingConnector) {
                c = (LaunchingConnector)connector;
                break;
            }
        }
        
        Map<String, ? extends Argument> connectorArgs = c.defaultArguments();
        ((StringArgument)connectorArgs.get("main")).setValue(mainAndArgs);
        ((StringArgument)connectorArgs.get("options")).setValue(vmArgs);
        ((BooleanArgument)connectorArgs.get("suspend")).setValue(suspend);
        try {
            return c.launch(connectorArgs);
        } catch (IllegalConnectorArgumentsException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static VirtualMachine connectOnPort(int port) throws IOException, IllegalConnectorArgumentsException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        AttachingConnector c = null;
        for (Connector connector : connectors) {
            if (connector instanceof AttachingConnector) {
                c = (AttachingConnector)connector;
                break;
            }
        }
        
        Map<String, Argument> connectorArgs = c.defaultArguments();
        ((IntegerArgument)connectorArgs.get("port")).setValue(port);
        return c.attach(connectorArgs);
    }
}
