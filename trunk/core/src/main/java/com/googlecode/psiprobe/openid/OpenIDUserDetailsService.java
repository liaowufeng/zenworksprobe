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

import java.io.InputStream;
import java.util.Properties;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

public class OpenIDUserDetailsService implements UserDetailsService,
        AuthenticationUserDetailsService<OpenIDAuthenticationToken>
{

    private static final String OPENID_CONFIG_FILE = "openid.properties";

    public OpenIDUserDetailsService()
    {

    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException
    {
        throw new UsernameNotFoundException("not found");
    }

    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException
    {

        String id = token.getIdentityUrl();
        String role = null;
        User probeUser = null;
        if (id == null)
            throw new UsernameNotFoundException(id);
        for (OpenIDAttribute attr : token.getAttributes())
        {
            if (attr.getName().equals("role"))
            {
                role = attr.getValues().get(0);
                probeUser = new User(id, "", true, true, true, true, AuthorityUtils.createAuthorityList(new String[]
                { role }));
            }
        }

        /*
         * The role will be returned only by ZENworks OpenID provider. The
         * following code is added to support other OpenId providers like yahoo.
         * When the openId user id is returned without role information,
         * assuming that the provider is something other than ZENworks and the
         * user will be created with the role information in
         * WEB-INF/classes/openid.properties
         */
        if (role == null)
        {
            try
            {
                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(OPENID_CONFIG_FILE);
                Properties prop = new Properties();
                prop.load(in);
                role = prop.getProperty("openid_default_role");
                if (role != null)
                {
                    System.out.println("role is " + role);
                    probeUser = new User(id, "", true, true, true, true,
                            AuthorityUtils.createAuthorityList(new String[]
                            { role }));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (probeUser == null)
            throw new UsernameNotFoundException(id);
        return probeUser;
    }

}