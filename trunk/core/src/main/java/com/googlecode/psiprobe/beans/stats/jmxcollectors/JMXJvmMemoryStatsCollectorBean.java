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

import com.googlecode.psiprobe.model.jmx.MemoryPool;

/**
 * 
 * @author vlavanya@novell.com
 */
public class JMXJvmMemoryStatsCollectorBean extends AbstractStatsCollectorBean 
{
	
    public void collect() throws Exception 
    {
    	List pools = getConnection().getMemoryPools();
    	
        long time = System.currentTimeMillis();
        for (Iterator it = pools.iterator(); it.hasNext(); ) {
            MemoryPool pool = (MemoryPool) it.next();
            buildAbsoluteStats("memory.pool."+pool.getName(), pool.getUsed(), time);
        }
    }

}
