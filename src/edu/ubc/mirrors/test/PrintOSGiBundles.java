package edu.ubc.mirrors.test;

import java.io.IOException;

import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;

public class PrintOSGiBundles {

    public static Object print(BundleRepository repository) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Bundle b : repository.getBundles()) {
            builder.append(b + "\n");
        }
        return builder;
    }
    
}
