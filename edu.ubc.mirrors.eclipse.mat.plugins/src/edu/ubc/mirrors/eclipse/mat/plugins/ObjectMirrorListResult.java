package edu.ubc.mirrors.eclipse.mat.plugins;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IDecorator;
import org.eclipse.mat.query.IIconProvider;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.GCRootInfo;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.query.Icons;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class ObjectMirrorListResult {
    
    public static Tree make(ISnapshot snapshot, List<Object> mirrors, boolean inbound) {
        if (inbound) {
            return new Inbound(snapshot, mirrors);
        } else {
            return new Outbound(snapshot, mirrors);
        }
    }
    
    /**
     * Helper class which describes a tree of objects by inbound references.
     */
    public static class Inbound extends Tree
    {
        /**
         * Construct a inbound references tree
         * @param snapshot the snapshot
         * @param mirrors the set of objects to form roots of the trees
         */
        public Inbound(ISnapshot snapshot, List<Object> mirrors)
        {
            super(snapshot, mirrors);
        }

        protected int[] children(Node node) throws SnapshotException
        {
            if (node instanceof MirrorNode) return new int[0];
            return snapshot.getInboundRefererIds(node.objectId);
        }

        protected void fillInAttribute(LinkedNode node) throws SnapshotException
        {
            IObject heapObject = snapshot.getObject(node.objectId);
            long parentAddress = snapshot.mapIdToAddress(node.parent.objectId);
            node.attribute = extractAttribute(heapObject, parentAddress);
        }

        /**
         * Get the URL for a row of the tree.
         * Returns either an arrow item or the base icon if no children.
         * @return the URL of the icon
         */
        public URL getIcon(Object row)
        {
            if (row instanceof MirrorNode) {
                return HolographVMRegistry.icon(((MirrorNode)row).value);
            } else if (row instanceof LinkedNode)
                return Icons.inbound(snapshot, ((Node) row).objectId);
            else
                return Icons.forObject(snapshot, ((Node) row).objectId);
        }
    }

    /**
     * Helper class which describes a tree of objects by outbound references.
     */
    public static class Outbound extends Tree
    {
        /**
         * Construct a outbound references tree
         * @param snapshot the snapshot
         * @param objectIds the set of objects to form roots of the trees
         */
        public Outbound(ISnapshot snapshot, List<Object> mirrors)
        {
            super(snapshot, mirrors);
        }

        protected int[] children(Node node) throws SnapshotException
        {
            if (node instanceof MirrorNode) return new int[0];
            return snapshot.getOutboundReferentIds(node.objectId);
        }

        protected void fillInAttribute(LinkedNode node) throws SnapshotException
        {
            IObject heapObject = snapshot.getObject(node.parent.objectId);
            long parentAddress = snapshot.mapIdToAddress(node.objectId);
            node.attribute = extractAttribute(heapObject, parentAddress);
        }

        /**
         * Get the URL for a row of the tree.
         * Returns either an arrow item or the base icon if no children.
         * @return the URL of the icon
         */
        public URL getIcon(Object row)
        {
            if (row instanceof MirrorNode) {
                return HolographVMRegistry.icon(((MirrorNode)row).value);
            } else {
                return Icons.outbound(snapshot, ((Node) row).objectId);
            }
        }
    }

    private abstract static class Tree implements IResultTree, IIconProvider, IDecorator
    {
        protected ISnapshot snapshot;
        private List<?> objects;

        /**
         * Build a tree from objects as roots
         * @param snapshot
         * @param objectIds
         */
        public Tree(ISnapshot snapshot, List<Object> mirrors)
        {
            this.snapshot = snapshot;
            this.objects = new LazyList(mirrors);
        }

        /**
         * Enhance the tree with extra data.
         */
        public final ResultMetaData getResultMetaData()
        {
            return null;
        }

        /**
         * Get the columns, which are the class name, the shallow heap and the retained heap.
         */
        public final Column[] getColumns()
        {
            return new Column[] { new Column("Class Name").decorator(this), //
                    new Column("Shallow Heap", long.class).noTotals(), //
                    new Column("Retained Heap", long.class).noTotals() };
        }

        /**
         * Get the actual rows.
         */
        public final List<?> getElements()
        {
            return objects;
        }

        private final List<?> asList(Node parent, int[] ids)
        {
            List<LinkedNode> objects = new ArrayList<LinkedNode>(ids.length);
            for (int ii = 0; ii < ids.length; ii++)
                objects.add(new LinkedNode(parent, ids[ii]));
            return objects;
        }

        public final List<?> getChildren(Object parent)
        {
            try
            {
                Node node = (Node) parent;
                int[] outbounds = children(node);
                return asList(node, outbounds);
            }
            catch (SnapshotException e)
            {
                throw new RuntimeException(e);
            }
        }

        protected abstract int[] children(Node node) throws SnapshotException;

        public final boolean hasChildren(Object element)
        {
            return !(element instanceof MirrorNode);
        }

        public final Object getColumnValue(Object row, int columnIndex)
        {
            if (row instanceof MirrorNode) {
                MirrorNode node = (MirrorNode)row;
                
                switch (columnIndex)
                {
                case 0:
                    if (node.label == null)
                    {
                        ((MirrorNode)node).calculateLabel();
                    }
                    return node.label;
                case 1:
                    return "N/A";
                case 2:
                    return "N/A";
                }
                
                return null;
            }
            
            try
            {
                Node node = (Node) row;

                switch (columnIndex)
                {
                case 0:
                    if (node.label == null)
                    {
                        if (node instanceof MirrorNode) {
                            ((MirrorNode)node).calculateLabel();
                        } else {
                            IObject obj = snapshot.getObject(node.objectId);
                            node.label = obj.getDisplayName();
                            node.shallowHeap = obj.getUsedHeapSize();
                        }
                    }
                    return node.label;
                case 1:
                    if (node.shallowHeap == -1)
                        node.shallowHeap = snapshot.getHeapSize(node.objectId);
                    return node.shallowHeap;
                case 2:
                    if (node.retainedHeap == -1)
                        node.retainedHeap = snapshot.getRetainedHeapSize(node.objectId);
                    return node.retainedHeap;
                }
            }
            catch (SnapshotException e)
            {
                throw new RuntimeException(e);
            }

            return null;
        }

        public final IContextObject getContext(final Object row)
        {
            return ((Node) row).getContext();
        }

        public final String prefix(Object row)
        {
            if (row instanceof LinkedNode)
            {
                LinkedNode node = (LinkedNode) row;
                if (node.attribute == null)
                {
                    try
                    {
                        fillInAttribute(node);
                    }
                    catch (SnapshotException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                return node.attribute;
            }
            else if (row instanceof MirrorNode) 
            {
                return "<new holographic object>";
            }
            else
            {
                return null;
            }
        }

        protected abstract void fillInAttribute(LinkedNode node) throws SnapshotException;

        public final String suffix(Object row)
        {
            
            Node node = (Node) row;
            if (node instanceof MirrorNode) {
                return null;
            }
            if (node.gcRoots == null)
            {
                try
                {
                    GCRootInfo[] gc = snapshot.getGCRootInfo(node.objectId);
                    node.gcRoots = gc != null ? GCRootInfo.getTypeSetAsString(gc) : Node.NOT_A_GC_ROOT;
                }
                catch (SnapshotException e)
                {
                    throw new RuntimeException(e);
                }
            }

            return node.gcRoots == Node.NOT_A_GC_ROOT ? null : node.gcRoots;
        }

        protected String extractAttribute(IObject heapObject, long parentAddress)
        {
            StringBuilder s = new StringBuilder(64);

            List<NamedReference> refs = heapObject.getOutboundReferences();
            for (NamedReference reference : refs)
            {
                if (reference.getObjectAddress() == parentAddress)
                {
                    if (s.length() > 0)
                        s.append(", "); //$NON-NLS-1$
                        s.append(reference.getName());
                }
            }

            return s.toString();
        }
    }

    // //////////////////////////////////////////////////////////////
    // helper classes
    // //////////////////////////////////////////////////////////////

    private static class Node
    {
        /** A string guaranteed not to be equal by identity to any other String */
        public static final String NOT_A_GC_ROOT = new String("$ not a gc root $"); //$NON-NLS-1$

        int objectId;
        String label;
        String gcRoots;
        long shallowHeap;
        long retainedHeap;

        private Node(int objectId)
        {
            this.objectId = objectId;
            this.shallowHeap = -1;
            this.retainedHeap = -1;
        }
        
        public IContextObject getContext() {
            return new IContextObject()
            {
                public int getObjectId()
                {
                    return objectId;
                }
            };
        }
    }

    private static class LinkedNode extends Node
    {
        Node parent;
        String attribute;

        private LinkedNode(Node parent, int objectId)
        {
            super(objectId);
            this.parent = parent;
        }
    }

    private static class MirrorNode extends Node 
    {
        Object value;
        int stashedObjectId = -1;
        
        private MirrorNode(Object value)
        {
            super(-1);
            this.value = value;
        }

        public void calculateLabel() {
            if (value instanceof ObjectMirror) {
                ObjectMirror mirror = (ObjectMirror)value;
                if (mirror.getClassMirror().getClassName().equals(String.class.getName())) {
                    this.label = '"' + Reflection.getRealStringForMirror((InstanceMirror)mirror) + '"';
                } else {
                    this.label = mirror.getClassMirror().getClassName() + "@" + mirror.identityHashCode();
                }
            } else {
                this.label = String.valueOf(value);
            }
        }
        
        @Override
        public IContextObject getContext() {
            if (value instanceof ObjectMirror) {
                if (stashedObjectId == -1) {
                    stashedObjectId = HolographVMRegistry.stashObjectMirror((ObjectMirror)value);
                }
                return new IContextObject()
                {
                    public int getObjectId()
                    {
                        return stashedObjectId;
                    }
                };
            } else {
                return super.getContext();
            }
        }
    }
    
    private static class LazyList implements List<Node>, RandomAccess
    {
        int created = 0;

        private int[] objectIds;
        private List<Object> mirrors;
        private Node[] elements;

        private LazyList(List<Object> mirrors)
        {
            this.objectIds = new int[mirrors.size()];
            this.mirrors = mirrors;
            this.elements = new Node[objectIds.length];
            for (int i = 0; i < objectIds.length; i++) {
                Object value = mirrors.get(i);
                this.objectIds[i] = -1;
                if (value instanceof ObjectMirror) {
                    IObject object = HolographVMRegistry.fromMirror((ObjectMirror)value);
                    if (object != null) {
                        this.objectIds[i] = object.getObjectId();
                    }
                }
            }
        }

        public Node get(int index)
        {
            if (index < 0 || index >= objectIds.length)
                throw new ArrayIndexOutOfBoundsException(index);

            if (elements[index] == null)
            {
                elements[index] = initialNode(index);
                created++;
            }

            return elements[index];
        }

        private Node initialNode(int index) {
            if (objectIds[index] == -1) {
                return new MirrorNode(mirrors.get(index));
            } else {
                return new Node(objectIds[index]);
            }
           
        }
        
        public Node set(int index, Node node)
        {
            if (index < 0 || index >= objectIds.length)
                throw new ArrayIndexOutOfBoundsException(index);

            Node retValue = elements[index];
            if (retValue == null)
                retValue = initialNode(index);

            elements[index] = node;
            objectIds[index] = node.objectId;
            if (node instanceof MirrorNode) {
                mirrors.set(index, ((MirrorNode)node).value);
            }

            return retValue;
        }

        public void add(int index, Node element)
        {
            throw new UnsupportedOperationException();
        }

        public boolean add(Node o)
        {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends Node> c)
        {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(int index, Collection<? extends Node> c)
        {
            throw new UnsupportedOperationException();
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o)
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        public int indexOf(Object o)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty()
        {
            return objectIds.length == 0;
        }

        public Iterator<Node> iterator()
        {
            return new Iterator<Node>()
                    {
                int index = 0;

                public boolean hasNext()
                {
                    return index < objectIds.length;
                }

                public Node next()
                {
                    return get(index++);
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
                    };
        }

        public int lastIndexOf(Object o)
        {
            throw new UnsupportedOperationException();
        }

        public ListIterator<Node> listIterator()
        {
            return listIterator(0);
        }

        public ListIterator<Node> listIterator(final int index)
        {
            return new ListIterator<Node>()
                    {
                int pos = index;
                int last = -1;

                public void add(Node o)
                {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext()
                {
                    return pos < elements.length;
                }

                public boolean hasPrevious()
                {
                    return pos > 0;
                }

                public Node next()
                {
                    Node n = get(pos);
                    last = pos++;
                    return n;
                }

                public int nextIndex()
                {
                    return pos + 1;
                }

                public Node previous()
                {
                    Node n = get(pos);
                    last = pos--;
                    return n;
                }

                public int previousIndex()
                {
                    return pos - 1;
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

                public void set(Node o)
                {
                    if (last == -1)
                        throw new IllegalStateException();

                    LazyList.this.set(last, o);
                }

                    };
        }

        public Node remove(int index)
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        public int size()
        {
            return objectIds.length;
        }

        public List<Node> subList(int fromIndex, int toIndex)
        {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray()
        {
            createNodesIfNecessary();

            Object[] copy = new Object[elements.length];
            System.arraycopy(elements, 0, copy, 0, elements.length);
            return copy;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a)
        {
            createNodesIfNecessary();

            if (a.length < elements.length)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), elements.length);
            System.arraycopy(elements, 0, a, 0, elements.length);
            if (a.length > elements.length)
                a[elements.length] = null;
            return a;
        }

        private void createNodesIfNecessary()
        {
            // for creation of nodes (if necessary)
            if (created != objectIds.length)
            {
                for (int ii = 0; ii < elements.length; ii++)
                    if (elements[ii] == null)
                        get(ii);
            }
        }
    }

    private ObjectMirrorListResult()
    {}

}

    