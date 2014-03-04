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

package com.googlecode.psiprobe.session;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.Connection;
import com.googlecode.psiprobe.connections.ConnectionType;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.connections.JMXConnection;
import com.googlecode.psiprobe.model.Application;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.jmx.JMXSystemInformation;

public class ProbeSession
{
	private ConnectionType connType;
	
	private String sessionId;
	
	private String processId;
	
	private Connection connection;
	
	private Date lastRequesteddate;

	public void setConnType(ConnectionType connType)
    {
	    this.connType = connType;
    }

	public ConnectionType getConnType()
    {
	    return connType;
    }

	public void setSessionId(String sessionId)
    {
	    this.sessionId = sessionId;
    }

	public String getSessionId()
    {
	    return sessionId;
    }

	public void setProcessId(String processId)
    {
	    this.processId = processId;
    }

	public String getProcessId()
    {
	    return processId;
    }

	public void setConnection(Connection connection)
    {
	    this.connection = connection;
    }

	public Connection getConnection()
    {
	    return connection;
    }

	public void setLastRequesteddate(Date lastRequesteddate)
    {
	    this.lastRequesteddate = lastRequesteddate;
    }

	public Date getLastRequesteddate()
    {
	    return lastRequesteddate;
    }
	
	public List getApplications() throws ZENworksException
	{
		return connection.getApplications();
	}
	
	public ProbeSession(String processId, String sessionId, ConnectionType connType) throws ZENworksException
	{
		this.processId = processId;
		this.sessionId = sessionId;
		this.connType = connType;
		connection = new JMXConnection(processId);
		lastRequesteddate = new Date();
	}
	
	public void expire()
	{
		try
		{
			connection.closeConnection();
		}catch(Exception e)
		{
			//Ignore this..
		}
	}

	public Application getApplication(String contextName) throws ZENworksException
    {
		return connection.getApplication(contextName);
    }
	
	public boolean reloadApplication(String contextName) throws ZENworksException
	{
		return connection.reloadContext(contextName);
	}
	
	public boolean toggleApplication(String contextName) throws ZENworksException
	{
		return connection.toggleContext(contextName);
	}
	
	public void stopApplication(String contextName) throws ZENworksException
	{
		connection.stopContext(contextName);
	}
	
	public void startApplication(String contextName) throws ZENworksException
	{
		connection.startContext(contextName);
	}
	
	public List getServlets(String contextName) throws ZENworksException
	{
		return connection.getServlets(contextName);
	}
	
	public List getServletMaps(String contextName) throws ZENworksException
	{
		return connection.getServletMaps(contextName);
	}

	public Connector getConnector(String connectorName) throws ZENworksException
    {
	    return connection.getConnector(connectorName);
    }

	public List getConnectors() throws ZENworksException
    {
	    return connection.getConnectors();
    }

	public List getSunThreads() throws ZENworksException
    {
	    return connection.getSunthreads();
    }

	public List getThreadStack(long threadID, String threadName, int stackElementCount) throws ZENworksException
    {
	    return connection.getThreadStack(threadID, threadName, stackElementCount);
	    
    }

	public List getThreadPools() throws ZENworksException
    {
	    return connection.getThreadPools();
    }

	public JMXSystemInformation getSystemInformation() throws ZENworksException
    {
	    return connection.getSystemInformation();
    }

	public Object getRuntimeInformation() throws ZENworksException
    {
		return connection.getRuntimeInfo();
    }

	public List getMemoryPools() throws ZENworksException
    {
		return connection.getMemoryPools();
    }

	public void adviceGC() throws ZENworksException
    {
	    connection.adviceGC();	    
    }

	public void undeployWebApp(String contextName) throws ZENworksException
    {
	    connection.undeployWebApp(contextName);
	    
    }

	public ModelAndView getXMLView(String contextName, Context context, String displayTarget, String viewName, String downloadUrl) throws ZENworksException
    {
	    return connection.getXMLView(contextName, context, displayTarget, viewName, downloadUrl);
    }

	public File getXMLFile(String contextName, Context context, String downloadTarget) throws ZENworksException
    {
	    return connection.getXMLFile(contextName, context, downloadTarget);
    }

	public File getThreadDumpFile(String processId) throws ZENworksException
    {
	    return connection.getThreadDumpFile(processId);
    }

    public String getHeapDumpFile(String processId) throws ZENworksException, Exception
    {
        return connection.getHeapDumpFile(processId);
    }


}
