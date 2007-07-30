/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.bes.jsdl;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.jsdl.ApplicationRedux;
import edu.virginia.vcgr.genii.container.jsdl.IJobPlanProvider;
import edu.virginia.vcgr.genii.container.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.UnsupportedJSDLElement;

public class SimpleApplicationRedux extends ApplicationRedux
{
	static public QName JSDL_POSIX_APP_QNAME = 
		new QName(GenesisIIConstants.JSDL_POSIX_NS, "POSIXApplication");
	static public QName HPC_APP_QNAME =
		new QName(GenesisIIConstants.JSDL_HPC_NS, "HPCProfileApplication");
	
	private PosixApplicationRedux _posixApplication = null;
	private HPCProfileApplicationRedux _hpcApplication = null;
	
	public SimpleApplicationRedux(IJobPlanProvider provider,
		Application_Type applicationType)
	{
		super(provider, applicationType);
	}
	
	public PosixApplicationRedux getPosixApplication()
	{
		return _posixApplication;
	}
	
	public HPCProfileApplicationRedux getHPCApplication()
	{
		return _hpcApplication;
	}
	
	public void consume() throws JSDLException
	{
		Application_Type application = getApplication();
		
		if (application != null)
		{
			understandApplicationName(application.getApplicationName());
			understandApplicationVersion(application.getApplicationVersion());
			understandApplicationDescription(application.getDescription());
			
			MessageElement []any = application.get_any();
			for (MessageElement anyElement : any)
			{
				QName extensionQName = anyElement.getQName();
				if (extensionQName.equals(JSDL_POSIX_APP_QNAME))
				{
					if (_posixApplication != null)
						throw new InvalidJSDLException(
							"Cannot have more than one \"" + JSDL_POSIX_APP_QNAME
							+ "\" element.");
					
					try
					{
						_posixApplication = 
							((IPOSIXProvider)getProvider()).createPosixApplication(
								(POSIXApplication_Type)ObjectDeserializer.toObject(
									anyElement,
									POSIXApplication_Type.class));
						_posixApplication.consume();
					}
					catch (ResourceException re)
					{
						throw new InvalidJSDLException(re.getMessage());
					}
				} else if (extensionQName.equals(HPC_APP_QNAME))
				{
					if (_hpcApplication != null)
						throw new InvalidJSDLException(
							"Cannot have more than one \"" + HPC_APP_QNAME
							+ "\" element.");
					
					try
					{
						_hpcApplication =
							((IHPCProvider)getProvider()).createHPCApplication(
								(HPCProfileApplication_Type)ObjectDeserializer.toObject(
									anyElement,
									HPCProfileApplication_Type.class));
						_hpcApplication.consume();
					}
					catch (ResourceException re)
					{
						throw new InvalidJSDLException(re.getMessage());
					}
				} else
				{
					throw new UnsupportedJSDLElement(extensionQName);
				}
			}
		}
	}
	
	public void understandApplicationName(String name)
	{
		// I ignore application names....
	}
	
	public void verifyComplete() throws JSDLException
	{
		if (_posixApplication != null)
			_posixApplication.verifyComplete();
	}
}
