package edu.ubc.mirrors.tod;

import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.ClassMirror;
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
        return vm.makeMethodMirror(event.getCallingBehavior());
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> arguments() {
        // TODO Auto-generated method stub
        return null;
    }

}
