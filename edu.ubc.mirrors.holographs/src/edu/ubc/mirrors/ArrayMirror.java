package edu.ubc.mirrors;

public interface ArrayMirror extends ObjectMirror {
    public int length();
    
    // TODO-RS: This has to be defined because of the hologram bytecode
    // translation strategy of using these array interfaces directly instead
    // of defining a parallel set separate from the public API.
    // To be reconsidered in the future.
    public Object clone() throws CloneNotSupportedException;
}
