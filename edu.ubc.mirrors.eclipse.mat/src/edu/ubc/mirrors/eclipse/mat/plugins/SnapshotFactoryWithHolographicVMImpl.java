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
package edu.ubc.mirrors.eclipse.mat.plugins;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.parser.IIndexBuilder;
import org.eclipse.mat.snapshot.IOQLQuery;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.OQLParseException;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.SnapshotFormat;
import org.eclipse.mat.util.IProgressListener;

public class SnapshotFactoryWithHolographicVMImpl implements SnapshotFactory.Implementation {

    private final SnapshotFactory.Implementation wrappedImpl;
    
    public SnapshotFactoryWithHolographicVMImpl() {
        try {
            // TODO-RS: Hackity hackity hack! But it's all for a good cause.
            String snapshotFactoryName = "org.eclipse.mat.parser.internal.SnapshotFactoryImpl";
            ClassLoader matCL = IIndexBuilder.class.getClassLoader();
            Class<?> snapshotFactoryClass = matCL.loadClass(snapshotFactoryName);
            this.wrappedImpl = (SnapshotFactory.Implementation)snapshotFactoryClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ISnapshot openSnapshot(File file, Map<String, String> arguments, IProgressListener listener) throws SnapshotException {
        ISnapshot snapshot = wrappedImpl.openSnapshot(file, arguments, listener);
        HolographVMRegistry.getHolographVM(snapshot, listener);
        return snapshot;
    }

    @Override
    public void dispose(ISnapshot snapshot) {
        HolographVMRegistry.dispose(snapshot);
        wrappedImpl.dispose(snapshot);
    }

    @Override
    public IOQLQuery createQuery(String queryString) throws OQLParseException, SnapshotException {
        return wrappedImpl.createQuery(queryString);
    }

    @Override
    public List<SnapshotFormat> getSupportedFormats() {
        return wrappedImpl.getSupportedFormats();
    }

}
