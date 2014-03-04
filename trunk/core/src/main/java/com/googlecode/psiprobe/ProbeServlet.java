/*
\ * Licensed under the GPL License.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.googlecode.psiprobe;

import java.text.ParseException;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Wrapper;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.web.servlet.DispatcherServlet;

import com.googlecode.psiprobe.beans.ContainerWrapperBean;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.session.SessionManager;


/**
 * Main dispatcher servlet. Spring default dispatcher servlet had to be
 * superceeded to handle "privileged" application context features. The actual
 * requirement is to capture passed Wrapper instance into ContainerWrapperBean.
 * Wrapper instance is our gateway to Tomcat.
 * 
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 */
public class ProbeServlet extends DispatcherServlet implements ContainerServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
	private Wrapper wrapper;
	public static final String KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
	
	public static final String KEYSTORE_PASSPHRASE = "ssl.keyStore.passPhrase";

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (wrapper != null) 
        {
        	getContainerWrapperBean().setWrapper(wrapper);
        	logger.info("Schedule session cleaning job");
        	MethodInvokingJobDetailFactoryBean clearSessionsJobDetail = new MethodInvokingJobDetailFactoryBean();
        	clearSessionsJobDetail.setTargetClass(com.googlecode.psiprobe.session.SessionManager.class);
        	clearSessionsJobDetail.setStaticMethod("com.googlecode.psiprobe.session.SessionManager.cleanUpSessions");
        	clearSessionsJobDetail.setConcurrent(false);
        	clearSessionsJobDetail.setName("cleanProbeSessions");
    		try
            {
    			clearSessionsJobDetail.afterPropertiesSet();
    	        logger.debug("calling afterproperties set on jobdetail");
            }
            catch (ClassNotFoundException e)
            {
    	        logger.info(Utils.getStackTrace(e));
            }
            catch (NoSuchMethodException e)
            {
    	        // TODO Auto-generated catch block
            	logger.info(Utils.getStackTrace(e));
            }
            
            SimpleTriggerBean triggerCleanPSessions = new SimpleTriggerBean();
            triggerCleanPSessions.setBeanName("CleanPSessions");
            triggerCleanPSessions.setJobDetail((JobDetail)clearSessionsJobDetail.getObject());
            triggerCleanPSessions.setStartDelay(50000);
            triggerCleanPSessions.setRepeatInterval(50000);
            try
            {
	            triggerCleanPSessions.afterPropertiesSet();
            }
            catch (ParseException e)
            {
            	logger.info(Utils.getStackTrace(e));
            }
            Scheduler scheduler = (Scheduler) getWebApplicationContext().getBean("scheduler"); 
            try
            {
	            scheduler.scheduleJob((JobDetail) clearSessionsJobDetail.getObject(),triggerCleanPSessions);
	            logger.info("Session cleanup is scheduled");
            }
            catch (SchedulerException e)
            {
            	logger.info(Utils.getStackTrace(e));
            }

        }
        else 
        {
            throw new ServletException("Wrapper is null");
        }
    }

    protected void doDispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		httpServletRequest.setCharacterEncoding("UTF-8");
		String sessionId = httpServletRequest.getParameter("sessionId");
		String processId = httpServletRequest.getParameter("processId");
		SessionManager.setAppContext(getWebApplicationContext());

		try
		{
			if ((sessionId == null || sessionId == ""))
			{
				if (processId == "" || processId == null)
				{
					throw new ZENworksException("probe.src.processId.isEmpty");
				}
				sessionId = UUID.randomUUID().toString();
				logger.debug("New session created with sessionId " + sessionId);
				SessionManager.createSession(sessionId, processId);

				httpServletRequest.setAttribute("sessionId", sessionId);
			}
			else
			{
				httpServletRequest.setAttribute("sessionId", sessionId);
				logger.debug("Updating the session " + sessionId + " with the current time stamp");
				SessionManager.updateSession(sessionId);
			}
			if((SessionManager.getProbeSession(sessionId)) == null){
				throw new ZENworksException("probe.src.session.expired");
			}
			else{
				httpServletRequest.setAttribute("connType", SessionManager.getProbeSession(sessionId).getConnType().getDisplayName());
			}
		}
		catch (ZENworksException e)
		{
			httpServletRequest.setAttribute("errorMessage", e.getMessage());
			RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher("/error.jsp");
			dispatcher.forward( httpServletRequest, httpServletResponse );
			return;
		}

		super.doDispatch(httpServletRequest, httpServletResponse);
	}
    
    public void destroy() {
        getContainerWrapperBean().setWrapper(null);
        super.destroy();
    }

    protected ContainerWrapperBean getContainerWrapperBean() {
        return (ContainerWrapperBean) getWebApplicationContext().getBean("containerWrapper");
    }

}
