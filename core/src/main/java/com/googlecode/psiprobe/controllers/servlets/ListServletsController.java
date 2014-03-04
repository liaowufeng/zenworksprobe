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
package com.googlecode.psiprobe.controllers.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Retrieves a list of servlets for a particular web application or for all
 * applications if an application name is not passed in a query string.
 * 
 * @author Andy Shapoval
 */
public class ListServletsController extends ContextHandlerController
{

	protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request,
	        HttpServletResponse response) throws Exception
	{
		List servlets;
		String sessionId = (String) request.getAttribute("sessionId");
		try
		{
			servlets = SessionManager.getProbeSession(sessionId).getServlets(contextName);		
		}catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
        return new ModelAndView(getViewName(), "servlets", servlets);
    }

	protected boolean isContextOptional()
	{
		return true;
	}
}
