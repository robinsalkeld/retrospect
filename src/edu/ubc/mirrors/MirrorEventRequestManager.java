package edu.ubc.mirrors;

import java.util.List;

public interface MirrorEventRequestManager {

    MethodMirrorEntryRequest createMethodMirrorEntryRequest();
    
    List<MethodMirrorEntryRequest> methodMirrorEntryRequests();
    
    void deleteMethodMirrorEntryRequest(MethodMirrorEntryRequest request);
    
    MethodMirrorExitRequest createMethodMirrorExitRequest();
    
    List<MethodMirrorExitRequest> methodMirrorExitRequests();
    
    void deleteMethodMirrorExitRequest(MethodMirrorExitRequest request);
    
    FieldMirrorSetRequest createFieldMirrorSetRequest(ClassMirror klass, String fieldName);
    
    List<FieldMirrorSetRequest> fieldMirrorSetRequests();
    
    void deleteFieldMirrorSetRequest(FieldMirrorSetRequest request);
    
    ClassMirrorPrepareRequest createClassMirrorPrepareRequest();
    
    List<ClassMirrorPrepareRequest> classMirrorPrepareRequests();
    
    void deleteClassMirrorPrepareRequest(ClassMirrorPrepareRequest request);
    
}
