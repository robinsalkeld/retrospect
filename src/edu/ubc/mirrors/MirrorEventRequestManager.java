package edu.ubc.mirrors;

import java.util.List;

public interface MirrorEventRequestManager {

    MethodMirrorEntryRequest createMethodMirrorEntryRequest();
    
    List<MethodMirrorEntryRequest> methodMirrorEntryRequests();
    
    MethodMirrorExitRequest createMethodMirrorExitRequest();
    
    List<MethodMirrorExitRequest> methodMirrorExitRequests();
    
    ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest();
    
    List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests();
    
    ConstructorMirrorExitRequest createConstructorMirrorExitRequest();
    
    List<ConstructorMirrorExitRequest> constructorMirrorExitRequests();
    
    FieldMirrorSetRequest createFieldMirrorSetRequest(ClassMirror klass, String fieldName);
    
    List<FieldMirrorSetRequest> fieldMirrorSetRequests();
    
    ClassMirrorPrepareRequest createClassMirrorPrepareRequest();
    
    List<ClassMirrorPrepareRequest> classMirrorPrepareRequests();
    
    void deleteMirrorEventRequest(MirrorEventRequest request);

}
