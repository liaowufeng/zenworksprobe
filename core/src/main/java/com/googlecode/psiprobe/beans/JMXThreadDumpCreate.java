/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package com.googlecode.psiprobe.beans;

import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.management.MBeanServerConnection;

public class JMXThreadDumpCreate
{
	public static File getThreadDumpFile(MBeanServerConnection conn, String processId)
	{
		ThreadInfo[] threads;
		Properties prop = new Properties();
		StringBuilder sb = new StringBuilder(1048576);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
		sb.append(df.format(new Date()) + "\n");

		try
		{
			RuntimeMXBean systemMXBean = newPlatformMXBeanProxy(conn, RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
			 if (systemMXBean != null)
	                prop.putAll(systemMXBean.getSystemProperties());
			 sb.append("Full thread dump " + prop.getProperty("java.vm.name") + // NOI18N
	                    " (" + prop.getProperty("java.vm.version") + " " + // NOI18N
	                    prop.getProperty("java.vm.info") + "):\n");  // NOI18N

			ThreadMXBean threadMXBean = newPlatformMXBeanProxy(conn, THREAD_MXBEAN_NAME, ThreadMXBean.class);
			if (threadMXBean == null)
			{
				return null;
			}
			threads = threadMXBean.dumpAllThreads(true, true);
			for (ThreadInfo threadInfo : threads)
				if (threadInfo != null)
					printThread(sb, threadMXBean, threadInfo);
			File dumpFile = getDumpFile(sb, processId);
			return dumpFile;

		}
		catch (Exception e)
		{
		}

		return null;
	}

	private static void printThread(final StringBuilder sb, final ThreadMXBean threadMXBean, final ThreadInfo thread)
	{
		MonitorInfo[] monitors = null;
		if (threadMXBean.isObjectMonitorUsageSupported())
		{
			monitors = thread.getLockedMonitors();
		}
		sb.append("\n\"" + thread.getThreadName() + // NOI18N
		        "\" - Thread t@" + thread.getThreadId() + "\n"); // NOI18N
		sb.append("   java.lang.Thread.State: " + thread.getThreadState()); // NOI18N
		sb.append("\n"); // NOI18N
		int index = 0;
		for (StackTraceElement st : thread.getStackTrace())
		{
			LockInfo lock = thread.getLockInfo();
			String lockOwner = thread.getLockOwnerName();

			sb.append("\tat " + st.toString() + "\n"); // NOI18N
			if (index == 0)
			{
				if ("java.lang.Object".equals(st.getClassName()) && // NOI18N
				        "wait".equals(st.getMethodName()))
				{ // NOI18N
					if (lock != null)
					{
						sb.append("\t- waiting on "); // NOI18N
						printLock(sb, lock);
						sb.append("\n"); // NOI18N
					}
				}
				else if (lock != null)
				{
					if (lockOwner == null)
					{
						sb.append("\t- parking to wait for "); // NOI18N
						printLock(sb, lock);
						sb.append("\n"); // NOI18N
					}
					else
					{
						sb.append("\t- waiting to lock "); // NOI18N
						printLock(sb, lock);
						sb.append(" owned by \"" + lockOwner + "\" t@" + thread.getLockOwnerId() + "\n"); // NOI18N
					}
				}
			}
			printMonitors(sb, monitors, index);
			index++;
		}
		StringBuilder jnisb = new StringBuilder();
		printMonitors(jnisb, monitors, -1);
		if (jnisb.length() > 0)
		{
			sb.append("   JNI locked monitors:\n");
			sb.append(jnisb);
		}
		if (threadMXBean.isSynchronizerUsageSupported())
		{
			sb.append("\n   Locked ownable synchronizers:"); // NOI18N
			LockInfo[] synchronizers = thread.getLockedSynchronizers();
			if (synchronizers == null || synchronizers.length == 0)
			{
				sb.append("\n\t- None\n"); // NOI18N
			}
			else
			{
				for (LockInfo li : synchronizers)
				{
					sb.append("\n\t- locked "); // NOI18N
					printLock(sb, li);
					sb.append("\n"); // NOI18N
				}
			}
		}
	}

	private static File getDumpFile(StringBuilder sb, String processId)
	{
	    SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
	    processId = processId.replaceAll(":","_");
		File dumpFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "TD_"+processId+"_"+df.format(new Date())+".txt");
		FileWriter writer = null;
		BufferedWriter bw = null;
		try
		{
			writer = new FileWriter(dumpFile);
			bw = new BufferedWriter(writer);
			bw.flush();
			bw.write(sb.toString());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bw.close();
				writer.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}

		return dumpFile;
	}

	private static void printMonitors(final StringBuilder sb, final MonitorInfo[] monitors, final int index)
	{
		if (monitors != null)
		{
			for (MonitorInfo mi : monitors)
			{
				if (mi.getLockedStackDepth() == index)
				{
					sb.append("\t- locked "); // NOI18N
					printLock(sb, mi);
					sb.append("\n"); // NOI18N
				}
			}
		}
	}

	private static void printLock(StringBuilder sb, LockInfo lock)
	{
		String id = Integer.toHexString(lock.getIdentityHashCode());
		String className = lock.getClassName();

		sb.append("<" + id + "> (a " + className + ")"); // NOI18N
	}


}
