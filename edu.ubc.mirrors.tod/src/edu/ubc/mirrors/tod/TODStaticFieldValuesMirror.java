package edu.ubc.mirrors.tod;

import tod.core.database.structure.IClassInfo;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;

public class TODStaticFieldValuesMirror extends TODInstanceMirror implements StaticFieldValuesMirror {

    private final ClassMirror forClassMirror;
    
    public TODStaticFieldValuesMirror(TODVirtualMachineMirror vm, IClassInfo klass) {
        super(vm, vm.getLogBrowser().createClassInspector(klass));
        
        this.forClassMirror = vm.makeClassMirror(klass);
    }
    
    @Override
    public ClassMirror forClassMirror() {
        return forClassMirror;
    }

}
