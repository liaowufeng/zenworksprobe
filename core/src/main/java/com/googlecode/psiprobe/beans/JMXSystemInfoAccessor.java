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

package com.googlecode.psiprobe.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.psiprobe.model.SunThread;
import com.googlecode.psiprobe.model.ThreadStackElement;
import com.googlecode.psiprobe.model.jmx.JMXSystemInformation;
import com.googlecode.psiprobe.tools.JmxTools;


/**
 * 
 * @author vlavanya@novell.com
 */
public class JMXSystemInfoAccessor
{

	private Log logger = LogFactory.getLog(this.getClass());
	private static List threads = null;
	private static int executionStackDepth = 1;
	private static long[] deadlockedIds = null;
	private static long[] allIds = null;
	private static ObjectName threadingOName;


	public static JMXSystemInformation getSystemInformation(MBeanServerConnection conn)
	{
		JMXSystemInformation systemInformation = new JMXSystemInformation();

		if (conn != null)
		{

			try
			{
				ObjectName engineOName = new ObjectName("Catalina:type=Engine");
				String baseDir = (String) conn.getAttribute(engineOName, "baseDir");
				ObjectName hostOName = new ObjectName("Catalina:type=Host,host=localhost");
				String appBase = (String) conn.getAttribute(hostOName, "appBase");
				ObjectName deployerOName = new ObjectName("Catalina:type=Deployer,host=localhost");
				String configBase = (String) conn.getAttribute(deployerOName, "configBaseName");
				ObjectName serverOName = new ObjectName("Catalina:type=Server");
				String serverInfo = (String) conn.getAttribute(serverOName, "serverInfo");
				systemInformation.setAppBase(baseDir + File.separator + appBase);
				systemInformation.setConfigBase(configBase);
				systemInformation.setWorkingDir(baseDir + File.separator + "bin");
				systemInformation.setServerInfo(serverInfo);

			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
			try
			{
				ObjectName memoryOName = new ObjectName("java.lang:type=Memory");
				CompositeData heapMemory = (CompositeData) conn.getAttribute(memoryOName, "HeapMemoryUsage");
				CompositeData nonHeapMemory = (CompositeData) conn.getAttribute(memoryOName, "NonHeapMemoryUsage");
				ObjectName osOName = new ObjectName("java.lang:type=OperatingSystem");
				int cpuCount = ((Integer) conn.getAttribute(osOName, "AvailableProcessors")).intValue();

				
				systemInformation.setTotalMemory(JmxTools.getLongAttr(heapMemory, "committed")
				        + JmxTools.getLongAttr(nonHeapMemory, "committed"));
				systemInformation.setUsedMemory(JmxTools.getLongAttr(heapMemory, "used")
				        + JmxTools.getLongAttr(nonHeapMemory, "used"));
				systemInformation.setMaxMemory(JmxTools.getLongAttr(heapMemory, "max")
				        + JmxTools.getLongAttr(nonHeapMemory, "max"));
				systemInformation.setFreeMemory(systemInformation.getTotalMemory()-systemInformation.getUsedMemory());

				systemInformation.setCpuCount(cpuCount);
				
				ObjectName runtimeOName = new ObjectName("java.lang:type=Runtime");
				TabularDataSupport sysProps = (TabularDataSupport) conn.getAttribute(runtimeOName, "SystemProperties");
				Map sysProperties = JmxTools.getMap(sysProps); 

				systemInformation.setSystemProperties(sysProperties);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
		return systemInformation;

	}
	
	private static boolean contains(long[] array, long e)
	{
		if (array != null)
		{
			for (int i = 0; i < array.length; i++)
			{
				if (array[i] == e)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static List getThreadStack(long threadID, String threadName, int stackElementCount, MBeanServerConnection conn)
    {
		List stack = null;
		try
		{
			if (conn != null)
			{
				if (threadID == -1 && threadName != null)
				{
					// find thread by name
					long[] allIds = (long[]) conn.getAttribute(threadingOName, "AllThreadIds");
					for (int i = 0; i < allIds.length; i++)
					{
						CompositeData cd = (CompositeData) conn.invoke(threadingOName, "getThreadInfo", new Object[]
						{ new Long(allIds[i]) }, new String[]
						{ "long" });
						String name = JmxTools.getStringAttr(cd, "threadName");
						if (threadName.equals(name))
						{
							threadID = allIds[i];
							break;
						}
					}
				}
				if (conn.queryMBeans(threadingOName, null) != null && threadID != -1)
				{
					CompositeData cd = (CompositeData) conn.invoke(threadingOName, "getThreadInfo", new Object[]
					{ new Long(threadID), new Integer(stackElementCount) }, new String[]
					{ "long", "int" });
					if (cd != null)
					{
						CompositeData[] elements = (CompositeData[]) cd.get("stackTrace");
						threadName = JmxTools.getStringAttr(cd, "threadName");

						stack = new ArrayList(elements.length);

						for (int i = 0; i < elements.length; i++)
						{
							CompositeData cd2 = elements[i];
							ThreadStackElement tse = new ThreadStackElement();
							tse.setClassName(JmxTools.getStringAttr(cd2, "className"));
							tse.setFileName(JmxTools.getStringAttr(cd2, "fileName"));
							tse.setMethodName(JmxTools.getStringAttr(cd2, "methodName"));
							tse.setLineNumber(JmxTools.getIntAttr(cd2, "lineNumber", -1));
							tse.setNativeMethod(JmxTools.getBooleanAttr(cd2, "nativeMethod"));
							stack.add(tse);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return stack;
	}

}
