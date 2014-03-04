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
package com.googlecode.psiprobe.beans;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import net.sf.javainetlocator.InetAddressLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.RequestProcessor;
import com.googlecode.psiprobe.model.ThreadPool;
import com.googlecode.psiprobe.model.jmx.ThreadPoolObjectName;
import com.googlecode.psiprobe.tools.JmxTools;

/**
 * This class interfaces Tomcat JMX functionality to read connection status. The
 * class essentially provides and maintains the list of connection ThreadPools.
 * 
 * @author vlavanya@novell.com
 */
public class JMXContainerListenerBean implements NotificationListener
{

	private Log logger = LogFactory.getLog(getClass());

	private List poolNames = null;

	private List executorNames = null;

	private MBeanServerConnection conn = null;
	/**
	 * Used to obtain required {@link MBeanServer} instance.
	 */
	public void setConn(MBeanServerConnection conn)
	{
		this.conn = conn;
	}
	
	public MBeanServerConnection getConn()
	{
		return conn;
	}
	
	private boolean isInitialized()
	{
		return poolNames != null && poolNames.size() > 0;
	}

	/**
	 * Finds ThreadPoolObjectName by its string name.
	 * 
	 * @param name
	 *            - pool name
	 * 
	 * @return null if the input name is null or ThreadPoolObjectName is not
	 *         found
	 */
	private ThreadPoolObjectName findPool(String name)
	{
		if (name != null && isInitialized())
		{
			for (Iterator it = poolNames.iterator(); it.hasNext();)
			{
				ThreadPoolObjectName threadPoolObjectName = (ThreadPoolObjectName) it.next();
				if (name.equals(threadPoolObjectName.getThreadPoolName().getKeyProperty("name")))
				{
					return threadPoolObjectName;
				}
			}
		}
		return null;
	}

	/**
	 * Handles creation and deletion of new "worker" threads.
	 */
	public synchronized void handleNotification(Notification notification, Object object)
	{
		if (notification instanceof MBeanServerNotification)
		{
			ObjectName objectName = ((MBeanServerNotification) notification).getMBeanName();

			if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
			{

				if ("RequestProcessor".equals(objectName.getKeyProperty("type")))
				{
					ThreadPoolObjectName threadPoolObjectName = findPool(objectName.getKeyProperty("worker"));
					if (threadPoolObjectName != null)
					{
						threadPoolObjectName.getRequestProcessorNames().add(objectName);
					}
				}

			}
			else if (notification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
			{

				if ("RequestProcessor".equals(objectName.getKeyProperty("type")))
				{
					ThreadPoolObjectName threadPoolObjectName = findPool(objectName.getKeyProperty("worker"));
					if (threadPoolObjectName != null)
					{
						threadPoolObjectName.getRequestProcessorNames().remove(objectName);
					}
				}
			}
		}
	}

	/**
	 * Load ObjectNames for the relevant MBeans so they can be queried at a
	 * later stage without searching MBean server over and over again.
	 * 
	 * @throws Exception
	 *             - this method does not handle any of the exceptions that may
	 *             be thrown when querying MBean server.
	 */
	private synchronized void initialize() throws Exception
	{

		String serverName = "Catalina";
		if (conn != null)
		{
			Set threadPools = conn.queryMBeans(new ObjectName(serverName + ":type=ThreadPool,*"), null);
			poolNames = new ArrayList(threadPools.size());
			for (Iterator it = threadPools.iterator(); it.hasNext();)
			{

				ThreadPoolObjectName threadPoolObjectName = new ThreadPoolObjectName();
				ObjectName threadPoolName = ((ObjectInstance) it.next()).getObjectName();

				String name = threadPoolName.getKeyProperty("name");

				threadPoolObjectName.setThreadPoolName(threadPoolName);
				ObjectName grpName = conn.getObjectInstance(
				        new ObjectName(threadPoolName.getDomain() + ":type=GlobalRequestProcessor,name=" + name))
				        .getObjectName();
				threadPoolObjectName.setGlobalRequestProcessorName(grpName);

				//
				// unfortunately exact workers could not be found at the time of
				// testing
				// so we filter out the relevant workers within the loop
				//
				Set workers = conn.queryMBeans(new ObjectName(threadPoolName.getDomain() + ":type=RequestProcessor,*"),
				        null);

				for (Iterator wrkIt = workers.iterator(); wrkIt.hasNext();)
				{
					ObjectName wrkName = ((ObjectInstance) wrkIt.next()).getObjectName();
					if (name.equals(wrkName.getKeyProperty("worker")))
					{
						threadPoolObjectName.getRequestProcessorNames().add(wrkName);
					}
				}

				poolNames.add(threadPoolObjectName);
			}

			Set executors = conn.queryMBeans(new ObjectName(serverName + ":type=Executor,*"), null);
			executorNames = new ArrayList(executors.size());
			for (Iterator it = executors.iterator(); it.hasNext();)
			{
				ObjectName executorName = ((ObjectInstance) it.next()).getObjectName();
				executorNames.add(executorName);
			}

			// Register with MBean server
			conn.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this, null, null);
		}

	}

	public synchronized List getThreadPools() throws Exception
	{
		System.out.println("JMXContainerListenerBean --- Getting Threadpools");

		if (!isInitialized())
		{
			initialize();
		}

		List threadPools = new ArrayList(poolNames.size());

		if (conn != null)
		{
			System.out.println("Ip not null.. Reading from jmx");

			for (Iterator it = executorNames.iterator(); it.hasNext();)
			{
				ObjectName executorName = (ObjectName) it.next();

				ThreadPool threadPool = new ThreadPool();
				threadPool.setName(executorName.getKeyProperty("name"));
				threadPool.setMaxThreads(JmxTools.getIntAttr(conn, executorName, "maxThreads"));
				threadPool.setMaxSpareThreads(JmxTools.getIntAttr(conn, executorName, "largestPoolSize"));
				threadPool.setMinSpareThreads(JmxTools.getIntAttr(conn, executorName, "minSpareThreads"));
				threadPool.setCurrentThreadsBusy(JmxTools.getIntAttr(conn, executorName, "activeCount"));
				threadPool.setCurrentThreadCount(JmxTools.getIntAttr(conn, executorName, "poolSize"));

				threadPools.add(threadPool);
			}

			for (Iterator it = poolNames.iterator(); it.hasNext();)
			{

				ThreadPoolObjectName threadPoolObjectName = (ThreadPoolObjectName) it.next();
				try
				{
					ObjectName poolName = threadPoolObjectName.getThreadPoolName();

					ThreadPool threadPool = new ThreadPool();
					threadPool.setName(poolName.getKeyProperty("name"));
					threadPool.setMaxThreads(JmxTools.getIntAttr(conn, poolName, "maxThreads"));

					if (JmxTools.hasAttribute(conn, poolName, "maxSpareThreads"))
					{
						threadPool.setMaxSpareThreads(JmxTools.getIntAttr(conn, poolName, "maxSpareThreads"));
						threadPool.setMinSpareThreads(JmxTools.getIntAttr(conn, poolName, "minSpareThreads"));
					}

					threadPool.setCurrentThreadsBusy(JmxTools.getIntAttr(conn, poolName, "currentThreadsBusy"));
					threadPool.setCurrentThreadCount(JmxTools.getIntAttr(conn, poolName, "currentThreadCount"));

					// Tomcat 6.0.21+ will return -1 for maxThreads if the
					// connector uses an executor for its threads.
					// In this case, don't add its ThreadPool to the results.
					if (threadPool.getMaxThreads() > -1)
					{
						threadPools.add(threadPool);
					}
				}
				catch (InstanceNotFoundException e)
				{
					logger.error("Failed to query entire thread pool " + threadPoolObjectName);
					logger.debug("  Stack trace:", e);
				}
			}
		}
		return threadPools;
	}

	public synchronized List getConnectors(boolean includeRequestProcessors) throws Exception
	{
		boolean workerThreadNameSupported = true;

		if (!isInitialized())
		{
			initialize();
		}

		List connectors = new ArrayList(poolNames.size());

		if (conn != null)
		{

			for (Iterator it = poolNames.iterator(); it.hasNext();)
			{

				ThreadPoolObjectName threadPoolObjectName = (ThreadPoolObjectName) it.next();
				boolean remoteAddrAvailable = true;
				try
				{
					ObjectName poolName = threadPoolObjectName.getThreadPoolName();

					Connector connector = new Connector();
					connector.setName(poolName.getKeyProperty("name"));

					ObjectName grpName = threadPoolObjectName.getGlobalRequestProcessorName();

					connector.setMaxTime(JmxTools.getLongAttr(conn, grpName, "maxTime"));
					connector.setProcessingTime(JmxTools.getLongAttr(conn, grpName, "processingTime"));
					connector.setBytesReceived(JmxTools.getLongAttr(conn, grpName, "bytesReceived"));
					connector.setBytesSent(JmxTools.getLongAttr(conn, grpName, "bytesSent"));
					connector.setRequestCount(JmxTools.getIntAttr(conn, grpName, "requestCount"));
					connector.setErrorCount(JmxTools.getIntAttr(conn, grpName, "errorCount"));

					if (includeRequestProcessors)
					{
						for (Iterator wrkIt = threadPoolObjectName.getRequestProcessorNames().iterator(); wrkIt
						        .hasNext();)
						{
							ObjectName wrkName = (ObjectName) wrkIt.next();

							try
							{
								RequestProcessor rp = new RequestProcessor();
								rp.setName(wrkName.getKeyProperty("name"));
								rp.setStage(JmxTools.getIntAttr(conn, wrkName, "stage"));
								rp.setProcessingTime(JmxTools.getLongAttr(conn, wrkName, "requestProcessingTime"));
								rp.setBytesSent(JmxTools.getLongAttr(conn, wrkName, "requestBytesSent"));
								rp.setBytesReceived(JmxTools.getLongAttr(conn, wrkName, "requestBytesReceived"));
								try
								{
									String remoteAddr = JmxTools.getStringAttr(conn, wrkName, "remoteAddr");
									rp.setRemoteAddr(remoteAddr);
									rp.setRemoteAddrLocale(InetAddressLocator.getLocale(InetAddress.getByName(
									        remoteAddr).getAddress()));
								}
								catch (RuntimeOperationsException ex)
								{
									/*
									 * if it's not available for this request
									 * processor, then it's not available for
									 * any request processor in this thread pool
									 */
									remoteAddrAvailable = false;
								}
								rp.setVirtualHost(JmxTools.getStringAttr(conn, wrkName, "virtualHost"));
								rp.setMethod(JmxTools.getStringAttr(conn, wrkName, "method"));
								rp.setCurrentUri(JmxTools.getStringAttr(conn, wrkName, "currentUri"));
								rp.setCurrentQueryString(JmxTools.getStringAttr(conn, wrkName, "currentQueryString"));
								rp.setProtocol(JmxTools.getStringAttr(conn, wrkName, "protocol"));

								// Relies on
								// https://issues.apache.org/bugzilla/show_bug.cgi?id=41128
								if (workerThreadNameSupported
								        && JmxTools.hasAttribute(conn, wrkName, "workerThreadName"))
								{
									rp.setWorkerThreadName(JmxTools.getStringAttr(conn, wrkName, "workerThreadName"));
									rp.setWorkerThreadNameSupported(true);
								}
								else
								{
									//
									// attribute should consistently either
									// exist or
									// be missing across all the workers so
									// it does not make sense to check attribute
									// existence
									// if we have found once that it is not
									// supported
									//
									rp.setWorkerThreadNameSupported(false);
									workerThreadNameSupported = false;
								}
								connector.addRequestProcessor(rp);
							}
							catch (InstanceNotFoundException e)
							{
								logger.info("Failed to query RequestProcessor " + wrkName);
								logger.debug("  Stack trace:", e);
							}
						}
					}

					connectors.add(connector);
				}
				catch (InstanceNotFoundException e)
				{
					logger.error("Failed to query entire thread pool " + threadPoolObjectName);
					logger.debug("  Stack trace:", e);
				}
			}
		}
		return connectors;
	}

}
