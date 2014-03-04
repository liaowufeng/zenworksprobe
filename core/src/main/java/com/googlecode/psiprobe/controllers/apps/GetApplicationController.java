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

import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.model.Application;
import com.googlecode.psiprobe.session.ProbeSession;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Retrieves Application model object populated with application information.
 * 
 * @author Andy Shapoval
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class GetApplicationController extends ContextHandlerController
{
	private long collectionPeriod;	

    public long getCollectionPeriod() {
        return collectionPeriod;
    }

    public void setCollectionPeriod(long collectionPeriod) {
        this.collectionPeriod = collectionPeriod;
    }

	protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request,
	        HttpServletResponse response) throws Exception
	{
		Application app = null;
		String sessionId = (String) request.getAttribute("sessionId");
		ProbeSession pSession = SessionManager.getProbeSession(sessionId);
		
		try
		{
			app = pSession.getApplication(contextName); 
		}catch(Exception e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
		
		return new ModelAndView(getViewName())
		        .addObject("app", app)
		        .addObject("no_resources",false)
		        .addObject("collectionPeriod", new Long(getCollectionPeriod()));
	}
}
