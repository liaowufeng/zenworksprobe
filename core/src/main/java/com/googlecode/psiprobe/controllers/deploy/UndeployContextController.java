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
package com.googlecode.psiprobe.controllers.deploy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Undeploys a web application.
 * 
 * @author Vlad Ilyushchenko
 * @author Andy Shapoval
 */
public class UndeployContextController extends ContextHandlerController
{
	private String failureViewName = null;
	
	public String getFailureViewName()
	{
		return failureViewName;
	}

	public void setFailureViewName(String failureViewName)
	{
		this.failureViewName = failureViewName;
	}

	protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request,
	        HttpServletResponse response) throws Exception
	{
        try
        {
        	String sessionId = (String) request.getAttribute("sessionId");
            try
            {
                SessionManager.getProbeSession(sessionId).undeployWebApp(contextName);
            }
            catch (ZENworksException e)
            {
                SessionManager.expireSession(sessionId);
                request.setAttribute("errorMessage", e.getMessage());
                return new ModelAndView(getErrorViewName());
            }
        }
        catch (IllegalStateException ie)
        {
            return new ModelAndView(new InternalResourceView(getViewName()));
        }
        return new ModelAndView(new RedirectView(request.getContextPath() + getViewName() + "?"
                + request.getQueryString()));
	}

	protected void executeAction(String contextName) throws Exception
	{
	}
}
