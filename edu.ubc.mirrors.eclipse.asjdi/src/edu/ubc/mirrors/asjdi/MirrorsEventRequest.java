package edu.ubc.mirrors.asjdi;

import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsEventRequest extends MirrorsMirror implements EventRequest {

    protected final MirrorEventRequest wrapped;
    
    protected static final String WRAPPER = "edu.ubc.mirrors.asjdi.wrapper";
    
    public MirrorsEventRequest(MirrorsVirtualMachine vm, MirrorEventRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
        this.wrapped.putProperty(WRAPPER, this);
    }

    @Override
    public void addCountFilter(int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disable() {
        wrapped.disable();
    }

    @Override
    public void enable() {
        wrapped.enable();
    }

    @Override
    public Object getProperty(Object key) {
        return wrapped.getProperty(key);
    }

    @Override
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }

    @Override
    public void putProperty(Object key, Object value) {
        wrapped.putProperty(key, value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        wrapped.setEnabled(enabled);
    }

    @Override
    public void setSuspendPolicy(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public int suspendPolicy() {
        // TODO Auto-generated method stub
        return 0;
    }

}
