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
 *  MODIFICATION HISTORY :
 *
 *  Version:    Change description:             Date:       Changed by:
 *
 *  1.0         Initial Version                 2014      Lavanya Vankadara
 *  
*/

package com.googlecode.psiprobe.controllers.threads;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.session.ProbeSession;
import com.googlecode.psiprobe.session.SessionManager;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Downloads a deployment descriptor (web.xml) or a context descriptor
 * (context.xml) of a web application
 * 
 * @author vlavanya@novell.com
 */
public class DumpAllThreadsController extends AbstractController
{
	private String errorViewName; 

	@Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
		try
		{
			File dumpFile = null;

			String sessionId = (String) request.getAttribute("sessionId");
			ProbeSession session = SessionManager.getProbeSession(sessionId);
			String processId = SessionManager.getProcessId(sessionId);
			try
			{
				dumpFile = session.getThreadDumpFile(processId);
			}
			catch (ZENworksException e)
			{
				request.setAttribute("errorMessage", e.getMessage());
				SessionManager.expireSession(sessionId);
				return new ModelAndView(getErrorViewName());
			}

			if (dumpFile.exists())
			{
				Utils.sendFile(request, response, dumpFile, true);
			}
			else
			{
				logger.debug("Could not create thread dump.");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		return null;

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
