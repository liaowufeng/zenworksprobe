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

package com.googlecode.psiprobe.stats;

import java.text.ParseException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.web.context.WebApplicationContext;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.beans.stats.jmxcollectors.AbstractStatsCollectorBean;
import com.googlecode.psiprobe.beans.stats.jmxcollectors.JMXAppStatsCollectorBean;
import com.googlecode.psiprobe.beans.stats.jmxcollectors.JMXConnectorStatsCollectorBean;
import com.googlecode.psiprobe.beans.stats.jmxcollectors.JMXJvmMemoryStatsCollectorBean;
import com.googlecode.psiprobe.beans.stats.jmxcollectors.JMXRuntimeStatsCollectorBean;
import com.googlecode.psiprobe.connections.Connection;
import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.connections.JMXConnection;
import com.googlecode.psiprobe.model.stats.StatsCollection;
import com.googlecode.psiprobe.tools.TimeExpression;
public class StatsCollectorThread
{
	private static final String MEM_PERIOD = "com.googlecode.psiprobe.beans.stats.collectors.memory.period";
	private static final String MEM_SPAN = "com.googlecode.psiprobe.beans.stats.collectors.memory.span";
	private static final String MEM_PHASE = "com.googlecode.psiprobe.beans.stats.collectors.memory.phase";
	
	private static final String APP_PERIOD = "com.googlecode.psiprobe.beans.stats.collectors.app.period";
	private static final String APP_SPAN = "com.googlecode.psiprobe.beans.stats.collectors.app.span";
	private static final String APP_PHASE = "com.googlecode.psiprobe.beans.stats.collectors.app.phase";
	
	private static final String RUNTIME_PERIOD = "com.googlecode.psiprobe.beans.stats.collectors.runtime.period";
	private static final String RUNTIME_SPAN = "com.googlecode.psiprobe.beans.stats.collectors.runtime.span";
	private static final String RUNTIME_PHASE = "com.googlecode.psiprobe.beans.stats.collectors.runtime.phase";
	
	private static final String CONNECTOR_PHASE = "com.googlecode.psiprobe.beans.stats.collectors.connector.phase";
	private static final String CONNECTOR_SPAN = "com.googlecode.psiprobe.beans.stats.collectors.connector.span";
	private static final String CONNECTOR_PERIOD = "com.googlecode.psiprobe.beans.stats.collectors.connector.period";
	
	private String processId;
	private StatsCollection statsCollection;
	private Connection connection;
	private JMXAppStatsCollectorBean appStats;
	private JMXJvmMemoryStatsCollectorBean memoryStats;
	private JMXRuntimeStatsCollectorBean runtimeStats;
	private JMXConnectorStatsCollectorBean connectorStats;
	private Scheduler scheduler;
	private static Log logger;
	private static Properties stats;
	
	static
	{
		logger = LogFactory.getLog("StatsCollector");
	}
	
	public StatsCollectorThread(String processId, WebApplicationContext appContext) throws ZENworksException
	{
		this.processId = processId;
		connection = new JMXConnection(processId);
		statsCollection = new StatsCollection();
		statsCollection.setApplicationContext(appContext);
		statsCollection.setMaxFiles(3);
		statsCollection.setSwapFileName(processId+"_stats.xml");
		scheduler = (Scheduler) appContext.getBean("scheduler"); 
		appStats = new JMXAppStatsCollectorBean();
		memoryStats = new JMXJvmMemoryStatsCollectorBean();
		connectorStats = new JMXConnectorStatsCollectorBean();
		runtimeStats = new JMXRuntimeStatsCollectorBean();
		stats = (Properties) appContext.getBean("stats");
	}
	public void setProcessId(String processId)
    {
	    this.processId = processId;
    }

	public String getProcessId()
    {
	    return processId;
    }

	public void setStatsCollection(StatsCollection statsCollection)
    {
	    this.statsCollection = statsCollection;
    }

	public StatsCollection getStatsCollection()
    {
	    return statsCollection;
    }

	public void setConn(Connection conn)
    {
	    this.connection = conn;
    }

	public Connection getConn()
    {
	    return connection;
    }

	public void setAppStats(JMXAppStatsCollectorBean appStats)
    {
	    this.appStats = appStats;
    }

	public JMXAppStatsCollectorBean getAppStats()
    {
	    return appStats;
    }
	
	public void startStatsCollection()
	{
		logger.info("Collect JVM Memory stats");
		triggerStatsCollection(memoryStats, stats.getProperty(MEM_PERIOD), stats.getProperty(MEM_SPAN), stats.getProperty(MEM_PHASE));
		logger.info("Collect App stats");
		triggerStatsCollection(appStats, stats.getProperty(APP_PERIOD), stats.getProperty(APP_SPAN), stats.getProperty(APP_PHASE));
		logger.info("Collect Runtime stats");
		triggerStatsCollection(runtimeStats, stats.getProperty(RUNTIME_PERIOD), stats.getProperty(RUNTIME_SPAN), stats.getProperty(RUNTIME_PHASE));
		logger.info("Collect Connector stats");
		triggerStatsCollection(connectorStats, stats.getProperty(CONNECTOR_PERIOD), stats.getProperty(CONNECTOR_SPAN), stats.getProperty(CONNECTOR_PHASE));
	}

	private void triggerStatsCollection(AbstractStatsCollectorBean statsCollector, String period, String span, String phase)
    {
		statsCollector.setStatsCollection(statsCollection);
		statsCollector.setConnection(connection);
		statsCollector.setMaxSeries((int) TimeExpression.dataPoints(period, span));

//		logger.debug("Creating jobdetail");
		MethodInvokingJobDetailFactoryBean statsJobDetail = new MethodInvokingJobDetailFactoryBean();
		statsJobDetail.setTargetObject(statsCollector);
		statsJobDetail.setTargetMethod("collect");
		statsJobDetail.setConcurrent(false);
		statsJobDetail.setName(processId+statsCollector.getClass().getSimpleName());
		statsJobDetail.setGroup(processId);
		try
        {
			statsJobDetail.afterPropertiesSet();
//	        logger.debug("calling afterproperties set on jobdetail");
        }
        catch (ClassNotFoundException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		
		CronTriggerBean statsTrigger = new CronTriggerBean();
		statsTrigger.setBeanName(processId+statsCollector.getClass().getSimpleName());
		statsTrigger.setGroup(processId);
		try
        {
			statsTrigger.setCronExpression(TimeExpression.cronExpression(period, phase));
        }
        catch (ParseException e1)
        {
	        e1.printStackTrace();
        }
		try
        {
			statsTrigger.afterPropertiesSet();
        }
        catch (ParseException e)
        {
	        e.printStackTrace();
        }
        		
		try
        {
			scheduler.scheduleJob((JobDetail) statsJobDetail.getObject(), statsTrigger);
        }
        catch (SchedulerException e)
        {
	        e.printStackTrace();
        }	    
    }

	public void stopStatsCollection()
	{
		logger.debug("Stopping stats Collection for process "+processId);
		try
        {
	        scheduler.deleteJob(processId+JMXJvmMemoryStatsCollectorBean.class.getSimpleName(),processId);
	        scheduler.deleteJob(processId+JMXAppStatsCollectorBean.class.getSimpleName(),processId);
	        scheduler.deleteJob(processId+JMXRuntimeStatsCollectorBean.class.getSimpleName(),processId);
	        scheduler.deleteJob(processId+JMXConnectorStatsCollectorBean.class.getSimpleName(),processId);
        }
        catch (SchedulerException e)
        {
	        logger.debug(Utils.getStackTrace(e));
        }
	}

	public void setMemStats(JMXJvmMemoryStatsCollectorBean memStats)
    {
	    this.memoryStats = memStats;
    }

	public JMXJvmMemoryStatsCollectorBean getMemStats()
    {
	    return memoryStats;
    }
	public void setRuntimeStats(JMXRuntimeStatsCollectorBean runtimeStats)
    {
	    this.runtimeStats = runtimeStats;
    }
	public JMXRuntimeStatsCollectorBean getRuntimeStats()
    {
	    return runtimeStats;
    }
	public void setConnectorStats(JMXConnectorStatsCollectorBean connectorStats)
    {
	    this.connectorStats = connectorStats;
    }
	public JMXConnectorStatsCollectorBean getConnectorStats()
    {
	    return connectorStats;
    }
	
}
