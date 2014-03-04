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
 * MODIFICATION HISTORY :
 *
 *  Version:    Change description:             Date:       Changed by:
 *
 *  1.0         Initial Version                 2014      Lavanya Vankadara
 *  
*/

package com.googlecode.psiprobe.beans.stats.jmxcollectors;

import com.googlecode.psiprobe.model.jmx.RuntimeInformation;

/**
 * 
 *@author vlavanya@novell.com
 */
public class JMXRuntimeStatsCollectorBean extends AbstractStatsCollectorBean 
{

    public void collect() throws Exception {

    	RuntimeInformation ri = getConnection().getRuntimeInfo();
    	
    	if (ri != null) 
    	{
            long time = System.currentTimeMillis();
            buildAbsoluteStats("os.memory.committed", ri.getCommittedVirtualMemorySize()/1024, time);
            buildAbsoluteStats("os.memory.physical", (ri.getTotalPhysicalMemorySize() - ri.getFreePhysicalMemorySize())/1024, time);
            buildAbsoluteStats("os.memory.swap", (ri.getTotalSwapSpaceSize() - ri.getFreeSwapSpaceSize())/1024, time);
            //
            // processCpuTime is in nano-seconds, to build timePercentageStats both time parameters have to use
            // in the same units.
            //
            buildTimePercentageStats("os.cpu", ri.getProcessCpuTime() / 1000000, time);
        }
    }
}
