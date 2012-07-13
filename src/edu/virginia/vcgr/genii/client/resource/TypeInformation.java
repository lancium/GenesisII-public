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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.XMLCommandFunction;

public class TypeInformation
{
	static private Log _logger = LogFactory.getLog(TypeInformation.class);
	
	private EndpointReferenceType _epr;
	
	private GUID _containerID;
	private String _pureURL = null;
	private PortType []_implementedPortTypes;
	private Collection<JavaCommandFunction> _commandFunctions;
	
	public TypeInformation(EndpointReferenceType epr)
	{
		_epr = epr;
		_implementedPortTypes = EPRUtils.getImplementedPortTypes(epr);
		
		_containerID = EPRUtils.getGeniiContainerID(epr);
		
		if (_implementedPortTypes == null)
		{
			_implementedPortTypes = new PortType[0];
			if (epr.getAddress() != null &&
				epr.getAddress().get_value() != null &&
				epr.get_any() == null &&
				epr.getMetadata() == null &&
				epr.getReferenceParameters() == null)
			{
				_pureURL = epr.getAddress().get_value().toString();
			}
		}
		
		_commandFunctions = new LinkedList<JavaCommandFunction>();
		MetadataType mdt = epr.getMetadata();
		if (mdt != null)
		{
			MessageElement []any = mdt.get_any();
			if (any != null)
			{
				for (MessageElement e : any)
				{
					QName eName = e.getQName();
					if (eName.equals(
						GenesisIIConstants.COMMAND_FUNCTION_QNAME))
					{
						try
						{
							XMLCommandFunction xf = ObjectDeserializer.toObject(
								e, XMLCommandFunction.class);
							_commandFunctions.add(new JavaCommandFunction(xf));
						}
						catch (ResourceException re)
						{
							_logger.warn("Unable to deserialize command function description.", 
								re);
						}
					}
				}
			}
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
	
	public Collection<JavaCommandFunction> commandFunctions()
	{
		return Collections.unmodifiableCollection(_commandFunctions);
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
	
	public boolean isLightweightExport()
	{
		return hasPortType(WellKnownPortTypes.EXPORTED_LIGHTWEIGHT_ROOT_SERVICE_PORT_TYPE);
	}
	
	public boolean isExport()
	{
		return hasPortType(WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE);
	}
	
	public boolean isFSProxy()
	{
		return hasPortType(WellKnownPortTypes.EXPORTED_FSPROXY_ROOT_SERVICE_PORT_TYPE);
	}
	
	public boolean isQueue()
	{
		return hasPortType(QueueConstants.QUEUE_PORT_TYPE);
	}
	
	public boolean isResourceFork()
	{
		return hasPortType(WellKnownPortTypes.RESOURCE_FORK_PORT_TYPE);
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
		return hasPortType(WellKnownPortTypes.RNS_PORT_TYPE);
	}
	
	public boolean isEnhancedRNS()
	{
		return hasPortType(WellKnownPortTypes.ENHANCED_RNS_PORT_TYPE);
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
	
	public boolean isEpiResolver()
	{
		return hasPortType(WellKnownPortTypes.ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE);
	}
	
	public boolean isUnknown()
	{
		return (_implementedPortTypes == null)	||
			(_implementedPortTypes.length == 0);
	}
	
	public boolean isPureURL()
	{
		return _pureURL != null;
	}
	
	public boolean isJDBCURL()
	{
		return _pureURL != null && _pureURL.toString().startsWith("jdbc:");
	}
	
	public boolean isTool()
	{
		return isPureURL() && _epr.getAddress().get_value().toString().startsWith("urn:genii-tool:");
	}
	
	public String getTypeDescription()
	{
		if (isByteIO())
			return describeByteIO();
		else if (isPureURL())
			return "pure-url";
		else
		{
			PortType highestRank = PortType.getHighestRankedPortType(
				_implementedPortTypes);
			return (highestRank == null) ? "" : highestRank.getDescription();
		}
	}

	public long getByteIOSize()
	{
		try
		{
			Long value = null;
			
			if (isRByteIO())
			{
				RandomByteIORP rp = (RandomByteIORP)ResourcePropertyManager.createRPInterface(
					_epr, OGSARP.class, RandomByteIORP.class);
				value = rp.getSize();
			} else if (isSByteIO())
			{
				StreamableByteIORP rp = (StreamableByteIORP)ResourcePropertyManager.createRPInterface(
					_epr, OGSARP.class, StreamableByteIORP.class);
				value = rp.getSize();
			} else if (isSByteIOFactory())
			{
				StreamableByteIORP rp = (StreamableByteIORP)ResourcePropertyManager.createRPInterface(
					_epr, OGSARP.class, StreamableByteIORP.class);
				value = rp.getSize();
			}
			
			if (value == null)
				return -1L;
			return value.longValue();
		}
		catch (Throwable t)
		{
			return -1L;
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
	
	public GUID getGenesisIIContainerID()
	{
		return _containerID;
	}
	
	/**
	 * Return the name of the GenesisII top-level port type that most closely matches the given resource.
	 * (Note that the given resource is not necessarily a GenesisII resource.)
	 * This function is a hack and it should be removed.
	 * It is used by ReplicateTool and AutoReplicate.
	 */
	public String getBestMatchServiceName()
	{
		if (isEpiResolver())
			return "GeniiResolverPortType";
		if (isResourceFork() || isIDP())
			return null;
		if (isByteIO() && isRNS())
			return null;
		if (isByteIO())
			return "RandomByteIOPortType";
		if (isRNS())
			return "EnhancedRNSPortType";
		return null;
	}
}
