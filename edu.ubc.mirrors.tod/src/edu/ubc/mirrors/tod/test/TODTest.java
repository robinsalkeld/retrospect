package edu.ubc.mirrors.tod.test;

import java.net.URI;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;

public class TODTest implements IApplication {

    public static void main(String[] args) throws Exception {
        URI theUri = URI.create("tod-dbgrid-local:/");
        TODConfig theConfig = new TODConfig();
        ISession theSession = SessionTypeManager.getInstance().createSession(null, theUri, theConfig);
        ILogBrowser logBrowser = theSession.getLogBrowser();
        logBrowser.getStructureDatabase();
    }

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
