package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.eval.IEvaluationContext;

public class FakeJavaProject implements IJavaProject {

    public FakeJavaProject() {
        this.project = new FakeProject();
    }

    private final IProject project;

    @Override
    public IJavaElement[] getChildren() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasChildren() throws JavaModelException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean exists() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IJavaElement getAncestor(int ancestorType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttachedJavadoc(IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IResource getCorrespondingResource() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getElementName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getElementType() {
        return COMPILATION_UNIT;
    }

    @Override
    public String getHandleIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaModel getJavaModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaProject getJavaProject() {
        return this;
    }

    @Override
    public IOpenable getOpenable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaElement getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaElement getPrimaryElement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IResource getResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISchedulingRule getSchedulingRule() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IResource getUnderlyingResource() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStructureKnown() throws JavaModelException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String findRecommendedLineSeparator() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IBuffer getBuffer() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasUnsavedChanges() throws JavaModelException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConsistent() throws JavaModelException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void makeConsistent(IProgressMonitor progress)
            throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void open(IProgressMonitor progress) throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void save(IProgressMonitor progress, boolean force)
            throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IClasspathEntry decodeClasspathEntry(String encodedEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeClasspathEntry(IClasspathEntry classpathEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaElement findElement(IPath path) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaElement findElement(IPath path, WorkingCopyOwner owner)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IJavaElement findElement(String bindingKey, WorkingCopyOwner owner)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragment findPackageFragment(IPath path)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String fullyQualifiedName) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String fullyQualifiedName,
            IProgressMonitor progressMonitor) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String fullyQualifiedName, WorkingCopyOwner owner)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String fullyQualifiedName, WorkingCopyOwner owner,
            IProgressMonitor progressMonitor) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String packageName, String typeQualifiedName)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String packageName, String typeQualifiedName,
            IProgressMonitor progressMonitor) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String packageName, String typeQualifiedName,
            WorkingCopyOwner owner) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IType findType(String packageName, String typeQualifiedName,
            WorkingCopyOwner owner, IProgressMonitor progressMonitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot[] getAllPackageFragmentRoots()
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] getNonJavaResources() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOption(String optionName, boolean inheritJavaCoreOptions) {
        if (optionName.equals("org.eclipse.jdt.core.compiler.source")) {
            // TODO-RS: Not 100% sure about this
            return "1.7";
        }
        return null;
    }

    @Override
    public Map<String, String> getOptions(boolean inheritJavaCoreOptions) {
        return new HashMap<String, String>();
    }

    @Override
    public IPath getOutputLocation() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot getPackageFragmentRoot(
            String externalLibraryPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot[] getPackageFragmentRoots()
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPackageFragment[] getPackageFragments() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public IClasspathEntry[] getRawClasspath() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getRequiredProjectNames() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasBuildState() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasClasspathCycle(IClasspathEntry[] entries) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnClasspath(IJavaElement element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnClasspath(IResource resource) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IEvaluationContext newEvaluationContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IRegion region,
            IProgressMonitor monitor) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IRegion region,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IType type, IRegion region,
            IProgressMonitor monitor) throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IType type, IRegion region,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPath readOutputLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IClasspathEntry[] readRawClasspath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOption(String optionName, String optionValue) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOptions(Map newOptions) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOutputLocation(IPath path, IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRawClasspath(IClasspathEntry[] entries,
            IPath outputLocation, boolean canModifyResources,
            IProgressMonitor monitor) throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRawClasspath(IClasspathEntry[] entries,
            boolean canModifyResources, IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRawClasspath(IClasspathEntry[] entries,
            IClasspathEntry[] referencedEntries, IPath outputLocation,
            IProgressMonitor monitor) throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IClasspathEntry[] getReferencedClasspathEntries()
            throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRawClasspath(IClasspathEntry[] entries,
            IProgressMonitor monitor) throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRawClasspath(IClasspathEntry[] entries,
            IPath outputLocation, IProgressMonitor monitor)
            throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

}
