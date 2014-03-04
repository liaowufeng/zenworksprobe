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

package com.googlecode.psiprobe.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.sf.javainetlocator.InetAddressLocator;

import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.FilterDef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.psiprobe.Utils;
import com.googlecode.psiprobe.beans.JMXContainerListenerBean;
import com.googlecode.psiprobe.beans.ResourceResolver;
import com.googlecode.psiprobe.model.Application;
import com.googlecode.psiprobe.model.ApplicationParam;
import com.googlecode.psiprobe.model.ApplicationResource;
import com.googlecode.psiprobe.model.ApplicationSession;
import com.googlecode.psiprobe.model.Attribute;
import com.googlecode.psiprobe.model.Connector;
import com.googlecode.psiprobe.model.FilterInfo;
import com.googlecode.psiprobe.model.ServletInfo;
import com.googlecode.psiprobe.model.ServletMapping;

/**
 * 
 * @author vlavanya@novell.com
 */
public class JMXApplicationUtils
{

	private static Log logger = LogFactory.getLog(ApplicationUtils.class);

	private MBeanServerConnection conn = null;

	public static Application getApplication(Context context)
	{
		return getApplication(context.getName(), null, null, false);
	}

	/**
	 * Creates Application instance from Tomcat Context object. If
	 * ResourceResolver is passed the method will also collect additional
	 * information about the application such as session count, session
	 * attribute count, application attribute count, servlet count, servlet
	 * stats summary and datasource usage summary. Collecting additional
	 * information can be CPU intensive and time consuming so this should be
	 * avoided unless absolutely required. Some datasource implementations
	 * (c3p0) are known to be prone to internal deadlocks, so this method can
	 * also hang is datasource usage stats is to be collected.
	 * 
	 * @param context
	 * @param resourceResolver
	 * @param calcSize
	 * @return Application object
	 */
	public static Application getApplication(String contextName, MBeanServerConnection conn,
	        ResourceResolver resourceResolver, boolean calcSize)
	{
		Application app = new Application();
		try
		{
			ObjectName webModuleOName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			app.setName(contextName);
			app.setDocBase((String) conn.getAttribute(webModuleOName, "docBase"));
			String displayName = (String) conn.getAttribute(webModuleOName, "displayName");
			if (displayName == null)
				displayName = contextName;
			app.setDisplayName(displayName);
			if (((String) conn.getAttribute(webModuleOName, "stateName")).equalsIgnoreCase("STARTED"))
				app.setAvailable(true);
			else
				app.setAvailable(false);
			app.setDistributable(((Boolean) conn.getAttribute(webModuleOName, "distributable")).booleanValue());
			app.setSessionTimeout(((Integer) conn.getAttribute(webModuleOName, "sessionTimeout")).intValue());
			app.setServletVersion("0");
			app.setContextAttributeCount(0);

			ObjectName managerOName = new ObjectName("Catalina:type=Manager,context=" + contextName + ",host=localhost");
			app.setSessionCount(((Integer) conn.getAttribute(managerOName, "activeSessions")).intValue());
			String sessions = (String) conn.invoke(managerOName, "listSessionIds", null, null);
			app.setSerializable(true);
			app.setSessionAttributeCount(0);
			app.setSize(0);

			collectApplicationServletStats(contextName, app, conn);
			app.setDataSourceBusyScore(0);
			app.setDataSourceEstablishedScore(0);

			/*
			 * if (resourceResolver.supportsPrivateResources() &&
			 * app.isAvailable()) { int[] scores =
			 * getApplicationDataSourceUsageScores(context, resourceResolver);
			 * app.setDataSourceBusyScore(scores[0]);
			 * app.setDataSourceEstablishedScore(scores[1]); } }
			 */
		}
		catch (Exception e)
		{
		}
		return app;
	}

	/**
	 * Calculates Sum of requestCount, errorCount and processingTime for all
	 * servlets for the give application. It also works out minimum value of
	 * minTime and maximum value for maxTime for all servlets.
	 * 
	 * @param context
	 * @param app
	 */

	public static void collectApplicationServletStats(String contextName, Application app, MBeanServerConnection conn)
	{
		int reqCount = 0;
		int errCount = 0;
		long procTime = 0;
		long minTime = Long.MAX_VALUE;
		long maxTime = 0;
		try
		{

			ObjectName webModuleOName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			String[] childONames = (String[]) conn.getAttribute(webModuleOName, "servlets");
			for (int i = 0; i < childONames.length; i++)
			{
				ObjectName servletOName = new ObjectName(childONames[i]);
				reqCount += ((Integer) conn.getAttribute(servletOName, "requestCount")).intValue();
				errCount += ((Integer) conn.getAttribute(servletOName, "errorCount")).intValue();
				procTime += ((Long) conn.getAttribute(servletOName, "processingTime")).longValue();
				if (((Integer) conn.getAttribute(servletOName, "requestCount")).intValue() > 0)
					minTime = Math.min(minTime, ((Long) conn.getAttribute(servletOName, "minTime")).longValue());
				maxTime = Math.max(maxTime, ((Long) conn.getAttribute(servletOName, "maxTime")).longValue());
			}
			app.setServletCount(childONames.length);
			app.setRequestCount(reqCount);
			app.setErrorCount(errCount);
			app.setProcessingTime(procTime);
			app.setMinTime(minTime == Long.MAX_VALUE ? 0 : minTime);
			app.setMaxTime(maxTime);
			app.setAvgTime(reqCount == 0 ? 0: procTime/reqCount);
		}
		catch (Exception e)
		{

		}
	}
	
	public static ModelAndView getXMLView(String contextName, Context context, String displayTarget, String viewName, String downloadUrl, MBeanServerConnection conn) throws IOException
    {
	    ModelAndView mv = new ModelAndView(viewName);
	    try
        {
			if(conn != null)
			{
				ObjectName webModuleName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost"+contextName+",J2EEApplication=none,J2EEServer=none");
				String xmlString = (String) conn.getAttribute(webModuleName, "deploymentDescriptor");
				String encoding = null;
				String formattedString = Utils.getFormattedString(xmlString);
				InputStream is = new ByteArrayInputStream(formattedString.getBytes());
				mv.addObject("content", Utils.highlightStream("web.xml", is, "xml", encoding == null ? "ISO-8859-1" : encoding));
				mv.addObject("downloadUrl", downloadUrl);
			}
        }
        catch (MalformedObjectNameException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        catch (NullPointerException e)
        {
	    }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	    return mv;
    }

	
	public static List<Application> collectApplicationsServletStats(MBeanServerConnection conn)
	{
		List<Application> applications = getApplications(conn);
		Iterator it = applications.iterator();
		while(it.hasNext())
		{
			Application app = (Application) it.next();
			collectApplicationServletStats(app.getName(), app, conn);
		}
		return applications;
	}

	public static int[] getApplicationDataSourceUsageScores(Context context, ResourceResolver resolver)
	{
		logger.debug("Calculating datasource usage score");

		int[] scores = new int[]
		{ 0, 0 };
		List appResources;
		try
		{
			appResources = resolver.getApplicationResources(context);
		}
		catch (NamingException e)
		{
			throw new RuntimeException(e);
		}
		for (Iterator it = appResources.iterator(); it.hasNext();)
		{
			ApplicationResource appResource = (ApplicationResource) it.next();
			if (appResource.getDataSourceInfo() != null)
			{
				scores[0] = Math.max(scores[0], appResource.getDataSourceInfo().getBusyScore());
				scores[1] = Math.max(scores[1], appResource.getDataSourceInfo().getEstablishedScore());
			}
		}
		return scores;
	}

	public static ApplicationSession getApplicationSession(Session session, boolean calcSize, boolean addAttributes)
	{
		ApplicationSession sbean = null;
		if (session != null && session.isValid())
		{
			sbean = new ApplicationSession();

			sbean.setId(session.getId());
			sbean.setCreationTime(new Date(session.getCreationTime()));
			sbean.setLastAccessTime(new Date(session.getLastAccessedTime()));
			sbean.setMaxIdleTime(session.getMaxInactiveInterval() * 1000);
			sbean.setManagerType(session.getManager().getClass().getName());
			sbean.setInfo(session.getInfo());

			boolean sessionSerializable = true;
			int attributeCount = 0;
			long size = 0;

			HttpSession httpSession = session.getSession();
			Set processedObjects = new HashSet(1000);
			try
			{
				for (Enumeration e = httpSession.getAttributeNames(); e.hasMoreElements();)
				{
					String name = (String) e.nextElement();
					Object o = httpSession.getAttribute(name);
					sessionSerializable = sessionSerializable && o instanceof Serializable;

					long oSize = 0;
					if (calcSize)
					{
						try
						{
							oSize += Instruments.sizeOf(name, processedObjects);
							oSize += Instruments.sizeOf(o, processedObjects);
						}
						catch (Throwable th)
						{
							logger.error("Cannot estimate size of attribute \"" + name + "\"", th);
							//
							// make sure we always re-throw ThreadDeath
							//
							if (e instanceof ThreadDeath)
							{
								throw (ThreadDeath) e;
							}
						}
					}

					if (addAttributes)
					{
						Attribute saBean = new Attribute();
						saBean.setName(name);
						saBean.setType(ClassUtils.getQualifiedName(o.getClass()));
						saBean.setValue(o);
						saBean.setSize(oSize);
						saBean.setSerializable(o instanceof Serializable);
						sbean.addAttribute(saBean);
					}
					attributeCount++;
					size += oSize;
				}
				String lastAccessedIP = (String) httpSession.getAttribute(ApplicationSession.LAST_ACCESSED_BY_IP);
				if (lastAccessedIP != null)
				{
					sbean.setLastAccessedIP(lastAccessedIP);
				}
				try
				{
					sbean.setLastAccessedIPLocale(InetAddressLocator.getLocale(InetAddress.getByName(lastAccessedIP)
					        .getAddress()));
				}
				catch (Throwable e)
				{
					logger.error("Cannot determine Locale of " + lastAccessedIP);
					//
					// make sure we always re-throw ThreadDeath
					//
					if (e instanceof ThreadDeath)
					{
						throw (ThreadDeath) e;
					}
				}

			}
			catch (IllegalStateException e)
			{
				logger.info("Session appears to be invalidated, ignore");
			}

			sbean.setObjectCount(attributeCount);
			sbean.setSize(size);
			sbean.setSerializable(sessionSerializable);
		}

		return sbean;
	}

	public static List getApplicationAttributes(Context context)
	{
		List attrs = new ArrayList();
		ServletContext servletCtx = context.getServletContext();
		for (Enumeration e = servletCtx.getAttributeNames(); e.hasMoreElements();)
		{
			String attrName = (String) e.nextElement();
			Object attrValue = servletCtx.getAttribute(attrName);

			Attribute attr = new Attribute();
			attr.setName(attrName);
			attr.setValue(attrValue);
			attr.setType(ClassUtils.getQualifiedName(attrValue.getClass()));
			attrs.add(attr);
		}
		return attrs;
	}

	public static List getApplicationInitParams(Context context)
	{
		// We'll try to determine if a parameter value comes from a deployment
		// descriptor or a context descriptor.
		// assumption: Context.findParameter() returns only values of parameters
		// that are declared in a deployment descriptor.
		// If a parameter is declared in a context descriptor with
		// override=false and redeclared in a deployment descriptor,
		// Context.findParameter() still returns its value, even though the
		// value is taken from a context descriptor.
		// context.findApplicationParameters() returns all parameters that are
		// declared in a context descriptor regardless
		// of whether they are overridden in a deployment descriptor or not or
		// not.

		// creating a set of parameter names that are declared in a context
		// descriptor
		// and can not be ovevridden in a deployment descriptor.
		Set nonOverridableParams = new HashSet();
		ApplicationParameter[] appParams = context.findApplicationParameters();
		for (int i = 0; i < appParams.length; i++)
		{
			if (appParams[i] != null && !appParams[i].getOverride())
			{
				nonOverridableParams.add(appParams[i].getName());
			}
		}

		List initParams = new ArrayList();
		ServletContext servletCtx = context.getServletContext();
		for (Enumeration e = servletCtx.getInitParameterNames(); e.hasMoreElements();)
		{
			String paramName = (String) e.nextElement();

			ApplicationParam param = new ApplicationParam();
			param.setName(paramName);
			param.setValue(servletCtx.getInitParameter(paramName));
			// if the parameter is declared in a deployment descriptor
			// and it is not declared in a context descriptor with
			// override=false,
			// the value comes from the deployment descriptor
			param.setFromDeplDescr(context.findParameter(paramName) != null
			        && !nonOverridableParams.contains(paramName));
			initParams.add(param);
		}

		return initParams;
	}

	/*
	 * public static ServletInfo getApplicationServlet(Context context, String
	 * servletName) { Container c = context.findChild(servletName);
	 * 
	 * if (c instanceof Wrapper) { Wrapper w = (Wrapper) c; return
	 * getServletInfo(w, context.getName()); } else { return null; } }
	 */

	private static ServletInfo getServletInfo(String contextName, ObjectName servletOName, MBeanServerConnection conn)
	{
		ServletInfo si = new ServletInfo();
		try
		{
			si.setApplicationName(contextName);
			String servletFullName = servletOName.toString();
			StringTokenizer st = new StringTokenizer(servletFullName, "=,");
			while (st.hasMoreTokens())
			{
				String key = st.nextToken();
				String val = st.nextToken();
				if (key.equals("name"))
					si.setServletName(val);
			}

			si.setServletClass((String) conn.getAttribute(servletOName, "servletClass"));

			long available = ((Long) conn.getAttribute(servletOName, "available")).longValue();
			si.setAvailable(available == 0 ? true : false);

			si.setLoadOnStartup(((Integer) conn.getAttribute(servletOName, "loadOnStartup")).intValue());
			si.setRunAs((String) conn.getAttribute(servletOName, "runAs"));
			String[] ms = (String[]) conn.invoke(servletOName, "findMappings", null, null);
			for (int i = 0; i < ms.length; i++)
			{
				si.getMappings().add(ms[i]);
			}
			si.setAllocationCount(0);
			si.setErrorCount(((Integer) conn.getAttribute(servletOName, "errorCount")).intValue());
			si.setLoadTime(((Long) conn.getAttribute(servletOName, "loadTime")).longValue());
			si.setMaxInstances(((Integer) conn.getAttribute(servletOName, "maxInstances")).intValue());
			si.setMaxTime(((Long) conn.getAttribute(servletOName, "maxTime")).longValue());

			long minTime = ((Long) conn.getAttribute(servletOName, "minTime")).longValue();
			si.setMinTime(minTime == Long.MAX_VALUE ? 0 : minTime);

			si.setProcessingTime(((Long) conn.getAttribute(servletOName, "processingTime")).longValue());
			si.setRequestCount(((Integer) conn.getAttribute(servletOName, "requestCount")).intValue());
			si.setSingleThreaded(((Boolean) conn.getAttribute(servletOName, "singleThreaded")).booleanValue());
		}
		catch (Exception e)
		{
		}
		return si;
	}

	public static List getApplicationServlets(String contextName, MBeanServerConnection conn)
	{
		try
		{
			ObjectName webModuleOName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			String[] cns = (String[]) conn.getAttribute(webModuleOName, "servlets");
			List servlets = new ArrayList(cns.length);
			for (int i = 0; i < cns.length; i++)
			{
				ObjectName servletOName = new ObjectName(cns[i]);
				servlets.add(getServletInfo(contextName, servletOName, conn));
			}

			return servlets;

		}
		catch (Exception e)
		{
		}
		return null;
	}

	public static List getApplicationServletMaps(String contextName, MBeanServerConnection conn)
	{
		try
		{

			ObjectName webModuleOName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			// String[] cns = (String[]) conn.getAttribute(webModuleOName,
			// "servlets");
			// List servlets = new ArrayList(cns.length);
			String[] ms = (String[]) conn.invoke(webModuleOName, "findServletMappings", null, null);
			List servletMaps = new ArrayList(ms.length);
			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i] != null)
				{
					ServletMapping sm = new ServletMapping();
					sm.setApplicationName(contextName);
					sm.setUrl(ms[i]);
					Object params[] =
					{ ms[i] };
					String opSig[] =
					{ String.class.getName() };
					String servletName = (String) conn.invoke(webModuleOName, "findServletMapping", params, opSig);
					sm.setServletName(servletName);
					ObjectName servletOName = new ObjectName("Catalina:j2eeType=Servlet,name=" + servletName
					        + ",WebModule=//localhost" + contextName + ",J2EEApplication=none,J2EEServer=none");
					sm.setServletClass((String) conn.getAttribute(servletOName, "servletClass"));
					long available = ((Long) conn.getAttribute(servletOName, "available")).longValue();
					if (available == 0)
						sm.setAvailable(true);
					else
						sm.setAvailable(false);

					servletMaps.add(sm);
				}

			}

			return servletMaps;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;

	}

	public static FilterInfo getApplicationFilter(Context context, String filterName)
	{
		FilterDef fd = context.findFilterDef(filterName);
		if (fd != null)
		{
			return getFilterInfo(fd);
		}
		else
		{
			return null;
		}
	}

	private static FilterInfo getFilterInfo(FilterDef fd)
	{
		FilterInfo fi = new FilterInfo();
		fi.setFilterName(fd.getFilterName());
		fi.setFilterClass(fd.getFilterClass());
		fi.setFilterDesc(fd.getDescription());
		return fi;
	}

	public static List getApplicationFilters(Context context)
	{
		FilterDef[] fds = context.findFilterDefs();
		List filterDefs = new ArrayList(fds.length);
		for (int i = 0; i < fds.length; i++)
		{
			if (fds[i] != null)
			{
				FilterInfo fi = getFilterInfo(fds[i]);
				filterDefs.add(fi);
			}
		}
		return filterDefs;
	}

	public static List getApplications(MBeanServerConnection conn)
	{
		List applications = null;

		if (conn != null)
		{
			try
			{
				Set names = new TreeSet(conn.queryNames(new ObjectName("Catalina:j2eeType=WebModule,*"), null));
				applications = new ArrayList(names.size());
				Iterator it = names.iterator();
				while (it.hasNext())
				{
					ObjectName objName = (ObjectName) it.next();
					String name = (String) conn.getAttribute(objName, "name");
					if (name.length() == 0)
					{
						name = "/";
					}
					applications.add(JMXApplicationUtils.getApplication(name, conn, null, false));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return applications;
	}

    public static boolean getApplicationStatus(String contextName, MBeanServerConnection conn)
    {
        if (contextName.length() == 0)
            contextName = "/";
        ObjectName objName;
        boolean available = false;
        try
        {
            objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
                    + ",J2EEApplication=none,J2EEServer=none");
            String started = "STARTED";
            started = (String) conn.getAttribute(objName, "stateName");
            available = started.equalsIgnoreCase("STARTED");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return available;

    }
	
	public static boolean reloadApplication(String contextName, MBeanServerConnection conn)
	{
		if (contextName.length() == 0)
			contextName = "/";
		ObjectName objName;
		boolean available = false;
		try
		{
			objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			conn.invoke(objName, "reload", null, null);
			String started = "STARTED";

			started = (String) conn.getAttribute(objName, "stateName");
			available = started.equalsIgnoreCase("STARTED");

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return available;

	}
	
	public static boolean toggleApplication(String contextName, MBeanServerConnection conn)
	{
		if (contextName.length() == 0)
			contextName = "/";
		boolean available = false;
		try
		{
			ObjectName objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
			        + ",J2EEApplication=none,J2EEServer=none");
			if (((String) conn.getAttribute(objName, "stateName")).equalsIgnoreCase("STARTED"))
			{
				conn.invoke(objName, "stop", null, null);
			}
			else
			{
				conn.invoke(objName, "start", null, null);
			}
			String started = (String) conn.getAttribute(objName, "stateName");
			available = started.equalsIgnoreCase("STARTED");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return available;
	}
	
	public static void stopApplication(String contextName, MBeanServerConnection conn)
	{
		if(conn!=null)
    	{
			ObjectName objName;
            try
            {
	            objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
	                    + ",J2EEApplication=none,J2EEServer=none");
	            conn.invoke(objName, "stop", null, null);
            }
            catch (Exception e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
    	}	
	}

	public static void startApplication(String contextName, MBeanServerConnection conn)
	{
		if(conn!=null)
    	{
			ObjectName objName;
            try
            {
	            objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
	                    + ",J2EEApplication=none,J2EEServer=none");
	            conn.invoke(objName, "start", null, null);
            }
            catch (Exception e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
    	}	
	}

	public static List getServlets(String contextName, MBeanServerConnection conn)
    {
		List servlets = new ArrayList();
		try
		{
			if (contextName == null)
			{
				Set names = new TreeSet(conn.queryNames(new ObjectName(
				        "Catalina:j2eeType=WebModule,*"), null));
				Iterator it = names.iterator();
				while(it.hasNext())
				{
					ObjectName objName = (ObjectName) it.next();
					String name = (String) conn.getAttribute(objName, "name");
					if (name.length() == 0)
						name = "/";
					List appServlets = JMXApplicationUtils.getApplicationServlets(name, conn);
					servlets.addAll(appServlets);
				}
			}
			else
			{
				servlets = JMXApplicationUtils.getApplicationServlets(contextName, conn);
			}
		}
		catch (Exception e)
		{
		}
		return servlets;	    
    }

	public static List getServletMaps(String contextName, MBeanServerConnection conn)
    {
		List servletMaps = new ArrayList();
		
		try
		{
			if (contextName == null)
			{
				Set names = new TreeSet(conn.queryNames(new ObjectName("Catalina:j2eeType=WebModule,*"), null));
				Iterator it = names.iterator();
				while (it.hasNext())
				{
					ObjectName objName = (ObjectName) it.next();
					String name = (String) conn.getAttribute(objName, "name");
					if (name.length() == 0)
						name = "/";
					servletMaps.addAll(JMXApplicationUtils.getApplicationServletMaps(name, conn));
				}
			}
			else
			{
				servletMaps.addAll(JMXApplicationUtils.getApplicationServletMaps(contextName, conn));
			}
		}
		catch (Exception e)
		{
		}
		
		return servletMaps;
	 
    }

	public static Connector getConnector(String connectorName, JMXContainerListenerBean jmxContainerListener)
    {
		List connectors;
		Connector connector = null;
		try
		{
			if (connectorName != null)
			{
				connectors = jmxContainerListener.getConnectors(false);

				for (int i = 0; i < connectors.size(); i++)
				{
					Connector p = (Connector) connectors.get(i);
					if (connectorName.equals(p.getName()))
					{
						connector = p;
						break;
					}
				}
			}
		}
		catch (Exception e)
		{

		}
		return connector;
	}

	public static void undeployWebApp(String contextName, MBeanServerConnection conn)
    {
		try
		{
			if(conn != null){
				ObjectName objName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost" + contextName
						+ ",J2EEApplication=none,J2EEServer=none");
				conn.invoke(objName, "stop", null, null);
				conn.invoke(objName, "destroy", null, null);	
			}
		}
		catch (NullPointerException e)
        {
	    }
		catch (Exception e)
		{
			e.printStackTrace();
		}

	    
    }

	public static File getXLMFile(String contextName, Context context, String downloadTarget, MBeanServerConnection conn)
    {
		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir+File.separator+"web.xml");
		FileWriter writer = null;
	    try
        {
	        ObjectName webModuleName = new ObjectName("Catalina:j2eeType=WebModule,name=//localhost"+contextName+",J2EEApplication=none,J2EEServer=none");
	        String xmlString = (String) conn.getAttribute(webModuleName, "deploymentDescriptor");
	        String formattedString = Utils.getFormattedString(xmlString);
	        writer = new FileWriter(file);
	        writer.write(formattedString);

        }
        catch (MalformedObjectNameException e)
        {
        }
        catch (NullPointerException e)
        {
        }
        catch(Exception e)
        {
        }
        finally
        {
        	try
            {
	            writer.close();
            }
            catch (IOException e1)
            {
            }
        }

		return file;
	    
    }

}
