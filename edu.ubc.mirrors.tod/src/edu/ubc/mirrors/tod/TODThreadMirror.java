package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class TODThreadMirror extends TODInstanceMirror implements ThreadMirror {

    final IThreadInfo threadInfo;

    public TODThreadMirror(TODVirtualMachineMirror vm, IThreadInfo threadInfo) {
        super(vm, vm.getLogBrowser().createObjectInspector(threadInfo.getObjectId()));
        
        this.threadInfo = threadInfo;
    }
    
    public TODThreadMirror(TODVirtualMachineMirror vm, IObjectInspector inspector) {
        super(vm, inspector);
        
        this.threadInfo = vm.threadInfosByObjectId.get(inspector.getObject());
    }

    @Override
    public List<FrameMirror> getStackTrace() {
        // Find the most recent event on this thread call this thread is in
        IEventFilter filter = vm.getLogBrowser().createThreadFilter(threadInfo);
        IEventBrowser browser = vm.getLogBrowser().createBrowser(filter);
        browser.setPreviousTimestamp(vm.currentTimestamp());
        ILogEvent event = browser.previous();
        if (event == null) {
            // Should indicate a not-yet started thread or a zombie
            return null;
        }
        
        List<FrameMirror> result = new ArrayList<FrameMirror>(event.getDepth());
        while (event != null) {
            result.add(vm.makeFrameMirror(event));
            event = event.getParent();
        }
        
        Collections.reverse(result);
        return result;
    }

    @Override
    public InstanceMirror getContendedMonitor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InstanceMirror> getOwnedMonitors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interrupt() {
        throw new UnsupportedOperationException();
    }
}
