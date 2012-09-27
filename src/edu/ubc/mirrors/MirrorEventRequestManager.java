package edu.ubc.mirrors;

import java.util.List;

public interface MirrorEventRequestManager {

    MethodMirrorEntryRequest createMethodMirrorEntryRequest(MethodMirror method);
    
    List<MethodMirrorEntryRequest> methodMirrorEntryRequests();
    
    void deleteMethodMirrorEntryRequest(MethodMirrorEntryRequest request);
    
}
