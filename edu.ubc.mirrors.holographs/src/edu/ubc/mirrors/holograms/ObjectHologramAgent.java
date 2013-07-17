package edu.ubc.mirrors.holograms;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;

import edu.ubc.mirrors.holograms.MainEntryAdaptor;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.ObjectHologramAgent;

public class ObjectHologramAgent implements ClassFileTransformer {

    public static void premain(String options, Instrumentation instr) {
        for (Class<?> c : instr.getAllLoadedClasses()) {
            System.out.println(c.getName());
        }
        instr.addTransformer(new ObjectHologramAgent());
    }

    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        
        try {
            String binaryName = className.replace('/', '.');
//            System.out.println(binaryName);
            if (binaryName.equals(HologramClassLoader.class.getName())) return null;
//            Thread.dumpStack();
            // TODO: Enable once I work out how to ensure classes are resolved in the same order
//            ObjectHologram.defineHologramClass(binaryName, loader, new ClassReader(classfileBuffer));
            
            if (!(loader instanceof HologramClassLoader)) {
                return MainEntryAdaptor.generate(binaryName, new ClassReader(classfileBuffer), null);
            } else {
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(-1);
            return null;
        }
    }
}