<%--
 * Licensed under the GPL License.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="/WEB-INF/tld/probe.tld" prefix="probe" %>


<%--
	Main site decorator. Face of the Probe.

	Author: Vlad Ilyushchenko
--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
		"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="${lang}" xml:lang="${lang}">
	<head>
		<script>
			
			function showProbeHelpWindow(contaxtpath)
			{
			window.open
			(contaxtpath+'/help/probe_page.html','_blank','status=yes,menubar=yes,scrollbars=yes,resizable=yes,fullscreen=yes,width=1000,height=1000');
			window.focus();
			}
		</script>
		<title>Probe - <decorator:title default="Tomcat management"/></title>
		<link type="image/gif" rel="shortcut icon" href="<c:url value='/css/favicon.gif'/>"/>
		<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='tables.css'/>"/>
		<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='main.css'/>"/>
		<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='mainnav.css'/>"/>
		<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='messages.css'/>"/>
		<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='tooltip.css'/>"/>
		<decorator:head/>
	</head>

	<body style="margin:0px; padding:0px 0px 0px 0px; min-width:860px">
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tbody>
	<tr><td>
		<div id="caption">
			<table id="top" width="100%" border="0">
				<tbody>
					<tr>
						<td colspan="2" >
							<li id="logoutTitle">
								<a href="#"  onClick="javascript:showProbeHelpWindow('${pageContext.request.contextPath}')"><spring:message code="probe.jsp.help"/></a>
								<img width="2" height="15" border="0" src="/zenworks-probe/css/classic/img/banner_divider.gif">
								<a href="<c:url value='/static/j_spring_security_logout'><c:param name='sessionId'  value='${sessionId}'/></c:url>"><spring:message code="probe.jsp.Logout"/></a>
							</li>
						</td>
					</tr>		
					<tr> 
						<td id="runtime" >
							<spring:message code="probe.jsp.version" arguments="${version}"/>
							<spring:message code="probe.jsp.target" arguments="${hostname}"/>
							<span class="uptime"><spring:message code="probe.jsp.uptime"
													arguments="${uptime_days},${uptime_hours},${uptime_mins}"/>
							</span>
						</td>
						<td>
							<div id="title">
								<decorator:title default="Probe"/>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		</td>
	</tr>
	<tr>
		<td>
			<table width="83%" cellspacing="0" cellpadding="0" border="0" style="margin:0px; padding-left:30px;">
			<tbody>
				<tr>
					<td>
						<div id="navcontainer">
							<ul id="tabnav">
								<li>
									<a class="${navTabApps}" href="<c:url value='/index.htm?size=${param.size}'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.applications"/>
									</a>
								</li>
								<%-- <c:choose>
									<c:when ></c:when>
									<c:otherwise>
								<li>
									<a class="${navTabDatasources}" href="<c:url value='/datasources.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.datasources"/>
									</a>
								</li>
								<li>
									<a class="${navTabDeploy}" href="<c:url value='/deploy.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.deployment"/>
									</a>
								</li>
								<li><a class="${navTabLogs}"	href="<c:url value='/logs/index.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>"> 
									<spring:message code="probe.jsp.menu.logs" /> </a>
									</li>
									</c:otherwise>
								</c:choose> --%>
								<li>
									<a class="${navTabThreads}" href="<c:url value='/threads.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.threads"/>
									</a>
								</li>
								<%--  <c:choose>
									<c:when test="${Remote}"></c:when>
									<c:otherwise>
								<li>
									<a class="${navTabCluster}" href="<c:url value='/cluster.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.cluster"/>
									</a>
								</li>
								</c:otherwise>
								</c:choose> --%>
								<li>
									<a class="${navTabSystem}" href="<c:url value='/sysinfo.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.sysinfo"/>
									</a>
								</li>
								<li id="last">
									<a class="${navTabConnectors}" href="<c:url value='/connectors.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.connectors"/>
									</a>
								</li>
								<%--  <c:choose>
									<c:when test="${Remote}"></c:when>
									<c:otherwise>
								<li id="last">
									<a class="${navTabQuickCheck}" href="<c:url value='/adm/quickcheck.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
										<spring:message code="probe.jsp.menu.quickcheck"/>
									</a>
								</li>
								</c:otherwise>
								</c:choose> --%>
							</ul>
						</div>
					</td>
				</tr>
				<tr height="5">
					<td class="belowObjectTab" valign="top" align="left" style="padding-left:10px; background-repeat: repeat-x; background-image: url(/zenworks/images/tab_border-gradientfill.gif);" colspan="3"><img width="10" height="6" src="/zenworks/images/1pxSpacer.gif"></td>
				</tr>
			</tbody>
			</table>
			<c:choose>
				<c:when test="${! empty use_decorator}">
					<page:applyDecorator name="${use_decorator}">
						<decorator:body/>
					</page:applyDecorator>
				</c:when>
				<c:otherwise>
					<div id="mainBody">
						<decorator:body/>
					</div>
				</c:otherwise>
			</c:choose>
	
		</td>
	</tr>
	<tr>
		<td>
			<div id="footer">
				<ul>
					<li>
						<a href="<c:url value='/index.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.applications"/>
						</a>
					</li>
					<%-- <c:choose>
						<c:when test="${Remote}"></c:when>
						<c:otherwise>
					<li>
						<a href="<c:url value='/datasources.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.datasources"/>
						</a>
					</li>
					<li>
						<a href="<c:url value='/deploy.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.deployment"/>
						</a>
					</li>
					<li>
						<a href="<c:url value='/logs/index.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.logs"/>
						</a>
					</li>
					</c:otherwise>
					</c:choose> --%>
					<li>
						<a href="<c:url value='/threads.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.threads"/>
						</a>
					</li>
					<%-- <c:choose>
						<c:when test="${Remote}"></c:when>
						<c:otherwise>
					<li>
						<a href="<c:url value='/cluster.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.cluster"/>
						</a>
					</li>
					</c:otherwise>
					</c:choose> --%>
					<li>
						<a href="<c:url value='/sysinfo.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.sysinfo"/>
						</a>
					</li>
					<li>
						<a href="<c:url value='/connectors.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.connectors"/>
						</a>
					</li>
					<%-- <c:choose>
						<c:when test="${Remote}"></c:when>
						<c:otherwise>
					<li class="last">
						<a href="<c:url value='/adm/quickcheck.htm'><c:param name='sessionId'  value='${sessionId}'/></c:url>">
							<spring:message code="probe.jsp.menu.quickcheck"/>
						</a>
					</li>
					</c:otherwise>
					</c:choose> --%>
				</ul>
				<p>
					<spring:message code="probe.jsp.copyright"/>
					<br/>
					<spring:message code="probe.jsp.icons.credit"/>
				</p>
				<div id="locales">
					<a href="?<probe:addQueryParam param='lang' value='en'/>"><img
							src="<c:url value='/flags/gb.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="EN" /></a>
					<a href="?<probe:addQueryParam param='lang' value='ja'/>"><img
							src="<c:url value='/flags/jp.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="JP" /></a>
					<a href="?<probe:addQueryParam param='lang' value='it'/>"><img
							src="<c:url value='/flags/it.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="IT" /></a>
					<a href="?<probe:addQueryParam param='lang' value='de'/>"><img
							src="<c:url value='/flags/de.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="DE" /></a>
					<a href="?<probe:addQueryParam param='lang' value='es'/>"><img
							src="<c:url value='/flags/es.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="ES" /></a>
					<a href="?<probe:addQueryParam param='lang' value='fr'/>"><img
							src="<c:url value='/flags/fr.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="FR" /></a>
					<a href="?<probe:addQueryParam param='lang' value='pt_br'/>"><img
							src="<c:url value='/flags/br.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="BR" /></a>
					<a href="?<probe:addQueryParam param='lang' value='zh_CN'/>"><img
							src="<c:url value='/flags/cn.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="CN" /></a>
					<a href="?<probe:addQueryParam param='lang' value='zh_TW'/>"><img
							src="<c:url value='/flags/tw.gif'><c:param name='sessionId'  value='${sessionId}'/></c:url>" alt="TW" /></a>
				</div>
				<p>
					<spring:message code="probe.jsp.i18n.credit"/>
				</p>
			</div>
		</td>
	</tr>
</tbody>
</table>

	</body>
</html>