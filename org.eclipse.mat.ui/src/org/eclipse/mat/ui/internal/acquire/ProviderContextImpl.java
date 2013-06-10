/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.internal.acquire;

import java.io.File;
import java.text.ParsePosition;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.ContextDerivedData;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.registry.QueryContextImpl;

public class ProviderContextImpl extends QueryContextImpl
{

	public ContextDerivedData getContextDerivedData()
	{
		return null;
	}

	public File getPrimaryFile()
	{
		return null;
	}

	public String mapToExternalIdentifier(int objectId) throws SnapshotException
	{
		return String.valueOf(objectId);
	}

	public int mapToObjectId(String externalIdentifier) throws SnapshotException
	{
		return Integer.valueOf(externalIdentifier);
	}

	public Object parse(Class<?> type, Advice advice, String[] args, ParsePosition pos) throws SnapshotException
	{
		return null;
	}

	public boolean parses(Class<?> type, Advice advice)
	{
		return false;
	}

}
