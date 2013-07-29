package edu.ubc.mirrors.holographs.jdi;


import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import com.ibm.icu.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * A holograph socket attaching connector
 */
public class SocketAttachConnector implements IVMConnector {
		
	/**
	 * Return the socket transport attaching connector
	 * 
	 * @exception CoreException if unable to locate the connector
	 */
	protected static AttachingConnector getAttachingConnector() throws CoreException {
		return new HolographConnector();
	}

	/**
	 * @see IVMConnector#getIdentifier()
	 */
	public String getIdentifier() {
		return "edu.ubc.mirrors.holographs.jdi.SocketAttachConnector";
	}

	/**
	 * @see IVMConnector#getName()
	 */
	public String getName() {
		return "Holograph Socket Attach"; 
	}
	
	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}		

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#connect(java.util.Map, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.debug.core.ILaunch)
	 */
	public void connect(Map arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.SocketAttachConnector_Connecting____1, 2); 
		subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Configuring_connection____1); 
		
		AttachingConnector connector= getAttachingConnector();
		String portNumberString = (String)arguments.get("port"); //$NON-NLS-1$
		if (portNumberString == null) {
			abort(LaunchingMessages.SocketAttachConnector_Port_unspecified_for_remote_connection__2, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT); 
		}
		String host = (String)arguments.get("hostname"); //$NON-NLS-1$
		if (host == null) {
			abort(LaunchingMessages.SocketAttachConnector_Hostname_unspecified_for_remote_connection__4, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_HOSTNAME); 
		}
		Map map= connector.defaultArguments();
		
        Connector.Argument param= (Connector.Argument) map.get("hostname"); //$NON-NLS-1$
		param.setValue(host);
		param= (Connector.Argument) map.get("port"); //$NON-NLS-1$
		param.setValue(portNumberString);
        
        String timeoutString = (String)arguments.get("timeout"); //$NON-NLS-1$
        if (timeoutString != null) {
            param= (Connector.Argument) map.get("timeout"); //$NON-NLS-1$
            param.setValue(timeoutString);
        }
        
		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		boolean allowTerminate = false;
		if (configuration != null) {
			allowTerminate = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		}
		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Establishing_connection____2); 
		try {
			VirtualMachine vm = connector.attach(map);
			String vmLabel = constructVMLabel(vm, host, portNumberString, configuration);
			IDebugTarget debugTarget= JDIDebugModel.newDebugTarget(launch, vm, vmLabel, null, allowTerminate, true);
			launch.addDebugTarget(debugTarget);
			subMonitor.worked(1);
			subMonitor.done();
        } catch (TimeoutException e) {
            abort(LaunchingMessages.SocketAttachConnector_0, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (UnknownHostException e) {
			abort(MessageFormat.format(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_because_of_unknown_host____0___1, new String[]{host}), e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		} catch (ConnectException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_as_connection_was_refused_2, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		} catch (IOException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		}
	}

	/**
	 * Helper method that constructs a human-readable label for a remote VM.
	 */
	protected String constructVMLabel(VirtualMachine vm, String host, String port, ILaunchConfiguration configuration) {
		String name = null;
		try {
			name = vm.name();
		} catch (TimeoutException e) {
			// do nothing
		} catch (VMDisconnectedException e) {
			// do nothing
		}
		if (name == null) {
			if (configuration == null) {
				name = ""; //$NON-NLS-1$
			} else {
				name = configuration.getName();
			}
		}
		StringBuffer buffer = new StringBuffer(name);
		buffer.append('['); 
		buffer.append(host);
		buffer.append(':'); 
		buffer.append(port);
		buffer.append(']'); 
		return buffer.toString();
	}
		

	/**
	 * @see IVMConnector#getDefaultArguments()
	 */
	public Map<String, Connector.Argument> getDefaultArguments() throws CoreException {
		Map<String, Connector.Argument> def = getAttachingConnector().defaultArguments();
		Connector.IntegerArgument arg = (Connector.IntegerArgument)def.get("port"); //$NON-NLS-1$
		arg.setValue(8000);
		return def;
	}

	/**
	 * @see IVMConnector#getArgumentOrder()
	 */
	public List<String> getArgumentOrder() {
		List<String> list = new ArrayList<String>(2);
		list.add("hostname"); //$NON-NLS-1$
		list.add("port"); //$NON-NLS-1$
		return list;
	}

}
