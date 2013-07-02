package edu.ubc.mirrors;

import java.util.List;

public interface ThreadMirror extends InstanceMirror {

    List<FrameMirror> getStackTrace();

    void interrupt();
}
