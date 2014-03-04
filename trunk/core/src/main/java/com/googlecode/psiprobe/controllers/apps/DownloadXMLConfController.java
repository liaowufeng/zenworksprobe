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

/**
 * Downloads a deployment descriptor (web.xml) or a context descriptor
 * (context.xml) of a web application
 * 
 * @author Andy Shapoval
 */
public class DownloadXMLConfController extends ContextHandlerController {

    /**
     * Type of a configuration file to be downloaded
     */
    private String downloadTarget;

    public String getDownloadTarget() {
        return downloadTarget;
    }

    public void setDownloadTarget(String downloadTarget) {
        this.downloadTarget = downloadTarget;
    }

	protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request,
	        HttpServletResponse response) throws Exception
	{
		File xmlFile = null;
		if (downloadTarget == null)
		{
			throw new RuntimeException("Download target is not set for " + getClass().getName());
		}

		String sessionId = (String) request.getAttribute("sessionId");
		ProbeSession session = SessionManager.getProbeSession(sessionId);
		try
		{
			xmlFile = session.getXMLFile(contextName, context, downloadTarget);
		}catch(ZENworksException e)
		{
			request.setAttribute("errorMessage", e.getMessage());
			SessionManager.expireSession(sessionId);
			return new ModelAndView(getErrorViewName());
		}

		if (xmlFile.exists())
		{
			Utils.sendFile(request, response, xmlFile, true);
		}
		else
		{
			logger.debug("File  of " + contextName + " application does not exists.");
		}
		return null;
	}

}
