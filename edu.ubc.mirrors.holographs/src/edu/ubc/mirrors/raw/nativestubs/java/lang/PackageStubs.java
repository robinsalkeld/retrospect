package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.net.URL;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;

public class PackageStubs extends NativeStubs {

    public PackageStubs(ClassHolograph klass) {
	super(klass);
    }

    public InstanceMirror getSystemPackage(InstanceMirror name) {
        VirtualMachineHolograph vm = getVM();
        
        String realName = Reflection.getRealStringForMirror(name);
        URL url = vm.getBootstrapBytecodeLoader().getResource(realName.replace('.', '/') + ".package_info.java");
        if (url != null && url.getProtocol().equals("file")) {
            return Reflection.makeString(vm, url.getPath());
        } else {
            return null;
        }
    }
    
}
