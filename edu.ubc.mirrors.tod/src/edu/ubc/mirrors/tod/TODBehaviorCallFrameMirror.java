package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;

public class TODBehaviorCallFrameMirror implements FrameMirror {

    private final TODVirtualMachineMirror vm;
    private final IBehaviorCallEvent event;
    
    public TODBehaviorCallFrameMirror(TODVirtualMachineMirror vm, IBehaviorCallEvent event) {
        this.vm = vm;
        this.event = event;
    }

    @Override
    public ClassMirror declaringClass() {
        return vm.makeClassMirror(event.getCallingBehavior().getDeclaringType());
    }

    @Override
    public String methodName() {
        return event.getCallingBehavior().getName();
    }

    @Override
    public MethodMirror method() {
        if (methodName().equals("<init>")) {
            return null;
        } else {
            return vm.makeMethodMirror(event.getCallingBehavior());
        }
    }

    @Override
    public ConstructorMirror constructor() {
        if (methodName().equals("<init>")) {
            return vm.makeConstructorMirror(event.getCallingBehavior());
        } else {
            return null;
        }
    }
    
    @Override
    public String fileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int lineNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public InstanceMirror thisObject() {
        return (InstanceMirror)vm.makeMirror(event.getTarget());
    }

    @Override
    public List<Object> arguments() {
        List<Object> args = new ArrayList<Object>();
        for (Object arg : event.getArguments()) {
            args.add(vm.makeMirror(arg));
        }
        return args;
    }

}
