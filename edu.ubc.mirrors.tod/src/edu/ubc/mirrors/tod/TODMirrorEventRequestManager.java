package edu.ubc.mirrors.tod;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;

public class TODMirrorEventRequestManager implements MirrorEventRequestManager, MirrorEventQueue {

    private final TODVirtualMachineMirror vm;
    private final PriorityQueue<TODMirrorEventRequest> liveRequests = new PriorityQueue<TODMirrorEventRequest>();
    private final Set<TODMirrorEventRequest> deadRequests = new HashSet<TODMirrorEventRequest>();
    
    public TODMirrorEventRequestManager(TODVirtualMachineMirror vm) {
        this.vm = vm;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(FieldMirror field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(FieldMirror field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
        liveRequests.remove(request);
        deadRequests.remove(request);
    }

    public boolean hasNext() {
        return !liveRequests.isEmpty();
    }
    
    public MirrorEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        TODMirrorEventRequest request = liveRequests.remove();
        MirrorEvent result = request.next();
        if (request.hasNext()) {
            liveRequests.add(request);
        } else {
            deadRequests.add(request);
        }
        return result;
    }
    
    public long nextTimestamp() {
        TODMirrorEventRequest nextRequest = liveRequests.peek();
        return nextRequest != null ? nextRequest.getNextTimestamp() : Long.MAX_VALUE;
    }
    
    @Override
    public MirrorEventSet remove() throws InterruptedException {
        if (!hasNext()) {
            return null;
        }
        
        MirrorEventSet result = new TODMirrorEventSet();
        long timestamp = nextTimestamp();
        result.add(next());
        
        while (hasNext() && timestamp == nextTimestamp()) {
            result.add(next());
        }
        
        return result;
    }
}
