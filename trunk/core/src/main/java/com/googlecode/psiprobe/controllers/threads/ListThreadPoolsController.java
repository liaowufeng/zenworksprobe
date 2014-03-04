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
package com.googlecode.psiprobe.controllers.threads;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.TomcatContainerController;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Creates the list of http connection thread pools.
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class ListThreadPoolsController extends TomcatContainerController {

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception 
    {
  
    	String sessionId = (String) request.getAttribute("sessionId");    	
    	List pools = null;
    	try
    	{
    		pools = SessionManager.getProbeSession(sessionId).getThreadPools();
    	}catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
 
    	return new ModelAndView(getViewName())
                .addObject("pools", pools);
    }
}
