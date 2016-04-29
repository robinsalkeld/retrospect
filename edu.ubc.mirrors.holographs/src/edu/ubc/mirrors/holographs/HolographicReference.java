package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ObjectMirror;


/** A canary object to react to when a holographic object is no longer reachable
 * through holographic references. When this happens, we check if the object is
 * reachable within the wrapped VM, and if not the object is fully, holographically
 * collectable.
 */ 
class HolographicReference {
    
    private final ObjectMirror referent;
    
    public HolographicReference(ObjectMirror referent) {
        this.referent = referent;
    }
    
    public ObjectMirror getReferent() {
        return referent;
    }
    
    @Override
    protected void finalize() throws Throwable {
        referent.allowCollection(true);
    }
}