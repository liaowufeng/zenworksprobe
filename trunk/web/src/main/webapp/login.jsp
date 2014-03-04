<%--
 *
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
 *  DESCRIPTION :   This is the interface which provides the required information 
 *  from the remote process
 *
 *  MODIFICATION HISTORY :
 *  
 *  
*--%>

<%@ page language="java" contentType="text/html;"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.lang.*" %>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
 <title>Novell ZENworks Probe</title>
</head>
<%
    Properties prop = new Properties();
    String bodyContent = "";
    String provider_url = "";
    final String PROBE_CONFIG_FILE_NAME = "probe.properties";
        try
        {
        	String probeConfig = request.getRealPath("/");
       	
        	if(new File(probeConfig+File.separator+PROBE_CONFIG_FILE_NAME).exists()) {
        		probeConfig = probeConfig+File.separator+PROBE_CONFIG_FILE_NAME;
        	} else {
        		probeConfig = System.getProperty("probe.config.file");
        	}
        	
        	if(probeConfig == null  || probeConfig.trim().length() == 0) {
        		bodyContent = "<body>   You need to configure open ID providers and pass it as the java property probe.config.file";
        	} 
        	else {
        		File probeConfigFile = new File(probeConfig);
        	
        		if(probeConfigFile.exists()) {
            		//InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROBE_CONFIG_FILE_NAME);
            		InputStream in = new FileInputStream(probeConfigFile);
            		prop.load(in);
            		
            		provider_url=prop.getProperty("openid_provider_url");
        			if(provider_url != null && provider_url.trim().length() > 0)
        				bodyContent = "<body onLoad=\"document.forms[0].submit()\">";
        			else
        				bodyContent = "<body>openid_provider_url field is not present in the probe config file.";
            		
            	} else {
	            	bodyContent = "<body>" + probeConfigFile.getCanonicalPath() + " not found.";
        	    }
	        }
	    }
        catch (Exception e)
        {
            e.printStackTrace();
        }
 %>
 <%=bodyContent%>
<form action="j_spring_openid_security_check" method="post" target="_top">
    <input id="openid_identifier" name="openid_identifier" value="<%=provider_url%>" type="hidden"/>
</form>
</body>
</html>
