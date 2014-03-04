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

package com.googlecode.psiprobe.beans.stats.jmxcollectors;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.model.Application;

/**
 * Collects application statistics
 * @author vlavanya@novell.com
 */
public class JMXAppStatsCollectorBean extends AbstractStatsCollectorBean
{

	private Log logger = LogFactory.getLog(JMXAppStatsCollectorBean.class);
	
	public void collect() throws Exception
	{

		long currentTime = System.currentTimeMillis();
		long totalReqDelta = 0;
		long totalAvgProcTime = 0;
		int participatingAppCount = 0;

		List applications = getConnection().collectApplicationsServletStats();
		Iterator it = applications.iterator();
		while (it.hasNext())
		{
			Application app = (Application) it.next();
			String name = app.getName();
			long reqDelta = buildDeltaStats("app.requests." + name, app.getRequestCount(), currentTime);
			long procTimeDelta = buildDeltaStats("app.proc_time." + name, app.getProcessingTime(), currentTime);
			buildDeltaStats("app.errors." + name, app.getErrorCount());

			long avgProcTime = reqDelta == 0 ? 0 : procTimeDelta / reqDelta;
			buildAbsoluteStats("app.avg_proc_time." + name, avgProcTime, currentTime);

			// make sure applications that did not serve any
			// requests
			// do not participate in average response time
			// equasion thus diluting the value
			if (reqDelta > 0)
			{
				if (!name.equals("/probe"))
				{
					totalReqDelta += reqDelta;
					totalAvgProcTime += avgProcTime;
					participatingAppCount++;
				}
			}

		}
		buildAbsoluteStats("total.requests", totalReqDelta, currentTime);
		buildAbsoluteStats("total.avg_proc_time", participatingAppCount == 0 ? 0 : totalAvgProcTime
		        / participatingAppCount, currentTime);

	}

//	private boolean excludeFromTotal(Context ctx)
//	{
		//return isSelfIgnored() && getServletContext().equals(ctx.getServletContext());
//	}

	public void reset()
	{
		try
		{
		List applications = getConnection().getApplications();
		Iterator it = applications.iterator();
		while (it.hasNext())
		{
			Application app = (Application) it.next();
			reset(app.getName());
		}
		resetStats("total.requests");
		resetStats("total.avg_proc_time");
		}catch(Exception e)
		{
			logger.debug("Resetting App statitiscs failed");
			logger.debug(Utils.getStackTrace(e));
		}
	}

	public void reset(String appName)
	{
		resetStats("app.requests." + appName);
		resetStats("app.proc_time." + appName);
		resetStats("app.errors." + appName);
		resetStats("app.avg_proc_time." + appName);
	}

}
