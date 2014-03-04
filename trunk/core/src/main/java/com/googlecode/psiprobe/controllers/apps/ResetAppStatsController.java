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

import com.googlecode.psiprobe.session.SessionManager;
import com.googlecode.psiprobe.stats.StatisticsTriggerController;

/**
 *
 * @author Mark Lewis
 */
public class ResetAppStatsController extends NoSelfContextHandlerController {
    protected void executeAction(String contextName, HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String processId = SessionManager.getProcessId(sessionId);
        StatisticsTriggerController.getStatsCollector(processId).getAppStats().reset();
    }

}
