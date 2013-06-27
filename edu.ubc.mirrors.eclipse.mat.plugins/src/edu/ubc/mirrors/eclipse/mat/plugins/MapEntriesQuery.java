/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - enhancements and fixes
 *******************************************************************************/
package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.ContextProvider;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResultTable;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.IHeapObjectArgument;
import org.eclipse.mat.util.IProgressListener;

import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.Reflection;

@CommandName("map_entries")
@Name("Map Entries")
@Category("Java Collections")
public class MapEntriesQuery implements IQuery
{
	private static final String NULL = "<null>"; //$NON-NLS-1$

	@Argument
	public ISnapshot snapshot;

	@Argument(flag = Argument.UNFLAGGED)
	public IHeapObjectArgument objects;

	static class Entry
	{
		public Entry(ObjectMirror collection, ObjectMirror entry)
		{
			this.collection = collection;
			this.entry = entry;
		}

	        ObjectMirror collection;
		ObjectMirror entry;
		
		private static final MethodHandle entryGetKey = new MethodHandle() {
                    @Override
                    protected void methodCall() throws Throwable {
                        ((Map.Entry<?,?>)null).getKey();
                    }
                };
                private static final MethodHandle entryGetValue = new MethodHandle() {
                    @Override
                    protected void methodCall() throws Throwable {
                        ((Map.Entry<?,?>)null).getValue();
                    }
                };
                public ObjectMirror getKey() {
		    return (ObjectMirror)Reflection.invokeMethodHandle(entry, entryGetKey);
		}
		
		public ObjectMirror getValue() {
                    return (ObjectMirror)Reflection.invokeMethodHandle(entry, entryGetValue);
                }
	}

	public static class Result implements IResultTable
	{
		private ISnapshot snapshot;
		private List<Entry> entries;
		private ObjectMirror collection;
		
		
		private Result(ISnapshot snapshot, ObjectMirror collection, List<Entry> entries)
		{
			this.snapshot = snapshot;
			this.collection = collection;
			this.entries = entries;
		}

		public ResultMetaData getResultMetaData()
		{
			return new ResultMetaData.Builder() //

					.addContext(new ContextProvider("Key") {
						public IContextObject getContext(Object row)
						{
							return getKey(row);
						}
					}) //

					.addContext(new ContextProvider("Value") {
						public IContextObject getContext(Object row)
						{
							return getValue(row);
						}
					}) //

					.build();
		}

		public Column[] getColumns()
		{
			return new Column[] { new Column("Collection").sorting(Column.SortDirection.ASC), //
					new Column("Key"), //
					new Column("Value") };
		}

		public Object getColumnValue(Object row, int columnIndex)
		{
			try {
                            Entry entry = (Entry) row;
                            switch (columnIndex) {
                            case 0:
                                return HolographVMRegistry.toString(entry.collection);
                            case 1:
                                return HolographVMRegistry.toString(entry.getKey());
                            case 2:
                                return HolographVMRegistry.toString(entry.getValue());
                            }
                            return null;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
		}

		public int getRowCount()
		{
			return entries.size();
		}

		public Object getRow(int rowId)
		{
			return entries.get(rowId);
		}

		public IContextObject getContext(final Object row)
		{
			return new IContextObject() {
				public int getObjectId()
				{
					return HolographVMRegistry.fromMirror(((Entry) row).collection).getObjectId();
				}
			};
		}

		private IContextObject getKey(final Object row)
		{
				return new IContextObject() {
					public int getObjectId()
					{
					    return HolographVMRegistry.fromMirror(((Entry) row).getKey()).getObjectId();
					}
				};
		}

		private IContextObject getValue(final Object row)
		{
				return new IContextObject() {
					public int getObjectId()
					{
						return HolographVMRegistry.fromMirror(((Entry) row).getValue()).getObjectId();
					}
				};
		}

		// //////////////////////////////////////////////////////////////
		// map-like getters
		// //////////////////////////////////////////////////////////////

		private static final MethodHandle mapGet = new MethodHandle() {
                    @Override
                    protected void methodCall() throws Throwable {
                        ((Map<?,?>)null).get(null);
                    }
                };
                
		public String getString(String key, IProgressListener listener) throws MirrorInvocationTargetException
		{
			ObjectMirror value = (ObjectMirror)Reflection.invokeMethodHandle(collection, mapGet, key);
			return HolographVMRegistry.toString(value);
		}

		public int getObjectId(String key, IProgressListener listener)
		{
		        ObjectMirror value = (ObjectMirror)Reflection.invokeMethodHandle(collection, mapGet, key);
                        return HolographVMRegistry.fromMirror(value).getObjectId();
		}
	}

	public Result execute(IProgressListener listener) throws Exception
	{
//		InspectionAssert.heapFormatIsNot(snapshot, "DTFJ-PHD"); //$NON-NLS-1$
//		listener.subTask(Messages.HashEntriesQuery_Msg_Extracting);

		List<Entry> hashEntries = new ArrayList<Entry>();
		
		for (int[] ids : objects)
		{
			for (int id : ids)
			{
			        IObject obj = snapshot.getObject(id);
			        ObjectMirror mirror = HolographVMRegistry.getMirror(obj, listener);
			        ObjectMirror entrySet = (ObjectMirror)Reflection.invokeMethodHandle(mirror, new MethodHandle() {
			            @Override
			            protected void methodCall() throws Throwable {
			                ((Map<?, ?>)null).entrySet();
			            }
			        });
			        List<ObjectMirror> entryMirrors = CollectionValuesQuery.getValues(entrySet);
			        for (ObjectMirror element : entryMirrors) {
			            hashEntries.add(new Entry(mirror, element));
			        }

				if (listener.isCanceled()) throw new IProgressListener.OperationCanceledException();
			}
		}

		listener.done();

		// Interesting bug - this is a mixed set of entries from different collections!
		return new Result(snapshot, null, hashEntries);
	}
}
