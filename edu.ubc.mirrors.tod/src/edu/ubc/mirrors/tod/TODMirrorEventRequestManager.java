package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import tod.core.database.event.ILogEvent;
import edu.ubc.mirrors.AdviceMirrorHandlerRequest;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorGetRequest;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;
import edu.ubc.mirrors.VMMirrorDeathRequest;

public class TODMirrorEventRequestManager implements MirrorEventRequestManager {

    private final TODVirtualMachineMirror vm;
    
    /*
     * Only those requests that have more events.
     * Invariant: request.hasNext() for every request in this queue.
     */
    private final PriorityQueue<TODMirrorEventRequest> liveRequests = new PriorityQueue<TODMirrorEventRequest>();
    
    /*
     * All requests.
     */
    private final Set<TODMirrorEventRequest> requests = new HashSet<TODMirrorEventRequest>();
    private ILogEvent currentLogEvent = null;
    
    public TODMirrorEventRequestManager(TODVirtualMachineMirror vm) {
        this.vm = vm;
        
        currentLogEvent = vm.getLogBrowser().createBrowser().next();
    }
    
    @Override
    public MirrorLocationRequest createLocationRequest(MirrorLocation location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MirrorLocationRequest> locationRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
        TODMethodMirrorEntryRequest result = new TODMethodMirrorEntryRequest(vm);
        requests.add(result);
        return result;
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
        return allRequestsOfType(MethodMirrorEntryRequest.class);
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
        TODMethodMirrorExitRequest result = new TODMethodMirrorExitRequest(vm);
        requests.add(result);
        return result;
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
        return allRequestsOfType(MethodMirrorExitRequest.class);
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
        TODConstructorMirrorEntryRequest result = new TODConstructorMirrorEntryRequest(vm);
        requests.add(result);
        return result;
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
        return allRequestsOfType(ConstructorMirrorEntryRequest.class);
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
        TODConstructorMirrorExitRequest result = new TODConstructorMirrorExitRequest(vm);
        requests.add(result);
        return result;
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
        return allRequestsOfType(ConstructorMirrorExitRequest.class);
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(String declaringClass, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
        TODClassMirrorPrepareRequest result = new TODClassMirrorPrepareRequest(vm);
        requests.add(result);
        return result;
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
        return allRequestsOfType(ClassMirrorPrepareRequest.class);
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        throw new UnsupportedOperationException();
    }

    private <T> List<T> allRequestsOfType(Class<T> klass) {
        List<T> result = new ArrayList<T>();
        for (TODMirrorEventRequest request : requests) {
            if (klass.isInstance(request)) {
                result.add(klass.cast(request));
            }
        }
        return result;
    }
    
    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
        liveRequests.remove(request);
        requests.remove(request);
    }
    
    void setRequestEnabled(TODMirrorEventRequest request, boolean enabled) {
        if (enabled) {
            request.setTimestamp(currentLogEvent.getTimestamp());
            if (request.hasNext()) {
                liveRequests.add(request);
            }
        } else {
            liveRequests.remove(request);
        }
    }

    public boolean hasNext() {
        return !liveRequests.isEmpty();
    }
    
    public MirrorEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        currentLogEvent = nextLogEvent();
        
        TODMirrorEventRequest request = liveRequests.remove();
        
        MirrorEvent result = request.next();
        if (request.hasNext()) {
            liveRequests.add(request);
        }
        return result;
    }
    
    public ILogEvent currentLogEvent() {
        return currentLogEvent;
    }
    
    public long currentTimestamp() {
        return currentLogEvent.getTimestamp();
    }
    
    public ILogEvent nextLogEvent() {
        TODMirrorEventRequest nextRequest = liveRequests.peek();
        return nextRequest != null ? nextRequest.getNextEvent() : null;
    }
    
    public void resume() {
        if (!hasNext()) {
            vm.queue.addEventSet(null);
            return;
        }
        
        TODMirrorEventSet set = new TODMirrorEventSet(vm);
        set.add(next());
        
        while (hasNext() && currentLogEvent.getTimestamp() == nextLogEvent().getTimestamp()) {
            set.add(next());
        }
        
        vm.queue.addEventSet(set);
    }

    @Override
    public MethodMirrorHandlerRequest createMethodMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirrorHandlerRequest createConstructorMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirrorSetHandlerRequest createFieldMirrorSetHandlerRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirrorGetHandlerRequest createFieldMirrorGetHandlerRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public AdviceMirrorHandlerRequest createAdviceMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public VMMirrorDeathRequest createVMMirrorDeathRequest() {
        return new TODVMMirrorDeathRequest(vm);
    }
    
    public void dumpEvents() {
//        while (hasNext()) {
//            System.out.println(next());
//        }
        for (TODMirrorEventRequest request : liveRequests) {
            System.out.println(request);
            request.dumpEvents();
        }
    }
}
