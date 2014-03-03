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
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = HPCFSEConstants.HPCFSE_NS, name = "Credential")
public class Credential implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElements({ @XmlElement(namespace = HPCFSEConstants.WSSEC_SECEXT_NS, name = "UsernameToken", required = false,
		type = UsernameToken.class) })
	private List<SecurityToken> _tokens = new Vector<SecurityToken>();

	@XmlAnyElement
	private List<Element> _any = new LinkedList<Element>();

	public Credential(SecurityToken... tokens)
	{
		for (SecurityToken token : tokens)
			_tokens.add(token);
	}

	public Credential()
	{
	}

	final public List<SecurityToken> tokens()
	{
		return _tokens;
	}

	final public List<Element> any()
	{
		return _any;
	}
}
