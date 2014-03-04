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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.TomcatContainerController;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.RequestProcessor;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */ 
public class ListConnectorsController extends TomcatContainerController {
    private boolean includeRequestProcessors;
    private long collectionPeriod;

    public long getCollectionPeriod() {
        return collectionPeriod;
    }

    public void setCollectionPeriod(long collectionPeriod) {
        this.collectionPeriod = collectionPeriod;
    }

    public boolean isIncludeRequestProcessors() {
        return includeRequestProcessors;
    }

    public void setIncludeRequestProcessors(boolean includeRequestProcessors) {
        this.includeRequestProcessors = includeRequestProcessors;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
        boolean workerThreadNameSupported = false;
        List connectors = null;
        
        String sessionId = (String) request.getAttribute("sessionId");
        try
        {
        	connectors = SessionManager.getProbeSession(sessionId).getConnectors();
        }catch(ZENworksException e)
		{
			SessionManager.expireSession(sessionId);
			request.setAttribute("errorMessage", e.getMessage());
			return new ModelAndView(getErrorViewName());
		}
 
        if (connectors.size() > 0 && ((Connector)connectors.get(0)).getRequestProcessors().size() > 0) {
            workerThreadNameSupported = ((RequestProcessor)((Connector)connectors.get(0)).getRequestProcessors().get(0)).isWorkerThreadNameSupported();
        }

        return new ModelAndView(getViewName())
                .addObject("connectors", connectors)
                .addObject("workerThreadNameSupported", Boolean.valueOf(workerThreadNameSupported))
                .addObject("collectionPeriod", new Long(getCollectionPeriod()));
    }
}
