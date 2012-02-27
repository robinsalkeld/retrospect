package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;

/**
 * TODO: Probably can't actually throw these with the right semantics...
 * @author robinsalkeld
 */
public class ThrowableMirage extends Throwable implements Mirage {

    public ObjectMirror mirror;
    
    /**
     * Constructor for translated new statements - mirror will be created
     * by the first constructor called.
     */
    protected ThrowableMirage() {
    }
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    public ThrowableMirage(ObjectMirror mirror) {
        this.mirror = mirror;
    }
    
    @Override
    public ObjectMirror getMirror() {
        return mirror;
    }
}
