package edu.ubc.retrospect;

import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.InvocableMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.MirrorInvocationHandlerProvider;

public class AroundClosureNativeStubsProvider implements MirrorInvocationHandlerProvider {

    @Override
    public MirrorInvocationHandler getInvocationHandler(MethodMirror method) {
        if (method.getDeclaringClass().getClassName().equals(MirrorInvocationHandlerAroundClosure.class.getName())) {
            return HANDLER;
        } else {
            return null;
        }
    }
    
    private static final MirrorInvocationHandler HANDLER = new MirrorInvocationHandler() {
        public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
            InstanceMirror closure = (InstanceMirror)args.get(0);
            ObjectArrayMirror closureArgsMirror = (ObjectArrayMirror)args.get(1);
            Object[] closureArgs = Reflection.fromArray(closureArgsMirror);
            try {
                AroundClosureMirror mirror = (AroundClosureMirror)closure.get(closure.getClassMirror().getDeclaredField("handler"));
                return mirror.getHandler().invoke(thread, Arrays.asList(closureArgs));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
