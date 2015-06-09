package edu.ubc.mirrors;

import java.util.List;

public interface MethodMirrorHandlerRequest extends MirrorEventRequest {

    public void setMethodFilter(String declaringClass, String name, List<String> paramterTypeNames);
}
