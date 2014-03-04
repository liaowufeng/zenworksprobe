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

import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Reloads application context.
 * 
 * @author Vlad Ilyushchenko
 */
public class ReloadContextController extends NoSelfContextHandlerController {
    protected void executeAction(String contextName, HttpServletRequest request) throws Exception 
    {
    	String sessionId = (String) request.getAttribute("sessionId");
    	try
    	{
    		SessionManager.getProbeSession(sessionId).reloadApplication(contextName);
    	}catch(ZENworksException e)
    	{
    		SessionManager.expireSession(sessionId);
    		throw new ZENworksException(e.getMessage());
    	}
    }
}
