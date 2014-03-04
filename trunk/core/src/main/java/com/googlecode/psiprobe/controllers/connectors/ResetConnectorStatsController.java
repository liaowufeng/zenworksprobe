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
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import com.googlecode.psiprobe.session.SessionManager;
import com.googlecode.psiprobe.stats.StatisticsTriggerController;

/**
 *
 * @author Mark Lewis
 */
public class ResetConnectorStatsController extends ParameterizableViewController {


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String connectorName = ServletRequestUtils.getRequiredStringParameter(request, "cn");
        String sessionId = (String) request.getAttribute("sessionId");
        String processId = SessionManager.getProcessId(sessionId);
        StatisticsTriggerController.getStatsCollector(processId).getConnectorStats().reset(connectorName);

        return new ModelAndView(new RedirectView(request.getContextPath() + getViewName() + "?" + request.getQueryString()));
    }

}
