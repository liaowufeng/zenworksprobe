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

<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<p>
<c:choose>
	<c:when test="${! empty dumpFile}">
		<c:choose>
		 	<c:when test="${connType=='LOCAL'}">
				<spring:message code="probe.jsp.sysinfo.memory.heapDumpInfo"/>&nbsp;${dumpFile}
			</c:when>
			<c:otherwise>
				<spring:message code="probe.jsp.sysinfo.memory.heapDumpInfoRemote" arguments="${remoteProcessIP},${dumpFile}"/>
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<spring:message code="probe.jsp.sysinfo.memory.heapDumpErr"/>
	</c:otherwise>
</c:choose>
</p>