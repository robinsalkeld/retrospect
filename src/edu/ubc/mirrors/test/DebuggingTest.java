package edu.ubc.mirrors.test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import sun.misc.Unsafe;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;

public class DebuggingTest {

    public static class Entry {
        int hash;
        Object key;
        volatile Object value;
        volatile Entry next;
    }
    
    public static void main(String[] args) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> barClass = Class.forName("edu.ubc.mirrors.test.Bar", true, DebuggingTest.class.getClassLoader());
        barClass.getFields();
        barClass.newInstance();
        
        Charset.forName("ISO-8859-1");
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<Connector> connectors = vmm.allConnectors();
        
        Unsafe unsafe = Unsafe.getUnsafe();
        for (String field : Arrays.asList("hash", "key", "value", "next")) {
                System.out.println(field + ": " + unsafe.objectFieldOffset(Entry.class.getDeclaredField(field)));
        }
    }
    
}
