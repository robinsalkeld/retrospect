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
    public static VirtualMachine commandLineLaunch(String mainAndArgs, String vmArgs, boolean suspend, boolean echoStreams) throws VMStartException, IOException {
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
            return fixTimeoutAndHandleStreams(vm, echoStreams);
        } catch (IllegalConnectorArgumentsException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static VirtualMachine connectOnPort(int port, boolean echoStreams) throws IOException, IllegalConnectorArgumentsException {
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
        return fixTimeoutAndHandleStreams(c.attach(connectorArgs), echoStreams);
    }
    
    private static VirtualMachine fixTimeoutAndHandleStreams(VirtualMachine vm, boolean echoStreams) {
        ((org.eclipse.jdi.VirtualMachine)vm).setRequestTimeout(6000000);
        if (echoStreams) {
            Process process = vm.process();
            new StreamSiphon(process.getInputStream(), System.out).start();
            new StreamSiphon(process.getErrorStream(), System.err).start();
        }
        
        return vm;
    }
    
    private static class StreamSiphon extends Thread {
        
        public StreamSiphon(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        private final InputStream in;
        private final OutputStream out;
        
        @Override
        public void run() {
            byte[] buffer = new byte[10];
            int read;
            try {
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
}
