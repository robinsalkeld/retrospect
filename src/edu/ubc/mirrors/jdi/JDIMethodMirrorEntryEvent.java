package edu.ubc.mirrors.jdi;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;

public class JDIMethodMirrorEntryEvent extends JDIMirrorEvent implements MethodMirrorEntryEvent {

    private final MethodMirror method;
    
    public JDIMethodMirrorEntryEvent(MethodMirror method) {
	this.method = method;
    }

    @Override
    public MethodMirror method() {
        return method;
    }
}
