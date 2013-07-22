package edu.ubc.mirrors.test;

import org.eclipse.equinox.app.IApplicationContext;

public abstract class DefaultApplication {

    protected abstract Object run(String[] args) throws Exception;
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        return run(args);
    }

    public void stop() {
        
    }
}
