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

package com.googlecode.psiprobe.tools;

import com.googlecode.psiprobe.model.jmx.RuntimeInformation;
import com.googlecode.psiprobe.tools.JmxTools;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.Registry;

public class JMXRuntimeInfoAccessor
{

	
	public static RuntimeInformation getRuntimeInformation(MBeanServerConnection conn) throws Exception
	{
	
		if (conn != null)
		{

			RuntimeInformation ri = new RuntimeInformation();

			try
			{
				ObjectName runtimeOName = new ObjectName("java.lang:type=Runtime");
				ri.setStartTime(JmxTools.getLongAttr(conn, runtimeOName, "StartTime"));
				ri.setUptime(JmxTools.getLongAttr(conn, runtimeOName, "Uptime"));
				ri.setVmVendor(JmxTools.getStringAttr(conn, runtimeOName, "VmVendor"));

				ObjectName osOName = new ObjectName("java.lang:type=OperatingSystem");
				ri.setOsName(JmxTools.getStringAttr(conn, osOName, "Name"));
				ri.setOsVersion(JmxTools.getStringAttr(conn, osOName, "Version"));

				if (!ri.getVmVendor().startsWith("IBM Corporation"))
				{
					ri.setTotalPhysicalMemorySize(JmxTools.getLongAttr(conn, osOName, "TotalPhysicalMemorySize"));
					ri.setCommittedVirtualMemorySize(JmxTools.getLongAttr(conn, osOName, "CommittedVirtualMemorySize"));
					ri.setFreePhysicalMemorySize(JmxTools.getLongAttr(conn, osOName, "FreePhysicalMemorySize"));
					ri.setFreeSwapSpaceSize(JmxTools.getLongAttr(conn, osOName, "FreeSwapSpaceSize"));
					ri.setTotalSwapSpaceSize(JmxTools.getLongAttr(conn, osOName, "TotalSwapSpaceSize"));
					ri.setProcessCpuTime(JmxTools.getLongAttr(conn, osOName, "ProcessCpuTime"));
				}
				else
				{
					ri.setTotalPhysicalMemorySize(JmxTools.getLongAttr(conn, osOName, "TotalPhysicalMemory"));
				}

				return ri;
			}
			catch (Exception e)
			{
			//	logger.debug("OS information is unavailable");
				return null;
			}
		}
		return null;
	}


	public static void adviceGC(MBeanServerConnection conn)
    {
		ObjectName memOName;
        try
        {
	        memOName = new ObjectName("java.lang:type=Memory");
	        if(conn!=null)
		    	   conn.invoke(memOName, "gc", null, null);

        }
        catch (Exception e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
     }
	
	
}
