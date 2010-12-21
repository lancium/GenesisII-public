package edu.virginia.vcgr.genii.client.queue;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

@XmlRootElement(namespace = QueueConstants.QUEUE_NS,
	name = QueueConstants.CURRENT_RESOURCE_INFORMATION_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class CurrentResourceInformation
{
	@XmlAttribute(name = "max-slots", required = true)
	private int _maxSlots;
	
	@XmlAttribute(name = "current-slots-used", required = true)
	private int _currentSlotsUsed;
	
	@XmlAttribute(name = "is-accepting-activities", required = true)
	private boolean _isAcceptingActivities;
	
	@XmlAttribute(name = "processor-architecture", required = true)
	private ProcessorArchitecture _processorArchitecture;
	
	@XmlAttribute(name = "operating-system-type", required = true)
	private OperatingSystemNames _operatingSystemType;
	
	@XmlAttribute(name = "operating-system-version", required = false)
	private String _operatingSystemVerison;
	
	@XmlAttribute(name = "physical-memory", required = false)
	private Double _physicalMemory;
	
	@XmlAttribute(name = "resource-manager-type", required = true)
	private ResourceManagerType _resourceManagerType = null;
	
	@XmlAttribute(name = "available", required = true)
	private boolean _available;
	
	@XmlElement(namespace = QueueConstants.QUEUE_NS, name = "last-updated",
		nillable = true, required = false)
	private Calendar _lastUpdated = null;
	
	@XmlElement(namespace = QueueConstants.QUEUE_NS, name = "next-update",
		nillable = true, required = false)
	private Calendar _nextUpdate = null;
	
	protected CurrentResourceInformation()
	{
		// This is for JAXB Only
		this(-1, -1, false, 
			ProcessorArchitecture.other,
			OperatingSystemNames.other,
			"<unknown>", null,
			ResourceManagerType.Unknown, false, null, null);
	}
	
	public CurrentResourceInformation(int maxSlots, int currentSlotsUsed,
		boolean isAcceptingActivities, 
		ProcessorArchitecture processorArchitecture, 
		OperatingSystemNames osType, String osVersion, 
		Double physicalMemory, ResourceManagerType resourceManagerType,
		boolean available, Date lastUpdated, Date nextUpdate)
	{
		_maxSlots = maxSlots;
		_currentSlotsUsed = currentSlotsUsed;
		_isAcceptingActivities = isAcceptingActivities;
		_processorArchitecture = processorArchitecture;
		_operatingSystemType = osType;
		_operatingSystemVerison = osVersion;
		_physicalMemory = physicalMemory;
		_resourceManagerType = resourceManagerType;
		
		_available = available;
		if (lastUpdated != null)
		{
			_lastUpdated = Calendar.getInstance();
			_lastUpdated.setTime(lastUpdated);
		}
		if (nextUpdate != null)
		{
			_nextUpdate = Calendar.getInstance();
			_nextUpdate.setTime(nextUpdate);
		}
	}
	
	final public int maxSlots()
	{
		return _maxSlots;
	}
	
	final public int currentSlotsUsed()
	{
		return _currentSlotsUsed;
	}
	
	final public boolean isAcceptingActivities()
	{
		return _isAcceptingActivities;
	}
	
	final public ProcessorArchitecture processorArchitecture()
	{
		return _processorArchitecture;
	}
	
	final public OperatingSystemNames operatingSystem()
	{
		return _operatingSystemType;
	}
	
	final public String operatingSystemVersion()
	{
		return _operatingSystemVerison;
	}
	
	final public Double physicalMemory()
	{
		return _physicalMemory;
	}
	
	final public ResourceManagerType resourceManagerType()
	{
		return _resourceManagerType;
	}
	
	final public boolean available()
	{
		return _available;
	}
	
	final public Calendar lastUpdated()
	{
		return _lastUpdated;
	}
	
	final public Calendar nextUpdate()
	{
		return _nextUpdate;
	}
	
	@Override
	final public String toString()
	{
		return String.format("%d slots of %d used", 
			_currentSlotsUsed, _maxSlots);
	}
}