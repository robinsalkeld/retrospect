/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.snapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.SnapshotInfo;
import org.eclipse.mat.ui.MemoryAnalyserPlugin;
import org.eclipse.mat.ui.Messages;
import org.eclipse.mat.ui.SnapshotHistoryService;
import org.eclipse.mat.ui.internal.PreferenceConstants;
import org.eclipse.mat.ui.util.ErrorHelper;
import org.eclipse.mat.ui.util.ProgressMonitorWrapper;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.MessageUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

public abstract class ParseHeapDumpJob extends Job
{
    private final IPath path;
    private final Map<String, String> arguments;
    
    private static Map<String, String> defaultArguments()
    {
        IPreferenceStore prefs = MemoryAnalyserPlugin.getDefault().getPreferenceStore();
        if (prefs.getBoolean(PreferenceConstants.P_KEEP_UNREACHABLE_OBJECTS))
        {
            Map<String, String> args = new HashMap<String, String>();
            args.put("keep_unreachable_objects", Boolean.TRUE.toString()); //$NON-NLS-1$
            return args;
        }
        else
        {
            return Collections.<String, String> emptyMap();
        }
    }

    public ParseHeapDumpJob(IPath path)
    {
        this(path, defaultArguments());
    }

    public ParseHeapDumpJob(IPath path, Map<String, String> args)
        {
        super(MessageUtil.format(Messages.ParseHeapDumpJob_ParsingHeapDumpFrom, path.toOSString()));
        this.path = path;
        this.arguments = args;
        this.setUser(true);

        this.setRule(new ParseRule(path));
    }

    protected IStatus run(IProgressMonitor monitor)
    {
        this.setProperty(IProgressConstants.PROPERTY_IN_DIALOG, Boolean.TRUE);

        try
        {
            SnapshotHistoryService.getInstance().addVisitedPath(MemoryAnalyserPlugin.EDITOR_ID, path.toOSString());

            final ISnapshot snapshot = SnapshotFactory.openSnapshot(path.toFile(), arguments, new ProgressMonitorWrapper(monitor));

            if (snapshot == null)
            {
                return Status.CANCEL_STATUS;
            }
            else
            {
                // copy snapshot info -> needed for serialization of file
                // history
                SnapshotInfo source = snapshot.getSnapshotInfo();
                SnapshotInfo destination = new SnapshotInfo(source.getPath(), source.getPrefix(), source.getJvmInfo(),
                                source.getIdentifierSize(), source.getCreationDate(), source.getNumberOfObjects(),
                                source.getNumberOfGCRoots(), source.getNumberOfClasses(), source
                                                .getNumberOfClassLoaders(), source.getUsedHeapSize());
                // This property is needed for the outline view , but don't copy all properties e.g. UnreachableObjectsHistogram
                Serializable format = source.getProperty("$heapFormat"); //$NON-NLS-1$
                if (format != null)
                    destination.setProperty("$heapFormat", format); //$NON-NLS-1$
                SnapshotHistoryService.getInstance().addVisitedPath(MemoryAnalyserPlugin.EDITOR_ID, path.toOSString(),
                                destination);

                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        ParseHeapDumpJob.this.finished(snapshot);
                    }
                });

                return Status.OK_STATUS;
            }
        }
        catch (IProgressListener.OperationCanceledException e)
        {
            return Status.CANCEL_STATUS;
        }
        catch (SnapshotException e)
        {
            return ErrorHelper.createErrorStatus(e);
        }
    }

    public boolean isModal()
    {
        Boolean isModal = (Boolean) this.getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
        if (isModal == null)
            return false;
        return isModal.booleanValue();
    }

    protected abstract void finished(ISnapshot snapshot);

    // //////////////////////////////////////////////////////////////
    // internal classes
    // //////////////////////////////////////////////////////////////

    class ParseRule implements ISchedulingRule
    {
        IPath path;

        public ParseRule(IPath filename)
        {
            this.path = filename;
        }

        public boolean contains(ISchedulingRule rule)
        {
            return rule instanceof ParseRule && ((ParseRule) rule).path.equals(path);
        }

        public boolean isConflicting(ISchedulingRule rule)
        {
            return contains(rule);
        }

    }

}
