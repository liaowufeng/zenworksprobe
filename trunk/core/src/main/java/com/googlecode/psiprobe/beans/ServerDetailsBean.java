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

import javax.management.MBeanServerConnection;


/**
 * Interface of beans that retrieve information about "resources" of application server. Typically those resources would
 * be datasources.
 * 
 * @author vlavanya@novell.com
 */
public class ServerDetailsBean {

	private String ip = null;
	private String port = null;
	private MBeanServerConnection conn = null;
	
	public void setIp(String ip)
	{
		System.out.println("Setting ip in server details "+ip);
		this.ip = ip;
	}
	
	public String getIp()
	{
		return ip;
	}
	
	public void setPort(String port)
	{
		System.out.println("Setting port in server details "+port);
		this.port = port;
	}
	
	public String getPort()
	{
		return port;
	}
	
	public void setConn(MBeanServerConnection conn)
	{
		this.conn = conn;
	}
	
	public MBeanServerConnection getConn()
	{
		return conn;
	}
}
