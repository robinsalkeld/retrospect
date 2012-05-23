package edu.ubc.mirrors.test;

import java.util.List;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;

public class DebuggingTest {

    public static void main(String[] args) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
    }
    
}
