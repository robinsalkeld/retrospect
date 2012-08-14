package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.net.URL;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class PackageStubs {

    public static Mirage getSystemPackage(Class<?> classLoaderLiteral, Mirage name) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        URL url = vm.getBootstrapBytecodeLoader().getResource(realName.replace('.', '/') + ".package_info.java");
        if (url != null && url.getProtocol().equals("file")) {
            return ObjectMirage.make(Reflection.makeString(vm, url.getPath()));
        } else {
            return null;
        }
    }
    
}
