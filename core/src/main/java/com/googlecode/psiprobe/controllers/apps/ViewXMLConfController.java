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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.controllers.ContextHandlerController;
import com.googlecode.psiprobe.session.ProbeSession;
import com.googlecode.psiprobe.session.SessionManager;

/**
 * Displays a deployment descriptor (web.xml) or a context descriptor
 * (context.xml) of a web application
 * 
 * @author Andy Shapoval
 * @author Vlad Ilyushchenko
 */
public class ViewXMLConfController extends ContextHandlerController {
    public static final String TARGET_WEB_XML = "web.xml";
    public static final String TARGET_CONTEXT_XML = "context.xml";

    /**
     * Type of a file to be displayed
     */
    private String displayTarget;
    private MBeanServerConnection conn = null;

    /**
     * Url that will be used in the view to download the file
     */
    private String downloadUrl;

    public String getDisplayTarget() {
        return displayTarget;
    }

    public void setDisplayTarget(String displayTarget) {
        this.displayTarget = displayTarget;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    protected ModelAndView handleContext(String contextName, Context context, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mv;
        if (displayTarget == null) {
            throw new RuntimeException("Display target is not set for " + getClass().getName());
        }
        
        String sessionId = (String) request.getAttribute("sessionId");
        ProbeSession session = SessionManager.getProbeSession(sessionId);
        try
        {
        	mv = session.getXMLView(contextName, context, displayTarget, getViewName(), downloadUrl);
        }catch(ZENworksException e)
        {
        	request.setAttribute("errorMessage",e.getMessage());
        	SessionManager.expireSession(sessionId);
        	return new ModelAndView(getErrorViewName());
        }

        return mv;	
    }

}
