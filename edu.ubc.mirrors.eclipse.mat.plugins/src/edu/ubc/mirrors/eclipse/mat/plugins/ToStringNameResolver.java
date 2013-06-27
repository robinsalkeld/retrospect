package edu.ubc.mirrors.eclipse.mat.plugins;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.extension.Subject;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.VoidProgressListener;

import edu.ubc.mirrors.mirages.Reflection;

@Subject("java.lang.Object")
public class ToStringNameResolver implements IClassSpecificNameResolver {

    @Override
    public String resolve(IObject object) throws SnapshotException {
        try {
            return HolographVMRegistry.toString(HolographVMRegistry.getMirror(object, new VoidProgressListener()));
        } catch (Throwable t) {
            System.out.println("Error on object #" + object.getObjectId());
            t.printStackTrace();
//            System.exit(-1);
//            // Never reached
            return null;
        }
    }

    
    
}
