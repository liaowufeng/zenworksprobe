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

package com.googlecode.psiprobe.controllers.system;

import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class JMXHeapDump
{

    public static String getHeapDumpFile(MBeanServerConnection conn, String processId) throws Exception 
    {
        String fileName;
        fileName = getFileName(processId, conn);
        ObjectName memoryName = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        Object[] params = new Object[] { fileName, Boolean.TRUE };
        String[] signature = new String[] { String.class.getName(), boolean.class.getName() };
        conn.invoke(memoryName, "dumpHeap", params, signature);

        return fileName;
    }

    private static String getFileName(String processId, MBeanServerConnection conn) throws IOException
    {
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        processId = processId.replaceAll(":","_");
        String tmpDir = getTempDir(conn);
        String fileName = tmpDir + "HD_"+processId+"_"+df.format(new Date())+".hprof";
        return fileName;
    }

    private static String getTempDir(MBeanServerConnection conn) throws IOException
    {
        Properties prop = new Properties();
        RuntimeMXBean systemMXBean = newPlatformMXBeanProxy(conn, RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        if (systemMXBean != null)
        {
            prop.putAll(systemMXBean.getSystemProperties());
            String tmpDir = prop.getProperty("java.io.tmpdir");
            String osName = prop.getProperty("os.name");
            if(osName.toLowerCase().contains("windows"))
                return tmpDir+"\\";
            else
                return tmpDir+"/";
        }
        return null;
        
    }
    

}
