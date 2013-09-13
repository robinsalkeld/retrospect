/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.test;

import java.io.IOException;
import java.net.Socket;

import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;


public class PrintOSGiBundles {

    public static String printBundles(Bundle[] bundles) throws IOException {
        Appendable builder = System.out; //new StringBuilder();
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
        Appendable builder = System.out; //new StringBuilder();
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
