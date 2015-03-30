package edu.ubc.mirrors.holograms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;

public class MethodHologram implements MirrorInvocationHandler {

    private final VirtualMachineMirror vm;
    private final Method hologramMethod;
    
    private MethodHologram(VirtualMachineMirror vm, Method hologramMethod) {
        this.vm = vm;
        this.hologramMethod = hologramMethod;
        this.hologramMethod.setAccessible(true);
    }
    
    public static MethodHologram make(ClassHolograph klass, String name, List<ClassMirror> paramTypes) {
        Class<?>[] hologramParamTypes = new Class<?>[paramTypes.size()];
        for (int i = 0; i < hologramParamTypes.length; i++) {
            hologramParamTypes[i] = ClassHolograph.getHologramClass(paramTypes.get(i), false);
        }
        Class<?> hologramClass = klass.getHologramClass(true);
        // Account for the fact that ObjectHologram is not actually the top of the type lattice
        if (klass.getClassName().equals(Object.class.getName())) {
            hologramClass = Object.class;
        }
        try {
            return new MethodHologram(klass.getVM(), hologramClass.getDeclaredMethod(name, hologramParamTypes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        Object hologramObj = null;
        if ((hologramMethod.getModifiers() & Modifier.STATIC) == 0) {
            hologramObj = ClassHolograph.makeHologram(args.get(0));
            args = args.subList(1, args.size());
        }
        
        Object[] hologramArgs = new Object[args.size()];
        for (int i = 0; i < hologramArgs.length; i++) {
            hologramArgs[i] = ClassHolograph.makeHologram(args.get(i));
        }
        try {
            Object result = hologramMethod.invoke(hologramObj, hologramArgs);
            // Account for the fact that toString() has to return a real String here
            if (result instanceof String) {
                return vm.makeString((String)result);
            } else {
                return ClassHolograph.unwrapHologram(result);
            }
        
        } catch (InvocationTargetException e) {
            throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
        } catch (IllegalAccessException e) {
            // Shouldn't happen
            throw new RuntimeException(e);
        }
    }

}
