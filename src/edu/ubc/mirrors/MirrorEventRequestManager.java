package edu.ubc.mirrors;

import java.util.List;

public interface MirrorEventRequestManager {

    MethodMirrorEntryRequest createMethodMirrorEntryRequest();
    
    List<MethodMirrorEntryRequest> methodMirrorEntryRequests();
    
    void deleteMethodMirrorEntryRequest(MethodMirrorEntryRequest request);
    
}
