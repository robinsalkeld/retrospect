package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;

/**
 * TODO: Probably can't actually throw these with the right semantics...
 * @author robinsalkeld
 */
public class ThrowableMirage extends Throwable implements Mirage {

    public ObjectMirror mirror;
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    protected ThrowableMirage(Object mirror) {
        this.mirror = (ObjectMirror)mirror;
    }
    
    public ThrowableMirage(ObjectMirage message, InstanceMirror mirror) {
        super(ObjectMirage.getRealStringForMirage(message));
        this.mirror = mirror;
    }
    
    @Override
    public ObjectMirror getMirror() {
        return mirror;
    }
    
    @Override
    public String toString() {
        String s = mirror.getClassMirror().getClassName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
    
    public StackTraceElement cleanStackTraceElement(StackTraceElement e) {
        return new StackTraceElement(getOriginalBinaryClassName(e.getClassName()), e.getMethodName(), e.getFileName(), e.getLineNumber());
    }
}
