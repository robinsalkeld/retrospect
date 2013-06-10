/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.snapshot.actions;

import java.math.BigInteger;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.ui.MemoryAnalyserPlugin;
import org.eclipse.mat.ui.Messages;
import org.eclipse.mat.ui.QueryExecution;
import org.eclipse.mat.ui.snapshot.editor.HeapEditor;
import org.eclipse.mat.ui.snapshot.editor.ISnapshotEditorInput;
import org.eclipse.mat.util.MessageUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class OpenObjectByIdAction extends Action
{

    public OpenObjectByIdAction()
    {
        super(Messages.OpenObjectByIdAction_FindObjectByAddress, MemoryAnalyserPlugin
                        .getImageDescriptor(MemoryAnalyserPlugin.ISharedImages.FIND));
    }

    @Override
    public void run()
    {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = page == null ? null : page.getActiveEditor();

        if (part instanceof HeapEditor)
        {
            HeapEditor editor = (HeapEditor) part;

            String value = askForAddress();

            if (value != null)
            {
                retrieveObjectAndOpenPane(editor, value);
            }
        }
    }

    private void retrieveObjectAndOpenPane(HeapEditor editor, String value)
    {
        String errorMessage = null;

        try
        {
            // Long.parseLong works only for positive hex
            long objectAddress = new BigInteger(value.substring(2), 16).longValue();
            ISnapshot snapshot = ((ISnapshotEditorInput) editor.getPaneEditorInput()).getSnapshot();
            if (snapshot == null)
            {
                errorMessage = Messages.OpenObjectByIdAction_ErrorGettingHeapDump;
            }
            else
            {
                int objectId = snapshot.mapAddressToId(objectAddress);
                if (objectId < 0)
                {
                    errorMessage = MessageUtil.format(Messages.OpenObjectByIdAction_NoObjectWithAddress,
                                    new Object[] { value });
                }
                else
                {
                    QueryExecution.executeCommandLine(editor, null, "list_objects " + value); //$NON-NLS-1$
                }
            }
        }
        catch (NumberFormatException e)
        {
            // $JL-EXC$
            errorMessage = Messages.OpenObjectByIdAction_AddressIsNotHexNumber;
        }
        catch (SnapshotException e)
        {
            // $JL-EXC$
            errorMessage = MessageUtil.format(Messages.OpenObjectByIdAction_ErrorReadingObject, new Object[] { e
                            .getMessage() });
        }

        if (errorMessage != null)
        {
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                            Messages.OpenObjectByIdAction_ErrorOpeningObject, errorMessage);
        }
    }

    private String askForAddress()
    {
        final Pattern pattern = Pattern.compile("^0x\\p{XDigit}+$");//$NON-NLS-1$

        InputDialog dialog = new InputDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                        Messages.OpenObjectByIdAction_FindObjectByAddress, Messages.OpenObjectByIdAction_ObjectAddress,
                        "0x", new IInputValidator() //$NON-NLS-1$
                        {

                            public String isValid(String newText)
                            {
                                return !pattern.matcher(newText).matches() ? Messages.OpenObjectByIdAction_AddressMustBeHexNumber
                                                : null;
                            }

                        });

        int result = dialog.open();

        String value = dialog.getValue();
        if (result == IDialogConstants.CANCEL_ID)
            return null;
        return value;
    }
}
