/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl.hpcfse;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = HPCFSEConstants.WSSEC_SECEXT_NS, name = "UsernameToken")
public class UsernameToken implements SecurityToken, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = HPCFSEConstants.WSSEC_SECEXT_NS, name = "Username", required = true)
	private String _username;

	@XmlElement(namespace = HPCFSEConstants.WSSEC_SECEXT_NS, name = "Password", required = true)
	private String _password;

	/**
	 * Only used for XML Unmarshalling.
	 */
	@SuppressWarnings("unused")
	private UsernameToken()
	{
	}

	public UsernameToken(String username, String password)
	{
		username(username);
		password(password);
	}

	final public void username(String username)
	{
		if (username == null)
			throw new IllegalArgumentException("User name cannot be null.");

		_username = username;
	}

	final public String username()
	{
		return _username;
	}

	final public void password(String password)
	{
		if (password == null)
			throw new IllegalArgumentException("Password cannot be null.");

		_password = password;
	}

	final public String password()
	{
		return _password;
	}
}
