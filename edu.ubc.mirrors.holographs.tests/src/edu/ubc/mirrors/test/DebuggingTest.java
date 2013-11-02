package edu.ubc.mirrors.test;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class DebuggingTest implements IApplication {

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        return run(args);
    }

    public void stop() {
        
    }

    protected Object run(String[] args) throws Exception {
        Bundle appBundle = null;
        for (Bundle bundle : FrameworkUtil.getBundle(DebuggingTest.class).getBundleContext().getBundles()) {
            if (bundle.getSymbolicName().equals("org.eclipse.equinox.app")) {
                appBundle = bundle;
            }
        }
        for (ServiceReference<?> service : appBundle.getServicesInUse()) {
            if ("org.eclipse.ui.ide.workbench".equals(service.getProperty("service.pid"))) {
                lookAt(service);
            }
        }
        return null;
    }

    private void lookAt(ServiceReference<?> service) {
        int bp = 5;
        bp++;
    }

}
