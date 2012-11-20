package edu.ubc.mirrors;

import java.util.List;

public interface ThreadMirror extends InstanceMirror {

    /**
     * @return Mirror on an object of type java.lang.StackTraceElement[]
     */
    List<FrameMirror> getStackTrace();
    
}
