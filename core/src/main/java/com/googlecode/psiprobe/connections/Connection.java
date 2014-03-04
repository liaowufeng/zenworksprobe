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
 *  FILENAME    :   Connection.java
 *
 *  DESCRIPTION :   This is the interface which provides the required information 
 *  from the remote process
 *
 *  MODIFICATION HISTORY :
 *
 *  Version:    Change description:             Date:       Changed by:
 *
 *  1.0         Initial Version                 2014      Lavanya Vankadara
 *  
*/


package com.googlecode.psiprobe.connections;

import java.io.File;
import java.util.List;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.model.Application;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.jmx.JMXSystemInformation;
import com.googlecode.psiprobe.model.jmx.RuntimeInformation;

public interface Connection
{
	public List getApplications() throws ZENworksException;
	
	public RuntimeInformation getRuntimeInfo() throws ZENworksException;
	
	public void closeConnection();
	
	public Application getApplication(String contextName) throws ZENworksException;

	public List getMemoryPools() throws ZENworksException;
	
	public List<Application> collectApplicationsServletStats() throws ZENworksException;
	
	public List<Connector> getConnectors() throws ZENworksException;
	
	public boolean reloadContext(String contextName) throws ZENworksException;
	
	public void startContext(String contextName) throws ZENworksException;
	
	public void stopContext(String contextName) throws ZENworksException;
	
	public boolean toggleContext(String contextName) throws ZENworksException;
	
	public List getServlets(String contextName) throws ZENworksException;

	public List getServletMaps(String contextName) throws ZENworksException;
	
	public Connector getConnector(String connectorName) throws ZENworksException;

	public List getSunthreads() throws ZENworksException;

	public List getThreadStack(long threadID, String threadName, int stackElementCount) throws ZENworksException;

	public List getThreadPools() throws ZENworksException;

	public JMXSystemInformation getSystemInformation() throws ZENworksException;

	public void adviceGC() throws ZENworksException;

	public void undeployWebApp(String contextName) throws ZENworksException;

	public String getProcessId();

	public ModelAndView getXMLView(String contextName, Context context, String displayTarget, String viewName, String downloadUrl) throws ZENworksException;

	public File getXMLFile(String contextName, Context context, String downloadTarget) throws ZENworksException;

	public File getThreadDumpFile(String processId) throws ZENworksException;

    public String getHeapDumpFile(String processId) throws ZENworksException, Exception;
	
}
