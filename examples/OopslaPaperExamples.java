package examples;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

public class OopslaPaperExamples {

public List<ServiceProperty> getProperties(IObject propertiesObject) {
    String[] keys = null;
    String[] values = null;
    
    IObjectArray keysArray = 
            (IObjectArray) propertiesObject.resolveValue("headers");
    if (keysArray != null) {
        long[] keyAddresses = keysArray.getReferenceArray();
        if (keyAddresses != null) {
            keys = getServiceProperties(keyAddresses);
        }
    }
    
    IObjectArray valuesArray = /* similar */;
    
    if (keys == null || values == null)
        return null;
    List<ServiceProperty> properties = 
        new ArrayList<ServiceProperty>(keys.length);
    for (int i = 0; i < keys.length; i++) {
        properties.add(new ServiceProperty(keys[i], values[i]));
    }
    return properties;
}


    }
    
}
