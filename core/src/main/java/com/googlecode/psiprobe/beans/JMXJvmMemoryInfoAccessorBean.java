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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.psiprobe.model.jmx.MemoryPool;
import com.googlecode.psiprobe.tools.JmxTools;


/**
 * 
 * @author vlavanya@novell.com
 */
public class JMXJvmMemoryInfoAccessorBean
{

	private Log logger = LogFactory.getLog(this.getClass());

	private MBeanServerConnection conn = null;
	
	public void setConn(MBeanServerConnection conn)
	{
		this.conn = conn;
	}
	
	public MBeanServerConnection getConn()
	{
		return conn;
	}
	
	public List getPools() throws Exception
	{

		if (conn != null)
		{
			List memoryPools = new LinkedList();
			Set memoryOPools = conn.queryMBeans(new ObjectName("java.lang:type=MemoryPool,*"), null);

			//
			// totals
			//
			long totalInit = 0;
			long totalMax = 0;
			long totalUsed = 0;
			long totalCommitted = 0;

			for (Iterator it = memoryOPools.iterator(); it.hasNext();)
			{
				ObjectInstance oi = (ObjectInstance) it.next();
				ObjectName oName = oi.getObjectName();
				MemoryPool memoryPool = new MemoryPool();
				memoryPool.setName(JmxTools.getStringAttr(conn, oName, "Name"));
				memoryPool.setType(JmxTools.getStringAttr(conn, oName, "Type"));

				CompositeDataSupport cd = (CompositeDataSupport) conn.getAttribute(oName, "Usage");
				//
				// It seems that "Usage" attribute of one of the pools may turn
				// into null intermittently. We better have a
				// dip in the graph then an NPE though.
				//
				if (cd != null)
				{
					memoryPool.setMax(JmxTools.getLongAttr(cd, "max"));
					memoryPool.setUsed(JmxTools.getLongAttr(cd, "used"));
					memoryPool.setInit(JmxTools.getLongAttr(cd, "init"));
					memoryPool.setCommitted(JmxTools.getLongAttr(cd, "committed"));
				}
				else
				{
					logger.error("Oops, JVM problem? " + oName.toString() + " \"Usage\" attribute is NULL!");
				}

				totalInit += memoryPool.getInit();
				totalMax += memoryPool.getMax();
				totalUsed += memoryPool.getUsed();
				totalCommitted += memoryPool.getCommitted();

				memoryPools.add(memoryPool);
			}

			if (!memoryPools.isEmpty())
			{
				MemoryPool pool = new MemoryPool();
				pool.setName("Total");
				pool.setType("TOTAL");
				pool.setInit(totalInit);
				pool.setUsed(totalUsed);
				pool.setMax(totalMax);
				pool.setCommitted(totalCommitted);
				memoryPools.add(pool);
			}

			return memoryPools;

		}
		return null;

	}
}
