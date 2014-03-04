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

package com.googlecode.psiprobe.model.jmx;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * POJO representing system information for "system infromation" tab.
 * 
 * @author vlavanya@novell.com
 */
public class JMXSystemInformation implements Serializable
{

	private String appBase;
	
	private String workingDir;

	private String configBase;
	
	private String serverInfo;

	private Map systemProperties;
	
	private long maxMemory, freeMemory, totalMemory, usedMemory;
	
	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	private int cpuCount; 
	private Date date;

	private String ip = null;

	private String port = null;

	public long getMaxMemory()
	{
		return maxMemory;
	}	

	public void setMaxMemory(long maxMemory)
	{
		this.maxMemory = maxMemory;
	}
	
	public long getFreeMemory()
	{
		return freeMemory;
	}	
	
	public void setFreeMemory(long freeMemory)
	{
		this.freeMemory = freeMemory;
	}
	
	public long getTotalMemory()
	{
		return totalMemory;
	}
	
	public void setTotalMemory(long totalMemory)
	{
		this.totalMemory = totalMemory;
	}
	
	public int getCpuCount()
	{
		return cpuCount;
	}
	
	public void setCpuCount(int cpuCount)
	{
		this.cpuCount = cpuCount;
	}

	public Date getDate()
	{
		return date;
	}
	
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public String getServerInfo()
	{
		return serverInfo;
	}	
	
	public void setServerInfo(String serverInfo)
	{
		this.serverInfo = serverInfo;		
	}
	
	public String getWorkingDir()
	{
		return workingDir;
	}
	
	public void setWorkingDir(String workingDir)
	{
		this.workingDir = workingDir;
	}

	public String getAppBase()
	{
		return appBase;
	}

	public void setAppBase(String appBase)
	{
		this.appBase = appBase;
	}

	public String getConfigBase()
	{
		return configBase;
	}

	public void setConfigBase(String configBase)
	{
		this.configBase = configBase;
	}

	public Map getSystemProperties()
	{
		return systemProperties;
	}

	public void setSystemProperties(Map systemProperties)
	{
		this.systemProperties = systemProperties;
	}

	public Set getSystemPropertySet()
	{
		return systemProperties.entrySet();
	}

}
