package edu.ubc.mirrors.tod;

import tod.core.database.browser.IEventBrowser;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;

public class TODClassMirrorPrepareRequest extends TODMirrorEventRequest implements ClassMirrorPrepareRequest {

    public TODClassMirrorPrepareRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
    }

    @Override
    protected IEventBrowser createEventBrowser() {
        // TODO-RS: No such TOD event, so for now this is a no-op.
        // We should be able to manufacture them (or add them to TOD) since 
        return vm.getLogBrowser().createBrowser(vm.getLogBrowser().createEventFilter(null));
    }
}
