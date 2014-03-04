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

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import com.googlecode.psiprobe.model.SunThread;
import com.googlecode.psiprobe.model.ThreadStackElement;
import com.googlecode.psiprobe.tools.JmxTools;

/**
 * 
 * @author vlavanya@novell.com
 */
public class JMXThreadInfoAccessor
{

	private static List threads = null;

	private static int executionStackDepth = 1;

	private static long[] deadlockedIds = null;

	private static long[] allIds = null;

	private static ObjectName threadingOName;

	public static List getSunThreads(MBeanServerConnection conn)
	{

		try
		{
			if (conn != null)
			{
				threadingOName = new ObjectName("java.lang:type=Threading");
				deadlockedIds = (long[]) conn.invoke(threadingOName, "findMonitorDeadlockedThreads", null, null);
				allIds = (long[]) conn.getAttribute(threadingOName, "AllThreadIds");
				if (allIds != null)
				{
					threads = new ArrayList(allIds.length);

					for (int i = 0; i < allIds.length; i++)
					{
						CompositeData cd = null;

						cd = (CompositeData) conn.invoke(threadingOName, "getThreadInfo", new Object[]
						{ new Long(allIds[i]), new Integer(executionStackDepth) }, new String[]
						{ "long", "int" });

						if (cd != null)
						{
							SunThread st = new SunThread();
							st.setId(JmxTools.getLongAttr(cd, "threadId"));
							st.setName(JmxTools.getStringAttr(cd, "threadName"));
							st.setState(JmxTools.getStringAttr(cd, "threadState"));
							st.setSuspended(JmxTools.getBooleanAttr(cd, "suspended"));
							st.setInNative(JmxTools.getBooleanAttr(cd, "inNative"));
							st.setLockName(JmxTools.getStringAttr(cd, "lockName"));
							st.setLockOwnerName(JmxTools.getStringAttr(cd, "lockOwnerName"));
							st.setWaitedCount(JmxTools.getLongAttr(cd, "waitedCount"));
							st.setBlockedCount(JmxTools.getLongAttr(cd, "blockedCount"));
							st.setDeadlocked(contains(deadlockedIds, st.getId()));

							CompositeData[] stack = (CompositeData[]) cd.get("stackTrace");

							if (stack.length > 0)
							{
								CompositeData cd2 = stack[0];
								ThreadStackElement tse = new ThreadStackElement();
								tse.setClassName(JmxTools.getStringAttr(cd2, "className"));
								tse.setFileName(JmxTools.getStringAttr(cd2, "fileName"));
								tse.setMethodName(JmxTools.getStringAttr(cd2, "methodName"));
								tse.setLineNumber(JmxTools.getIntAttr(cd2, "lineNumber", -1));
								tse.setNativeMethod(JmxTools.getBooleanAttr(cd2, "nativeMethod"));
								st.setExecutionPoint(tse);
							}

							threads.add(st);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return threads;

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

	public static List getThreadStack(long threadID, String threadName, int stackElementCount,
	        MBeanServerConnection conn)
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
