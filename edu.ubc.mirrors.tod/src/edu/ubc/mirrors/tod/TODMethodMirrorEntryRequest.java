package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IEventPredicate;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ITypeInfo;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class TODMethodMirrorEntryRequest extends TODMirrorEventRequest implements MethodMirrorEntryRequest {

    private IBehaviorInfo behaviorInfo;
    private List<String> classNameFilters = new ArrayList<String>();
    private List<ITypeInfo> classFilters = new ArrayList<ITypeInfo>();
    
    public TODMethodMirrorEntryRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }
    
    private boolean eventMatchesFilters(ILogEvent event) {
        IBehaviorCallEvent callEvent = (IBehaviorCallEvent)event;
        IClassInfo declaringType = callEvent.getCalledBehavior().getDeclaringType();
        String className = declaringType.getName();
        for (String classNameFilter : classNameFilters) {
            // TODO-RS: See comment in PointcutMirrorRequestExtractor
            // Need to sort out precisely the mirrors API w.r.t. patterns.
            if (!classNameFilter.equals(className)) {
                return false;
            }
        }
        for (ITypeInfo classFilter : classFilters) {
            if (!classFilter.equals(declaringType)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected IEventBrowser createEventBrowser() {
        IEventFilter filter;
        if (behaviorInfo != null) {
            filter = vm.getLogBrowser().createBehaviorCallFilter(behaviorInfo);
        } else {
            filter = vm.getLogBrowser().createBehaviorCallFilter();
            IEventPredicate predicate = new IEventPredicate() {
                @Override
                public boolean match(ILogEvent aEvent) {
                    return eventMatchesFilters(aEvent);
                }
            };
            filter = vm.getLogBrowser().createPredicateFilter(predicate, filter);
        }
        return vm.getLogBrowser().createBrowser(filter);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        classNameFilters.add(classNamePattern);
        
    }
    
    @Override
    public void addClassFilter(ClassMirror klass) {
        classFilters.add(((TODClassMirror)klass).classInfo);
        
    }

    @Override
    public void setMethodFilter(MethodMirror method) {
        this.behaviorInfo = ((TODMethodOrConstructorMirror)method).behaviourInfo;
    }
}
