package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
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

    private ILogEvent getCurrentCallEvent() {
        // Find the most recent call event on this thread at the right depth
        ILogBrowser logBrowser = vm.getLogBrowser();
        IEventFilter threadFilter = logBrowser.createThreadFilter(threadInfo);
        IEventFilter callFilter = logBrowser.createBehaviorCallFilter();
        IEventFilter exitFilter = logBrowser.createBehaviorExitFilter();
        IEventFilter callOrExitFilter = logBrowser.createUnionFilter(callFilter, exitFilter);
        IEventFilter filter = logBrowser.createIntersectionFilter(threadFilter, callOrExitFilter);
        IEventBrowser browser = logBrowser.createBrowser(filter);
        browser.setPreviousTimestamp(vm.currentTimestamp());
        ILogEvent event = browser.previous();
        if (event == null) {
            // Should indicate a not-yet started thread
            return null;
        }
        
        if (event instanceof IBehaviorExitEvent && event.getTimestamp() == vm.currentTimestamp()) {
            return event.getParent();
        }
        
        int depth = 1;
        for (;;) {
            if (event instanceof IBehaviorCallEvent) {
                depth--;
            } else if (event instanceof IBehaviorExitEvent) {
                depth++;
            }

            if (depth == 0) {
                break;
            }
            
            event = browser.previous();
        }
        return event;
    }
    
    @Override
    public List<FrameMirror> getStackTrace() {
        ILogEvent event = getCurrentCallEvent();
        
        List<FrameMirror> result = new ArrayList<FrameMirror>(event.getDepth());
        while (event != null) {
            result.add(vm.makeFrameMirror(event));
            event = event.getParent();
        }
        
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
