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

package com.googlecode.psiprobe.session;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;

import com.googlecode.psiprobe.connections.ConnectionType;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.stats.StatisticsTriggerController;
import com.googlecode.psiprobe.tools.TimeExpression;

public class SessionManager
{
	private static Map<String, ProbeSession> sessions= new ConcurrentHashMap<String,ProbeSession>();
	
	private static Map<String, ArrayList<String>> sessionMap = new ConcurrentHashMap<String, ArrayList<String>>();
	private static long expiryPeriod = 30;
	private static Properties conf;
	private static WebApplicationContext appContext;
	
	
	private static Log logger;
	
	static
	{
		logger = LogFactory.getLog(SessionManager.class.getClass());
	}
	
	public static void setSessions(Map<String, ProbeSession> sessions)
    {
	    SessionManager.sessions = sessions;
    }

	public static Map<String, ProbeSession> getSessions()
    {
	    return sessions;
    }
	
	public static void updateSession(String sessionId) throws ZENworksException
	{
		ProbeSession pSession = sessions.get(sessionId);
		if(pSession == null)
		{			
			throw new ZENworksException("probe.src.session.expired");
		}
		pSession.setLastRequesteddate(new Date());
		sessions.put(sessionId, pSession);
	}
	
	public static synchronized void expireSession(String sessionId) 
	{
		ProbeSession pSession = sessions.get(sessionId);
		
			if (pSession != null) {
				String processId = pSession.getProcessId();
				stopStatsCollection(processId, sessionId);
				sessions.remove(sessionId);
			}
	}
	
	public static synchronized void expireAllSession(String processId){
		ArrayList<String> SessionList = SessionManager.getSessionMap().get(processId);
		if(SessionList != null){
			for(String sessionId : SessionList){
				try{
					SessionManager.expireSession(sessionId);
				}catch (Exception e2) {
				}
			}
		}
		if(sessionMap.get(processId) == null){
			sessionMap.remove(processId);
		}
	}
	
	public static synchronized void cleanUpSessions() 
	{
		Date dateNow = new Date();
		logger.debug("Cleaning up the expired sessions");
		Set<String> keySet = sessions.keySet();
		Iterator<String> it= keySet.iterator();
		while (it.hasNext()) {
			String sessionId = it.next();
			ProbeSession pSession = sessions.get(sessionId);
			if (pSession != null) {
				Date sessionLastUpdated = pSession.getLastRequesteddate();
				String processId = pSession.getProcessId();
				long diffInMillis = Math.abs(dateNow.getTime()
						- sessionLastUpdated.getTime());
				if (diffInMillis > expiryPeriod * 1000) {
					logger.debug("Session with sessionId " + sessionId
							+ " is expired.. , removing it");
					logger.debug("Check if no other sessions corresponds to this process");
					stopStatsCollection(processId, sessionId);
					pSession.expire();
					sessions.remove(sessionId);
				}
			}
		}
	}

	private static synchronized void stopStatsCollection(String processId, String sessionId)
    {
	    // Remove the sessionId from the list of sessions associated with
	    // the process when session is being removed.
	    // Stop background statistics when there are no foreground sessions
	    // for the process, i.e., when the last sessionId is removed from
	    // the corresponding list of sessionIds.
	    ArrayList<String> sessionList = sessionMap.get(processId);
	    sessionList.remove(sessionId);
	    if(sessionList.isEmpty())
	    {
	    	logger.debug("The statistics collection for this process can be stopped");
	    	StatisticsTriggerController.stopStatistics(processId);
	    	sessionMap.remove(processId);	    	
	    }
//			else
//				sessionMap.put(processId, sessionList);
    }
	
	public static synchronized void createSession(String sessionId,
			String processId) throws ZENworksException, SocketException {

		ProbeSession pSession = null;

		String[] localProcessID = processId.split(":");

		boolean isLocal = false;

		InetAddress localhost;
		try {
			localhost = InetAddress.getLocalHost(); // give host name
			String localHostName = localhost.getHostName();
			InetAddress[] localIPS = InetAddress.getAllByName(localHostName);

			if (localIPS != null && localIPS.length >= 1) {
				for (int i = 0; i < localIPS.length; i++) {
					if ((localIPS[i].getHostAddress()).equals(localProcessID[0])) {
						isLocal = true;
					}
				}
			}			
		} catch (UnknownHostException e1) 
		{

		} finally 
		{
			if (isLocal == true)
				pSession = new ProbeSession(processId, sessionId, ConnectionType.LOCAL_SERVER);
			else
				pSession = new ProbeSession(processId, sessionId, ConnectionType.REMOTE_SERVER);

			sessions.put(sessionId, pSession);
			logger.debug("Starting statistics collection for the session "+ sessionId);
			startStatsCollection(processId, sessionId);			
		}
	}
	
	private static synchronized void startStatsCollection(String processId, String sessionId) throws ZENworksException
    {
	    ArrayList<String> sessionList = sessionMap.get(processId);
	    if(sessionList == null)
	    { 
	    	sessionList = new ArrayList<String>();
	    	sessionList.add(sessionId);
	    	sessionMap.put(processId, sessionList);
	    	StatisticsTriggerController.startStatistics(processId);
	    }
	    else if(!sessionList.contains(sessionId))
	    {	    	
	    	sessionList.add(sessionId);
	    	sessionMap.put(processId, sessionList);
	    }
    }

	public static ProbeSession getProbeSession(String sessionId)
	{
		return sessions.get(sessionId);
	}

	public static void setSessionMap(Map<String, ArrayList<String>> sessionMap)
    {
	    SessionManager.sessionMap = sessionMap;
    }

	public static Map<String, ArrayList<String>> getSessionMap()
    {
	    return sessionMap;
    }
	
	public static String getProcessId (String sessionId)
	{
		ProbeSession session = sessions.get(sessionId);	
		if(session!=null)
			return session.getProcessId();
		return null;
	}

	public static void setAppContext(WebApplicationContext appContext)
    {
	    SessionManager.appContext = appContext;
	    conf = (Properties) appContext.getBean("conf");
	    expiryPeriod = TimeExpression.inSeconds((conf.getProperty("com.googlecode.psiprobe.sessions.sessionExpiryPeriod")));
	    StatisticsTriggerController.setAppContext(appContext);
    }

	public static WebApplicationContext getAppContext()
    {
	    return appContext;
    }


}
