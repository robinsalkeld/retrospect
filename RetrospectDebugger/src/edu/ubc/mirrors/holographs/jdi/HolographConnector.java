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
