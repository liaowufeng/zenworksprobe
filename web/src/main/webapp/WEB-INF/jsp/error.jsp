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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>


<html>
<head>
	<title>
		<spring:message code="probe.jsp.error.title" />
	</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<%
    Properties prop = new Properties();
    String errorMessage = "probe.jsp.error.for.all";
    final String PROBE_RESOURCE_FILE_NAME = "resource.properties";
    try
    {
        String probeConfig = request.getRealPath("/");
        probeConfig = probeConfig+File.separator+"resources"+File.separator+PROBE_RESOURCE_FILE_NAME;
        File probeConfigFile = new File(probeConfig);
        InputStream in = new FileInputStream(probeConfigFile);
        prop.load(in);
        errorMessage = (String) request.getAttribute("errorMessage");
        if(errorMessage == null){
            errorMessage = "probe.jsp.error.for.all";
        }
    }
    catch (Exception e)
    {
    }
%>
<body style="margin:0px; padding:0px 0px 0px 0px; background-color: whitesmoke;" >
<div class="pageRegion">
	<table cellspacing="0" cellpadding="0" style="width:100%;">
		<tbody>
			<td width="1%" colspan="3">&nbsp;</td>
			<tr >
				<td width="20px"/>
				<td width="1%">
					<img src="/zenworks-probe/css/Error.gif"/>
				</td>
				<td>
					<spring:message code="<%=errorMessage%>" />
				</td>
			</tr>
		</tbody>
	</table>
</div>
</body>
</html>