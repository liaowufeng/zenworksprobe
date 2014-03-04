/*
 * Copyright (c) 2014 Novell, Inc.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, contact Novell, Inc.
 *
 * To contact Novell about this file by physical or electronic mail,
 * you may find current contact information at www.novell.com 
 *
 * Author   : Lavanya Vankadara
 * Email ID : vlavanya@novell.com
 *
 *  MODIFICATION HISTORY :
 *
 *  Version:    Change description:             Date:       Changed by:
 *
 *  1.0         Initial Version                 2014      Lavanya Vankadara
 *  
*/

package com.googlecode.psiprobe.connections;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import sun.rmi.server.UnicastRef2;
import sun.rmi.transport.LiveRef;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.beans.JMXContainerListenerBean;
import com.googlecode.psiprobe.beans.JMXJvmMemoryInfoAccessorBean;
import com.googlecode.psiprobe.beans.JMXSystemInfoAccessor;
import com.googlecode.psiprobe.beans.JMXThreadDumpCreate;
import com.googlecode.psiprobe.beans.JMXThreadInfoAccessor;
import com.googlecode.psiprobe.controllers.system.JMXHeapDump;
import com.googlecode.psiprobe.model.Application;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.jmx.JMXSystemInformation;
import com.googlecode.psiprobe.model.jmx.RuntimeInformation;
import com.googlecode.psiprobe.session.SessionManager;
import com.googlecode.psiprobe.stats.StatisticsTriggerController;
import com.googlecode.psiprobe.tools.JMXApplicationUtils;
import com.googlecode.psiprobe.tools.JMXRuntimeInfoAccessor;

public class JMXConnection implements Connection, NotificationListener
{
	private MBeanServerConnection conn = null;
	private JMXConnector jmxc = null;
	private String processId = null;
	private JMXContainerListenerBean jmxContainerListener = null;
	JMXJvmMemoryInfoAccessorBean jmxJVMMemInfoBean = null;
	
	private static final String CLOSED = "jmx.remote.connection.closed";
	private static final String FAILED = "jmx.remote.connection.failed";
	
	private boolean connectionClosed = true;
	
//    private static final String rmiServerImplStubClassName = "javax.management.remote.rmi.RMIServerImpl_Stub";
//    private static final Class<? extends Remote> rmiServerImplStubClass;
//    private static RMIServer stub;
	public static final String KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
	
	public static final String KEYSTORE_PASSPHRASE = "ssl.keyStore.passPhrase";
	
	private static Log logger;

    static
    {
    	logger = LogFactory.getLog("JMXConnection");
/*    	Class<? extends Remote> serverStubClass = null;
        try {
              serverStubClass = Class.forName(rmiServerImplStubClassName).asSubclass(Remote.class);
          } catch (ClassNotFoundException e) {
              // should never reach here
              throw (InternalError) new InternalError(e.getMessage()).initCause(e);
          }
          rmiServerImplStubClass = serverStubClass;
*/    }


	@Override
	public List getApplications() throws ZENworksException
	{
		checkConnection();
		return JMXApplicationUtils.getApplications(conn);
	}

	@Override
	public RuntimeInformation getRuntimeInfo() throws ZENworksException
	{
		checkConnection();
		try
		{
			return JMXRuntimeInfoAccessor.getRuntimeInformation(conn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void setConn(MBeanServerConnection conn)
	{
		this.conn = conn;
	}

	public MBeanServerConnection getConn()
	{
		return conn;
	}

	public JMXConnection(String processId) throws ZENworksException
	{
		this.conn = createMBeanServerConnection(processId);
		connectionClosed = false;
		this.processId = processId;
		jmxContainerListener = new JMXContainerListenerBean();
		jmxContainerListener.setConn(conn);
		jmxJVMMemInfoBean = new JMXJvmMemoryInfoAccessorBean();
		jmxJVMMemInfoBean.setConn(conn);
	}

	private MBeanServerConnection createMBeanServerConnection(String processId) throws ZENworksException
	{
		String url = "service:jmx:rmi://"+processId+"/jndi/rmi://"+processId+"/jmxrmi";
		MBeanServerConnection conn = null;
		try
		{
			/*setKeyStorePassword();
			String[] ipAndPort = processId.split(":");
			Registry registry;
			try
			{
				registry = LocateRegistry.getRegistry(ipAndPort[0], Integer.parseInt(ipAndPort[1]) , new SslRMIClientSocketFactory());
				try
				{
					stub = (RMIServer) registry.lookup("jmxrmi");
				}
				catch (NotBoundException nbe)
				{
					throw (IOException) new IOException(nbe.getMessage()).initCause(nbe);
				}
			}
			catch (Exception e)
			{
				registry = LocateRegistry.getRegistry(ipAndPort[0],Integer.parseInt(ipAndPort[1]));
				try
				{
					stub = (RMIServer) registry.lookup("jmxrmi");
				}
				catch (NotBoundException nbe)
				{
					throw (IOException) new IOException(nbe.getMessage()).initCause(nbe);
				}
				e.printStackTrace();
			}
			try
			{
				checkStub(stub, rmiServerImplStubClass);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			jmxc = new RMIConnector(stub, null);
			jmxc.connect();
			MBeanServerConnection conn = jmxc.getMBeanServerConnection();
			jmxc.addConnectionNotificationListener(this, null, null);*/
			HashMap<String, Object> env = new HashMap<String, Object>();
			setKeyStorePassword();
			SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
	        SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
	        
	        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
	        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);

	        // Needed to avoid "non-JRMP server at remote endpoint" error
	        JMXServiceURL target = new JMXServiceURL(url);
	        env.put("com.sun.jndi.rmi.factory.socket", csf);
	        jmxc = JMXConnectorFactory.connect(target, env);
	        conn = jmxc.getMBeanServerConnection();
	        jmxc.addConnectionNotificationListener(this, null, null);

			return conn;
		}
		catch (Exception e)
		{
			try
			{
				JMXServiceURL target = new JMXServiceURL(url);
				jmxc = JMXConnectorFactory.connect(target);
				conn = jmxc.getMBeanServerConnection();
				jmxc.addConnectionNotificationListener(this, null, null);
				return conn;
			}
			catch (Exception e1)
			{
				logger.info("Could not connect to process " + processId);
				logger.info(e.toString());
				//Since we can not connect to that process, we can stop the statistics and then expire all the sessions related to that process
				StatisticsTriggerController.stopStatistics(processId);
				SessionManager.expireAllSession(processId);
				throw new ZENworksException("probe.src.connection.closed");
			}
		}
	}

	private static void setKeyStorePassword()
	{
		if (System.getProperty(KEYSTORE_PASSWORD) == null)
		{
			String passPhrase = System.getProperty(KEYSTORE_PASSPHRASE);
			if (passPhrase != null)
			{

				try
				{
					FileInputStream fstream = new FileInputStream(passPhrase);
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String strLine;
					while ((strLine = br.readLine()) != null)
					{
						System.setProperty(KEYSTORE_PASSWORD, strLine);
					}
					in.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	private static void checkStub(Remote stub, Class<? extends Remote> stubClass)
	{
		if (stub.getClass() != stubClass)
		{

			if (!Proxy.isProxyClass(stub.getClass()))
			{
				throw new SecurityException("Expecting a " + stubClass.getName() + " stub!");
			}
			else
			{
				InvocationHandler handler = Proxy.getInvocationHandler(stub);
				if (handler.getClass() != RemoteObjectInvocationHandler.class)
				{
					throw new SecurityException("Expecting a dynamic proxy instance with a "
					        + RemoteObjectInvocationHandler.class.getName() + " invocation handler!");
				}
				else
				{
					stub = (Remote) handler;
				}
			}
		}
		// Check RemoteRef in stub is from the expected class
		// "sun.rmi.server.UnicastRef2".
		//
		RemoteRef ref = ((RemoteObject) stub).getRef();
		if (ref.getClass() != UnicastRef2.class)
		{
			throw new SecurityException("Expecting a " + UnicastRef2.class.getName() + " remote reference in stub!");
		}
		// Check RMIClientSocketFactory in stub is from the expected class
		// "javax.rmi.ssl.SslRMIClientSocketFactory".
		//
		LiveRef liveRef = ((UnicastRef2) ref).getLiveRef();
		RMIClientSocketFactory csf = liveRef.getClientSocketFactory();
		if (csf == null || csf.getClass() != SslRMIClientSocketFactory.class)
		{
			throw new SecurityException("Expecting a " + SslRMIClientSocketFactory.class.getName()
			        + " RMI client socket factory in stub!");
		}
	}

	@Override
    public void closeConnection()
    {
	    try
        {
	        jmxc.close();
        }
        catch (IOException e)
        {
	        // Can be ignored
        }
	    
    }

	@Override
    public Application getApplication(String contextName) throws ZENworksException
    {
		checkConnection();
		return JMXApplicationUtils.getApplication(contextName, conn, null, false);
    }

	@Override
    public List getMemoryPools() throws ZENworksException
    {
		checkConnection();
		
		jmxJVMMemInfoBean.setConn(conn);
		try
		{
			return jmxJVMMemInfoBean.getPools();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

	@Override
    public List<Application> collectApplicationsServletStats() throws ZENworksException
    {
		checkConnection();
		return JMXApplicationUtils.collectApplicationsServletStats(conn);
    }

	@Override
    public List<Connector> getConnectors() throws ZENworksException
    {
		checkConnection();
		try
		{
			jmxContainerListener.setConn(conn);
			return (jmxContainerListener.getConnectors(true));
		}
		catch (Exception e)
		{
			//ignore this.. For non tomcat processes, connectors should be null;
		}
		return null;
    }

	private void checkConnection() throws ZENworksException
    {
	    if(connectionClosed)
	    {
	    	createMBeanServerConnection(processId);
	    	connectionClosed = false;
	    }	    
    }

	@Override
    public boolean reloadContext(String contextName) throws ZENworksException
    {
		checkConnection();
		return JMXApplicationUtils.reloadApplication(contextName, conn);
	}

	@Override
    public void startContext(String contextName) throws ZENworksException
    {
		checkConnection();
		JMXApplicationUtils.startApplication(contextName, conn);	    
    }

	@Override
    public void stopContext(String contextName) throws ZENworksException
    {
		checkConnection();
	    JMXApplicationUtils.stopApplication(contextName,conn);
    }

	@Override
    public boolean toggleContext(String contextName) throws ZENworksException
    {
		checkConnection();
		return JMXApplicationUtils.toggleApplication(contextName, conn);
    }

	@Override
    public List getServlets(String contextName) throws ZENworksException
    {
		checkConnection();
	    return JMXApplicationUtils.getServlets(contextName, conn);
    }

	@Override
    public List getServletMaps(String contextName) throws ZENworksException
    {
		checkConnection();
		return JMXApplicationUtils.getServletMaps(contextName, conn);
    }

	@Override
    public Connector getConnector(String connectorName) throws ZENworksException
    {
		checkConnection();
		jmxContainerListener.setConn(conn);
	    return JMXApplicationUtils.getConnector(connectorName, jmxContainerListener);
    }

	@Override
    public List getSunthreads() throws ZENworksException
    {
		checkConnection();
		return JMXThreadInfoAccessor.getSunThreads(conn);
    }

	@Override
    public List getThreadStack(long threadID, String threadName, int stackElementCount) throws ZENworksException
    {
		checkConnection();
	    return JMXThreadInfoAccessor.getThreadStack(threadID, threadName, stackElementCount, conn);
    }

	@Override
    public List getThreadPools() throws ZENworksException 
    {
		checkConnection();
		List threadPools = null;
		try
		{
			jmxContainerListener.setConn(conn);
			threadPools = jmxContainerListener.getThreadPools();
		}
		catch (Exception e)
		{
			//ignore this.. For non-tomcat processes, we'll not get thread pools
		}
		return threadPools;
    }

	@Override
    public JMXSystemInformation getSystemInformation() throws ZENworksException
    {
		checkConnection();
		return JMXSystemInfoAccessor.getSystemInformation(conn);
    }

	@Override
    public void adviceGC() throws ZENworksException
    {
		checkConnection();
		JMXRuntimeInfoAccessor.adviceGC(conn);
    }

	@Override
    public void undeployWebApp(String contextName) throws ZENworksException
    {
		checkConnection();
	    JMXApplicationUtils.undeployWebApp(contextName, conn);	    
    }
	
	@Override
    public void handleNotification(Notification notification, Object arg1)
    {
		if (notification.getType().equals(CLOSED) || notification.getType().equals(FAILED))
		{
			connectionClosed = true;
			
			//remove notification listener in the first place
			try
			{
				jmxc.removeConnectionNotificationListener(this, null, null);
			}
			catch (ListenerNotFoundException e)
			{
				//ignore .. nothing to do
			}
			try
            {
	            checkConnection();
	            connectionClosed=false;
            }
            catch (ZENworksException e1)
            {
            	//checkConnection might have tried to add notification listener. Remove this if it does.
    			try
    			{
    				jmxc.removeConnectionNotificationListener(this, null, null);
    			}
    			catch (ListenerNotFoundException e)
    			{
    				//ignore... nothing to do
    			}
            }
		}
    }

	@Override
    public String getProcessId()
    {
	   return processId;
    }

	@Override
    public ModelAndView getXMLView(String contextName, Context context, String displayTarget, String viewName, String downloadUrl) throws ZENworksException
    {
	    checkConnection();
	    try
        {
	        return JMXApplicationUtils.getXMLView(contextName, context, displayTarget, viewName, downloadUrl, conn);
        }
        catch (FileNotFoundException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        catch (IOException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return null;
    }

	@Override
    public File getXMLFile(String contextName, Context context, String downloadTarget) throws ZENworksException
    {
	    checkConnection();
	    try
	    {
	    	return JMXApplicationUtils.getXLMFile(contextName, context, downloadTarget, conn);
	    }catch(Exception e)
	    {
	    	
	    }
		return null;
    }

	@Override
    public File getThreadDumpFile(String processId) throws ZENworksException
    {
	    checkConnection();
	    return JMXThreadDumpCreate.getThreadDumpFile(conn, processId);	    
    }

    @Override
    public String getHeapDumpFile(String processId) throws ZENworksException, Exception
    {
        checkConnection();
        return JMXHeapDump.getHeapDumpFile(conn, processId);
    }
	

}
