/*
 * Licensed under the GPL License.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.googlecode.psiprobe.controllers.system;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.TomcatContainerController;
import com.googlecode.psiprobe.model.jmx.JMXSystemInformation;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Creates an instance of SystemInformation POJO.
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class SysInfoController extends TomcatContainerController
{

	private List filterOutKeys = new ArrayList();

	private long collectionPeriod;

	public long getCollectionPeriod()
	{
		return collectionPeriod;
	}
	
	public List getFilterOutKeys()
	{
		return filterOutKeys;
	}
	
	public void setFilterOutKeys(List filterKeys)
	{
		filterOutKeys = filterKeys;
	}

	public void setCollectionPeriod(long collectionPeriod)
	{
		this.collectionPeriod = collectionPeriod;
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	        throws Exception
	{
		JMXSystemInformation systemInformation;
		String sessionId = (String) request.getAttribute("sessionId");
		try
		{
			systemInformation = SessionManager.getProbeSession(sessionId).getSystemInformation();
		}catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
		
		return new ModelAndView(getViewName()).addObject("systemInformation", systemInformation)
				        .addObject("runtime", SessionManager.getProbeSession(sessionId).getRuntimeInformation())
				        .addObject("collectionPeriod", new Long(getCollectionPeriod()));

	}
}
