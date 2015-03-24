package edu.ubc.mirrors;

import java.util.List;

public class MethodMirrorInvoker implements MirrorInvocationHandler {

    private final MethodMirror method;

    public MethodMirrorInvoker(MethodMirror method) {
        this.method = method;
    }

    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        InstanceMirror obj = (InstanceMirror)args.get(0);
        try {
            return method.invoke(thread, obj, args.subList(1, args.size()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
