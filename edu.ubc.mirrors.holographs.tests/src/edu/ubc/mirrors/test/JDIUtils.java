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
package edu.ubc.mirrors.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.Bootstrap;

import com.sun.jdi.VMDisconnectedException;
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
    public static VirtualMachine commandLineLaunch(String mainAndArgs, String vmArgs, boolean suspend, OutputStream out, OutputStream err) throws VMStartException, IOException {
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
        String vmexec = ((StringArgument)connectorArgs.get("vmexec")).value();
        
        System.out.println("Launching via JDI: " + vmexec + " " + vmArgs + "\n" + mainAndArgs);
        
        try {
            final VirtualMachine vm = c.launch(connectorArgs);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        vm.exit(0);
                    } catch (VMDisconnectedException e) {
                        // Ignore
                    }
                }
            });
            return fixTimeoutAndHandleStreams(vm, out, err);
        } catch (IllegalConnectorArgumentsException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static VirtualMachine connectOnPort(int port, OutputStream out, OutputStream err) throws IOException, IllegalConnectorArgumentsException {
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
        return fixTimeoutAndHandleStreams(c.attach(connectorArgs), out, err);
    }
    
    private static VirtualMachine fixTimeoutAndHandleStreams(VirtualMachine vm, OutputStream out, OutputStream err) {
        ((org.eclipse.jdi.VirtualMachine)vm).setRequestTimeout(6000000);
        ProcessUtils.handleStreams(vm.process(), out, err);
        return vm;
    }
}
