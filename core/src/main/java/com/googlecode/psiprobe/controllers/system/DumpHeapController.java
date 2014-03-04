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

package com.googlecode.psiprobe.controllers.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.session.ProbeSession;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Downloads a deployment descriptor (web.xml) or a context descriptor
 * (context.xml) of a web application
 * 
 */
public class DumpHeapController extends ParameterizableViewController
{

	@Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
		try
		{
			String dumpFileName = null;

			String sessionId = (String) request.getAttribute("sessionId");
			ProbeSession session = SessionManager.getProbeSession(sessionId);
			String processId = SessionManager.getProcessId(sessionId);
			try
			{
			    dumpFileName=session.getHeapDumpFile(processId);
			    request.setAttribute("dumpFile", dumpFileName);
			    request.setAttribute("remoteProcessIP",processId.split(":")[0]);
			    return new ModelAndView(getViewName(),"dumpFile",dumpFileName);
			    
			}
			catch (ZENworksException e)
			{
				request.setAttribute("errorMessage", e.getMessage());
				SessionManager.expireSession(sessionId);
				return new ModelAndView(getViewName(),"filecreated",false);
			}
			catch(Exception e)
			{
			    request.setAttribute("errorMessage", "could not create heapdump");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		return null;

    }

}
