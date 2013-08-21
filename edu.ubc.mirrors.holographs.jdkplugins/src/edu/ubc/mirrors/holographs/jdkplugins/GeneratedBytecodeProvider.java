package edu.ubc.mirrors.holographs.jdkplugins;

import java.lang.ref.Reference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.ProxyGenerator;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassMirrorBytecodeProvider;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class GeneratedBytecodeProvider implements ClassMirrorBytecodeProvider {

    private static final Map<ClassMirror, List<String>> proxyClassInterfaces =
                 new HashMap<ClassMirror, List<String>>();
    
    private List<String> getInterfacesForProxyClass(ClassMirror klass) throws IllegalAccessException {
        List<String> result = proxyClassInterfaces.get(klass);
        if (result != null) {
            return result;
        }
        
        ClassMirror proxyClass = klass.getVM().findBootstrapClassMirror(Proxy.class.getName());
        
        // Unfortunately we can't just call ClassMirror.getInterfaceMirrors(), since that
        // might depend on the very bytecode we're trying to find!
        // The list of interfaces is also not stored in any other way on the proxy class.
        // Luckily Proxy has a cache that uses the interface names as keys.
        InstanceMirror /* Map */ loaderToCache = (InstanceMirror)proxyClass.getStaticFieldValues().get(proxyClass.getDeclaredField("loaderToCache"));
        for (Map.Entry<ObjectMirror, ObjectMirror> loaderEntry : Reflection.mapEntries(loaderToCache).entrySet()) {
            InstanceMirror cache = (InstanceMirror)loaderEntry.getValue();
            for (Map.Entry<ObjectMirror, ObjectMirror> entry : Reflection.mapEntries(cache).entrySet()) {
                List<ObjectMirror> interfaceNameMirrors = Reflection.collectionValues(entry.getKey());
                List<String> interfaceNames = new ArrayList<String>();
                for (ObjectMirror m : interfaceNameMirrors) {
                    interfaceNames.add(Reflection.getRealStringForMirror((InstanceMirror)m));
                }
                InstanceMirror ref = (InstanceMirror)entry.getValue();
                ClassMirror cachedClass = (ClassMirror)Reflection.invokeMethodHandle(ref,
                        new MethodHandle() {
                            protected void methodCall() throws Throwable {
                                ((Reference<?>)null).get();
                            }
                        });
                proxyClassInterfaces.put(cachedClass, interfaceNames);
            }
        }
            
        return proxyClassInterfaces.get(klass);
    }
    
    @Override
    public byte[] getBytecode(ClassMirror classMirror) {
        VirtualMachineMirror vm = classMirror.getVM();
        ClassMirror proxyClass = vm.findBootstrapClassMirror(Proxy.class.getName());
        if (proxyClass.equals(classMirror.getSuperClassMirror())) {
            try {
                // Just reuse the same class that would have generated the bytecode
                ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
                List<String> interfaceNames = getInterfacesForProxyClass(classMirror);
                ObjectArrayMirror interfacesArrayMirror = (ObjectArrayMirror)classClass.newArray(interfaceNames.size());
                int index = 0;
                for (String interfaceName : interfaceNames) {
                    interfacesArrayMirror.set(index++, Reflection.classMirrorForName(vm, 
                            ThreadHolograph.currentThreadMirror(), interfaceName, false, classMirror.getLoader()));
                }
                
                // Could cache this per VM, but likely not worth it.
                ClassMirror generator = vm.findBootstrapClassMirror(ProxyGenerator.class.getName());
                MethodMirror generateMethod = generator.getDeclaredMethod("generateProxyClass", 
                            vm.findBootstrapClassMirror(String.class.getName()),
                            vm.getArrayClass(1, classClass));
                
                ByteArrayMirror bytesArray;
                bytesArray = (ByteArrayMirror)generateMethod.invoke(ThreadHolograph.currentThreadMirror(), null,
                        Reflection.makeString(vm, classMirror.getClassName()), interfacesArrayMirror);
                byte[] result = new byte[bytesArray.length()];
                ByteArrayMirror nativeBytes = new NativeByteArrayMirror(result);
                Reflection.arraycopy(bytesArray, 0, nativeBytes, 0, result.length);
                return result;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (MirrorInvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}
