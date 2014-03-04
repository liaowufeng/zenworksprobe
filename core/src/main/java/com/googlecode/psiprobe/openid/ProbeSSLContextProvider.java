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

package com.googlecode.psiprobe.openid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class ProbeSSLContextProvider
{
	private static SSLContext sslContext = null; 
	public static SSLContext getContext()
	{
		String contextProvider = System.getProperty("probe.sslContext.provider");
		if(contextProvider!=null)
		{
			try
            {
	            Class contextProviderClass = Class.forName(contextProvider);
	            Method method = contextProviderClass.getDeclaredMethod("getContext", null);
	            sslContext = (SSLContext) method.invoke(null,null);	            
            }
            catch (ClassNotFoundException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (SecurityException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (NoSuchMethodException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (IllegalArgumentException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
        else
            try
            {
                sslContext = SSLContext.getDefault();
            }
            catch (NoSuchAlgorithmException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		return sslContext;
	}
	

}
