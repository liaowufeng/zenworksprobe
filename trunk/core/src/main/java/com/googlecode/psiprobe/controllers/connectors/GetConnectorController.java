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
package com.googlecode.psiprobe.controllers.connectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.TomcatContainerController;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */ 
public class GetConnectorController extends TomcatContainerController {

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String connectorName = ServletRequestUtils.getStringParameter(request, "cn", null);
        Connector connector = null;
        String sessionId = (String) request.getAttribute("sessionId");
        try
        {
        	connector = SessionManager.getProbeSession(sessionId).getConnector(connectorName);
        }catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
        
        return new ModelAndView(getViewName(), "connector", connector);
    }
}
