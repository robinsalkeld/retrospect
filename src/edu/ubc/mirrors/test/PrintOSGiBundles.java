package edu.ubc.mirrors.test;

import java.io.IOException;

import org.aspectj.lang.annotation.Aspect;
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;


@Aspect
public class PrintOSGiBundles {

    public static String printBundles(Bundle[] bundles) {
        StringBuilder builder = new StringBuilder();
        for (Bundle b : bundles) {
            builder.append(b + "\n");
            ServiceReference<?>[] servicesInUse = b.getServicesInUse();
            if (servicesInUse != null) {
                builder.append("\tServices in use:\n");
                for (ServiceReference<?> ref : servicesInUse) {
                    builder.append("\t\t" + ref + "\n");
                }
            }
        }
        return builder.toString();
    }
    
    public static String print(BundleRepository repository) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Bundle b : repository.getBundles()) {
            builder.append(b + "\n");
            ServiceReference<?>[] servicesInUse = b.getServicesInUse();
            if (servicesInUse != null) {
                builder.append("\tServices in use:\n");
                for (ServiceReference<?> ref : servicesInUse) {
                    builder.append("\t\t" + ref + "\n");
                }
            }
        }
        return builder.toString();
    }
    
}
