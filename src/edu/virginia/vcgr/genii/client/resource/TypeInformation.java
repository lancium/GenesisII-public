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
package edu.virginia.vcgr.genii.client.resource;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class TypeInformation
{
	private EndpointReferenceType _epr;
	
	private boolean _pureURL;
	private PortType []_implementedPortTypes;
	
	public TypeInformation(EndpointReferenceType epr)
	{
		_epr = epr;
		_pureURL = false;
		_implementedPortTypes = EPRUtils.getImplementedPortTypes(epr);
		
		if (_implementedPortTypes == null)
		{
			_implementedPortTypes = new PortType[0];
			if (epr.getAddress() != null &&
				epr.getAddress().get_value() != null &&
				epr.get_any() == null &&
				epr.getMetadata() == null &&
				epr.getReferenceParameters() == null)
				_pureURL = true;
		}
	}
	
	public EndpointReferenceType getEndpoint()
	{
		return _epr;
	}
	
	public PortType[] getImplementedPortTypes()
	{
		return _implementedPortTypes;
	}
	
	public boolean hasPortType(PortType targetPortType)
	{
		for (PortType portType : _implementedPortTypes)
		{
			if (portType.equals(targetPortType))
				return true;
		}
		
		return false;
	}

	public boolean isScheduler()
	{
		return hasPortType(WellKnownPortTypes.SCHEDULER_PORT_TYPE);
	}
	
	public boolean isBESContainer()
	{
		return hasPortType(BESConstants.GENII_BES_PORT_TYPE);
	}
	
	public boolean isBESActivity()
	{
		return hasPortType(BESActivityConstants.GENII_BES_ACTIVITY_PORT_TYPE);
	}

	public boolean isBES()
	{
		return hasPortType(BESConstants.GENII_BES_PORT_TYPE);
	}
	
	public boolean isContainer()
	{
		return hasPortType(WellKnownPortTypes.VCGR_CONTAINER_SERVICE_PORT_TYPE);
	}
	
	public boolean isRByteIO()
	{
		return hasPortType(WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}
	
	public boolean isSByteIO()
	{
		return hasPortType(WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}
	
	public boolean isSByteIOFactory()
	{
		return hasPortType(WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE);
	}
	
	public boolean isByteIO()
	{
		return isRByteIO() || isSByteIO() || isSByteIOFactory();
	}
	
	public boolean isRNS()
	{
		return hasPortType(RNSConstants.RNS_PORT_TYPE);
	}
	
	public boolean isEnhancedRNS()
	{
		return hasPortType(RNSConstants.ENHANCED_RNS_PORT_TYPE);
	}

	public boolean isIDP() {
		return hasPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE);
	}
	
	public boolean isX509IDP() {
		return hasPortType(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE);
	}

	public boolean isCounter()
	{
		return hasPortType(WellKnownPortTypes.COUNTER_PORT_TYPE);
	}
	
	public boolean isTTY()
	{
		return hasPortType(TTYConstants.TTY_PORT_TYPE);
	}
	
	public boolean isUnknown()
	{
		return (_implementedPortTypes == null)	||
			(_implementedPortTypes.length == 0);
	}
	
	public boolean isPureURL()
	{
		return _pureURL;
	}
	
	public boolean isTool()
	{
		return isPureURL() && _epr.getAddress().get_value().toString().startsWith("urn:genii-tool:");
	}
	
	public String getTypeDescription()
	{
		if (isBESActivity())
			return "BES Activity";
		else if (isBES())
			return "BES";
		else if (isTTY())
			return "TTY";
		else if (isContainer())
			return "Container";
		else if (isCounter())
			return "Counter";
		else if (isRNS())
			return "RNS";
		else if (isPureURL())
			return "pure-url";
		else if (isByteIO())
			return describeByteIO();
		else
			return null;
	}

	public long getByteIOSize()
	{
		EndpointReferenceType epr = _epr;
		
		try
		{
			if (isSByteIOFactory())
			{
				StreamableByteIOFactory factory =
					ClientUtils.createProxy(StreamableByteIOFactory.class, _epr);
				epr = factory.openStream(null).getEndpoint();
			}
			
			Long value = null;
			
			if (isRByteIO())
			{
				RandomByteIORP rp = (RandomByteIORP)ResourcePropertyManager.createRPInterface(
					epr, OGSARP.class, RandomByteIORP.class);
				value = rp.getSize();
			} else
			{
				StreamableByteIORP rp = (StreamableByteIORP)ResourcePropertyManager.createRPInterface(
					epr, OGSARP.class, StreamableByteIORP.class);
				value = rp.getSize();
			}
			
			if (value == null)
				return -1L;
			return value.longValue();
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			return -1L;
		}
		finally
		{
			if (epr != _epr)
			{
				try
				{
					GeniiCommon common = ClientUtils.createProxy(
						GeniiCommon.class, epr);
					common.destroy(new Destroy());
				}
				catch (Throwable t)
				{
				}
			}
		}
	}
	
	private Date getTimeAttribute(String attrName)
		throws RemoteException
	{
		QName attrQName = new QName(
			(isRByteIO() ? ByteIOConstants.RANDOM_BYTEIO_NS : ByteIOConstants.STREAMABLE_BYTEIO_NS),
			attrName);
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class, _epr);
		GetResourcePropertyResponse resp = proxy.getResourceProperty(attrQName);
		
		MessageElement []any = resp.get_any();
		if (any == null || any.length != 1)
			return new Date();
		
		Calendar time = ObjectDeserializer.toObject(
			any[0], Calendar.class);
		return time.getTime();
	}
	
	public Date getByteIOCreateTime()
	{
		try
		{
			if (isSByteIOFactory())
			{
				EndpointReferenceType epr = null;
				StreamableByteIOFactory factory =
					ClientUtils.createProxy(StreamableByteIOFactory.class, _epr);
				try
				{
					epr = factory.openStream(null).getEndpoint();
					TypeInformation proxy = new TypeInformation(epr);
					return proxy.getByteIOCreateTime();
				}
				finally
				{
					if (epr != null)
					{
						try
						{
							GeniiCommon common = ClientUtils.createProxy(
								GeniiCommon.class, epr);
							common.destroy(new Destroy());
						}
						catch (Throwable t)
						{
						}
					}
				}
			}
			
			return getTimeAttribute(ByteIOConstants.CREATTIME_ATTR_NAME);
		}
		catch (Throwable t)
		{
			return new Date();
		}
	}
	
	public Date getByteIOAccessTime()
	{
		try
		{
			if (isSByteIOFactory())
			{
				EndpointReferenceType epr = null;
				StreamableByteIOFactory factory =
					ClientUtils.createProxy(StreamableByteIOFactory.class, _epr);
				try
				{
					epr = factory.openStream(null).getEndpoint();
					TypeInformation proxy = new TypeInformation(epr);
					return proxy.getByteIOCreateTime();
				}
				finally
				{
					if (epr != null)
					{
						try
						{
							GeniiCommon common = ClientUtils.createProxy(
								GeniiCommon.class, epr);
							common.destroy(new Destroy());
						}
						catch (Throwable t)
						{
						}
					}
				}
			}
			
			return getTimeAttribute(ByteIOConstants.ACCESSTIME_ATTR_NAME);
		}
		catch (Throwable t)
		{
			return new Date();
		}
	}
	
	public Date getByteIOModificationTime()
	{
		try
		{
			if (isSByteIOFactory())
			{
				EndpointReferenceType epr = null;
				StreamableByteIOFactory factory =
					ClientUtils.createProxy(StreamableByteIOFactory.class, _epr);
				try
				{
					epr = factory.openStream(null).getEndpoint();
					TypeInformation proxy = new TypeInformation(epr);
					return proxy.getByteIOCreateTime();
				}
				finally
				{
					if (epr != null)
					{
						try
						{
							GeniiCommon common = ClientUtils.createProxy(
								GeniiCommon.class, epr);
							common.destroy(new Destroy());
						}
						catch (Throwable t)
						{
						}
					}
				}
			}
			
			return getTimeAttribute(ByteIOConstants.MODTIME_ATTR_NAME);
		}
		catch (Throwable t)
		{
			return new Date();
		}
	}
	
	public String describeByteIO()
	{
		long size = getByteIOSize();
		if (size < 0)
			return "[file(non-rsp.)]";
		
		return Long.toString(size);
	}
}
