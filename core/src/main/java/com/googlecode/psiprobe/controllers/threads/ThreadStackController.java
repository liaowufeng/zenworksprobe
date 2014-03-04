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

import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.session.SessionManager;

public class ThreadStackController extends ParameterizableViewController
{
	private int stackElementCount = 20;
	private String errorViewName;
	
    public int getStackElementCount() {
        return stackElementCount;
    }

    public void setStackElementCount(int stackElementCount) {
        this.stackElementCount = stackElementCount;
    }

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	        throws Exception
	{

        long threadID = ServletRequestUtils.getLongParameter(request, "id", -1);
        String threadName = ServletRequestUtils.getStringParameter(request, "name", null);

		List stack = null;
		String sessionId = (String) request.getAttribute("sessionId");
		try
		{		
			stack = SessionManager.getProbeSession(sessionId).getThreadStack(threadID, threadName, stackElementCount);			
		}catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}

		return new ModelAndView(getViewName(), "stack", stack).addObject("threadName", threadName);
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
