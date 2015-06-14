package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IEventPredicate;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.utils.BehaviorEventPredicate;
import edu.ubc.mirrors.ClassMirror;

public abstract class TODBehaviorEventRequest extends TODMirrorEventRequest {

    protected IBehaviorInfo behaviorInfo;
    private List<Pattern> classNameFilters = new ArrayList<Pattern>();
    private List<ITypeInfo> classFilters = new ArrayList<ITypeInfo>();
    
    public TODBehaviorEventRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }
    
    protected abstract boolean matchConstructors();
    
    protected abstract boolean matchExits();
    
    @Override
    protected IEventBrowser createEventBrowser() {
        IEventFilter filter;
        if (matchExits()) {
            if (behaviorInfo != null) {
                filter = vm.getLogBrowser().createBehaviorExitFilter(behaviorInfo);
            } else {
                filter = vm.getLogBrowser().createBehaviorExitFilter();
            }
        } else {
            if (behaviorInfo != null) {
                filter = vm.getLogBrowser().createBehaviorCallFilter(behaviorInfo);
            } else {
                filter = vm.getLogBrowser().createBehaviorCallFilter();
            }
        }
        IEventPredicate predicate = new BehaviorEventPredicate(behaviorInfo, matchConstructors(), matchExits(), 
                classNameFilters, classFilters);
        filter = vm.getLogBrowser().createPredicateFilter(predicate, filter);
        return vm.getLogBrowser().createBrowser(filter);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        // TODO-RS: Need to sort out precisely the mirrors API w.r.t. patterns.
        classNameFilters.add(Pattern.compile(classNamePattern));
    }
    
    public void addClassFilter(ClassMirror klass) {
        classFilters.add(((TODClassMirror)klass).classInfo);
    }
    
    @Override
    public String toString() {
        return super.toString() + " (" + behaviorInfo + ", " + classFilters + ", " + classNameFilters + ")";
    }
}
