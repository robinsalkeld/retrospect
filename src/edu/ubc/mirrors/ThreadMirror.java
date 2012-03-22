package edu.ubc.mirrors;

public interface ThreadMirror extends InstanceMirror {

    /**
     * @return Mirror on an object of type java.lang.StackTraceElement[]
     */
    ObjectArrayMirror getStackTrace();
    
}
