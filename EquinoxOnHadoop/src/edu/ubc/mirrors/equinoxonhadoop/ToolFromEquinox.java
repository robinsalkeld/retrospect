package edu.ubc.mirrors.equinoxonhadoop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleRevision;

public class ToolFromEquinox implements Tool {

    private Tool wrappedTool;
    
    @Override
    public void setConf(Configuration config) {
        Framework framework = createFramework(config);
        wrappedTool = loadFromFramework(framework, Tool.class);
        wrappedTool.setConf(config);
        
        ClassLoader osgiLoader = wrappedTool.getClass().getClassLoader();
        config.setClassLoader(osgiLoader);
        Thread.currentThread().setContextClassLoader(osgiLoader);
    }
    
    @Override
    public Configuration getConf() {
        return wrappedTool.getConf();
    }
    
    @Override
    public int run(String[] args) throws Exception {
        return wrappedTool.run(args);
    }
    
    public Framework createFramework(Configuration config) {
        Map<String, String> frameworkConfig = new HashMap<String, String>();
        
        // TODO-RS: We should be able to remove sun.misc?
        frameworkConfig.put(Constants.FRAMEWORK_BOOTDELEGATION, "org.apache.hadoop.*,sun.misc,sun.reflect");
        frameworkConfig.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_APP);
        frameworkConfig.put(Constants.FRAMEWORK_STORAGE, "/Users/robinsalkeld/Documents/UBC/Code/Retrospect/EquinoxOnHadoop/eclipse");
        frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
        frameworkConfig.put("osgi.install.area", "/Users/robinsalkeld/Documents/UBC/Code/Retrospect/EquinoxOnHadoop/eclipse");
//        frameworkConfig.put("osgi.console", "true");
        
        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        Framework framework = factory.newFramework(frameworkConfig);
        try {
            framework.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
        
        InputStream in = ToolFromEquinox.class.getResourceAsStream("/bundles.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        List<Bundle> bundles = new ArrayList<Bundle>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                bundles.add(framework.getBundleContext().installBundle("reference:file:" + line));
            }
            
            for (Bundle bundle : bundles) {
                if (!bundleIsFragment(bundle)) {
                    bundle.start();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
        
        return framework;
    }
    
    private boolean bundleIsFragment(Bundle bundle) {
        return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
    }
    
    public <T> T loadFromFramework(Framework framework, Class<? extends T> klass) {
        BundleContext bundleContext = framework.getBundleContext();
        ServiceReference<? extends T> ref = bundleContext.getServiceReference(klass);
        return klass.cast(bundleContext.getService(ref));
    }
    
    public static void main(String[] args) throws Exception {
        int res = -1;
        try {
            res = ToolRunner.run(new Configuration(), new ToolFromEquinox(), args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(res);
    }
}