package edu.virginia.vcgr.genii.container.q2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.ggf.bes.factory.BasicResourceAttributesDocumentType;
import org.ggf.bes.factory.FactoryResourceAttributesDocumentType;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.queue.QueueConstructionParameters;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeed;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeedUnits;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParameter;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParameters;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

class QueueAsBESFactoryAttributesUtilities
{
	private QueueConstructionParameters _consParms;
	private ResourceOverrides _resO;
	private Collection<BESInformation> _allBESInformation;

	private OperatingSystem_Type operatingSystem()
	{
		OperatingSystemNames osName = _resO.operatingSystemName();
		if (osName == null)
			throw new IllegalArgumentException("Operating System name cannot be null!");

		String osVersion = _resO.operatingSystemVersion();
		if (osVersion == null)
			osVersion = "";

		return new OperatingSystem_Type(new OperatingSystemType_Type(OperatingSystemTypeEnumeration.fromString(osName.name()),
			null), osVersion, null, null);
	}

	private CPUArchitecture_Type cpuArchitecture()
	{
		ProcessorArchitecture arch = _resO.cpuArchitecture();
		if (arch == null)
			throw new IllegalArgumentException("Processor Architecture cannot be null!");

		return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.fromString(arch.name()), null);
	}

	private Collection<MessageElement> supportedFilesystems()
	{
		Set<String> supportedFilesystems = new HashSet<String>();

		for (BESInformation info : _allBESInformation)
			supportedFilesystems.addAll(info.supportedFilesystems());

		Collection<MessageElement> ret = new ArrayList<MessageElement>(supportedFilesystems.size());
		for (String fs : supportedFilesystems)
			ret.add(new MessageElement(BESConstants.FILESYSTEM_SUPPORT_ATTR, fs));

		return ret;
	}

	private int totalCPUCount()
	{
		int total = 0;
		for (BESInformation info : _allBESInformation) {
			Double count = info.getCPUCount();
			if (count != null)
				total += count.intValue();
		}

		return total;
	}

	private ClockSpeed highestCPUSpeed()
	{
		double highest = 0.0;

		for (BESInformation info : _allBESInformation) {
			Double speed = info.getCPUSpeed();
			if (speed != null)
				highest = Math.max(highest, speed.doubleValue());
		}

		return new ClockSpeed(highest);
	}

	private Size largestPhysicalMemory()
	{
		double largest = 0.0;

		for (BESInformation info : _allBESInformation) {
			Double mem = info.getPhysicalMemory();
			if (mem != null)
				largest = Math.max(mem, largest);
		}

		return new Size(largest);
	}

	private Size largestVirtualMemory()
	{
		double largest = 0.0;

		for (BESInformation info : _allBESInformation) {
			Double mem = info.getVirtualMemory();
			if (mem != null)
				largest = Math.max(mem, largest);
		}

		return new Size(largest);
	}

	private Duration getLargestWallclockTimeLimit()
	{
		Double largest = null;

		for (BESInformation info : _allBESInformation) {
			Double value = info.getWallclockTimeLimit();
			if (value != null) {
				if (largest == null)
					largest = value;
				else
					largest = Math.max(largest, value);
			}
		}

		return (largest == null) ? null : new Duration(largest);
	}

	private double cpuCount()
	{
		Integer i = _resO.cpuCount();
		if (i == null)
			i = totalCPUCount();

		return i.doubleValue();
	}

	private double cpuSpeed()
	{
		ClockSpeed speed = _resO.cpuSpeed();
		if (speed == null)
			speed = highestCPUSpeed();

		return speed.as(ClockSpeedUnits.Hertz);
	}

	private double physicalMemory()
	{
		Size size = _resO.physicalMemory();
		if (size == null)
			size = largestPhysicalMemory();

		return size.as(SizeUnits.Bytes);
	}

	private double virtualMemory()
	{
		Size size = _resO.virtualMemory();
		if (size == null)
			size = largestVirtualMemory();

		return size.as(SizeUnits.Bytes);
	}

	private Long wallclockTimeLimit()
	{
		Duration d = _resO.wallclockTimeLimit();
		if (d == null)
			d = getLargestWallclockTimeLimit();

		if (d != null)
			return new Double(d.as(DurationUnits.Milliseconds)).longValue();

		return null;
	}

	private Set<MatchingParameter> allMatchingParameters()
	{
		Set<MatchingParameter> ret = new HashSet<MatchingParameter>();

		for (BESInformation info : _allBESInformation) {
			MatchingParameters parameters = info.getMatchingParameters();
			ret.addAll(parameters.getParameters());
		}

		return ret;
	}

	QueueAsBESFactoryAttributesUtilities(Collection<BESInformation> allBESInformation, QueueConstructionParameters consParms)
	{
		_allBESInformation = allBESInformation;

		if (_allBESInformation == null)
			throw new IllegalArgumentException("Must have bes information!");

		_consParms = consParms;
		_resO = _consParms.getResourceOverrides();
		if (_resO == null)
			throw new IllegalArgumentException("Resource overrides cannot be null!");
	}

	FactoryResourceAttributesDocumentType factoryResourceAttributes(boolean isAcceptingNewActivities,
		long totalNumberOfActivities)
	{
		URI[] namingProfiles = null;
		URI[] besExtensions = null;
		URI localResourceManagerType = ResourceManagerType.GridQueue.toApacheAxisURI();

		try {
			namingProfiles = new URI[] { new URI(BESConstants.NAMING_PROFILE_WS_ADDRESSING),
				new URI(BESConstants.NAMING_PROFILE_WS_NAMING) };
		} catch (MalformedURIException e) {
			namingProfiles = new URI[0];
		}

		besExtensions = new URI[0];

		String machineName = Hostname.getLocalHostname().toString();

		Collection<MessageElement> any = new LinkedList<MessageElement>();
		any.addAll(supportedFilesystems());

		Long wallclockTimeLimit = wallclockTimeLimit();
		if (wallclockTimeLimit != null)
			any.add(new MessageElement(BESConstants.BES_WALLCLOCK_TIMELIMIT_ATTR, wallclockTimeLimit));
		for (MatchingParameter parameter : allMatchingParameters())
			any.add(new MessageElement(GenesisIIBaseRP.MATCHING_PARAMETER_ATTR_QNAME, parameter.toAxisType()));

		BasicResourceAttributesDocumentType basicResourceAttributesDocument = new BasicResourceAttributesDocumentType(
			machineName, operatingSystem(), cpuArchitecture(), cpuCount(), cpuSpeed(), physicalMemory(), virtualMemory(),
			any.toArray(new MessageElement[any.size()]));

		return new FactoryResourceAttributesDocumentType(basicResourceAttributesDocument, isAcceptingNewActivities,
			machineName, machineName, totalNumberOfActivities, null, _allBESInformation.size(), null, namingProfiles,
			besExtensions, localResourceManagerType, any.toArray(new MessageElement[any.size()]));
	}
}