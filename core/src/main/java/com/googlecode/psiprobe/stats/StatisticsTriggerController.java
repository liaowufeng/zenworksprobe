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

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.WebApplicationContext;

import com.googlecode.psiprobe.connections.ZENworksException;
import com.googlecode.psiprobe.model.stats.StatsCollection;

public class StatisticsTriggerController
{
	private static Map<String, StatsCollectorThread> statsMap = new HashMap<String, StatsCollectorThread>();
	private static WebApplicationContext appContext;

	private StatisticsTriggerController()
	{
		
	}
	public static void setStatsMap(Map<String, StatsCollectorThread> statsMap)
    {
	    StatisticsTriggerController.statsMap = statsMap;
    }

	public static Map<String, StatsCollectorThread> getStatsMap()
    {
	    return statsMap;
    }
	
	public static void startStatistics(String processId) throws ZENworksException
	{
		StatsCollectorThread statsCollector = new StatsCollectorThread(processId, appContext);
		statsMap.put(processId, statsCollector);
		statsCollector.startStatsCollection();
		
	}
	
	public static void stopStatistics(String processId)
	{
		StatsCollectorThread statsCollector = statsMap.get(processId);
		if(statsCollector!=null)
		{
			statsCollector.stopStatsCollection();
			statsCollector.getConn().closeConnection();
		}		
		statsMap.remove(processId);
	}

	public static void setAppContext(WebApplicationContext appContext)
    {
	    StatisticsTriggerController.appContext = appContext;
    }

	public static WebApplicationContext getAppContext()
    {
	    return appContext;
    }
	
	public static StatsCollection getStatsCollection(String processId)
	{
		StatsCollectorThread statsCollector = statsMap.get(processId);
		if(statsCollector!=null)
			return statsCollector.getStatsCollection();
		return null;		
	}
	
	public static StatsCollectorThread getStatsCollector(String processId)
	{
		return statsMap.get(processId);
	}

}
