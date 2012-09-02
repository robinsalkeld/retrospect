package edu.ubc.mirrors.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
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

public class DebuggingTest {

    public static void main(String[] args) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, IllegalConnectorArgumentsException, InterruptedException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        for (Connector connector : connectors) {
            if (connector instanceof SocketAttachingConnector) {
                SocketAttachingConnector c = (SocketAttachingConnector)connector;
                Map connectorArgs = c.defaultArguments();
                ((IntegerArgument)connectorArgs.get("port")).setValue(8998);
                VirtualMachine vm = c.attach(connectorArgs);
                EventRequestManager m = vm.eventRequestManager();
                MethodEntryRequest r = m.createMethodEntryRequest();
                r.enable();
                vm.resume();
                EventQueue q = vm.eventQueue();
                EventSet s;
                while ((s = q.remove()) != null) {
                    for (Object e : s) {
                        if (e instanceof MethodEntryEvent) {
                            MethodEntryEvent mer = (MethodEntryEvent)e;
                            System.out.println(mer.method());
                            mer.request();
                        }
                    }
                    s.resume();
                }
            }
        }
    }
    
}
