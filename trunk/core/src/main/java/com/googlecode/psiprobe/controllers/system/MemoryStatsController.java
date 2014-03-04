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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class MemoryStatsController extends ParameterizableViewController {
    private long collectionPeriod;
    private String errorViewName;

	public long getCollectionPeriod()
	{
		return collectionPeriod;
	}

    public void setCollectionPeriod(long collectionPeriod) {
        this.collectionPeriod = collectionPeriod;
    }

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	        throws Exception
	{
		List memoryPools;
		String sessionId = (String) request.getAttribute("sessionId");
		try
		{
			memoryPools = SessionManager.getProbeSession(sessionId).getMemoryPools();
		}catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
		return new ModelAndView(getViewName()).addObject("pools", memoryPools).addObject("collectionPeriod",
		        new Long(getCollectionPeriod()));
	}

	public void setErrorViewName(String errorViewName)
    {
	    this.errorViewName = errorViewName;
    }

	public String getErrorViewName()
    {
	    return errorViewName;
    }
}
