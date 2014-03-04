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
package com.googlecode.psiprobe.controllers.apps;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Reloads application context.
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class AjaxReloadContextController extends ContextHandlerController {
	
	protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request,
	        HttpServletResponse response) throws Exception
	{
	    
		boolean available = false;
        if (!request.getContextPath().equals(contextName))
        {

            String sessionId = (String) request.getAttribute("sessionId");
            try
            {
                available = SessionManager.getProbeSession(sessionId).reloadApplication(contextName);
            }
            catch (ZENworksException e)
            {
                SessionManager.expireSession(sessionId);
                request.setAttribute("errorMessage", e.getMessage());
                return new ModelAndView(getErrorViewName());
            }

            return new ModelAndView(getViewName(), "available", Boolean.valueOf(available));
        }
		return new ModelAndView(getViewName(), "available", true);
	}

}
