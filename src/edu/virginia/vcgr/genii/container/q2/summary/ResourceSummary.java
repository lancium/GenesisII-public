package edu.virginia.vcgr.genii.container.q2.summary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;

public class ResourceSummary
{
	static public OperatingSystemType translate(
		OperatingSystemTypeEnumeration osTypeEnum)
	{
		return OperatingSystemType.valueOf(osTypeEnum.getValue());
	}
	
	static public ProcessorArchitecture translate(
		ProcessorArchitectureEnumeration archEnum)
	{
		return ProcessorArchitecture.valueOf(archEnum.getValue());
	}
	
	private Map<HostDescription, SlotSummary> _summary =
		new HashMap<HostDescription, SlotSummary>();
	
	final public Set<HostDescription> hostDescriptions()
	{
		return _summary.keySet();
	}
	
	final public SlotSummary get(HostDescription description)
	{
		return _summary.get(description);
	}
	
	final public void add(HostDescription description,
		SlotSummary newSummary)
	{
		SlotSummary summary = _summary.get(description);
		if (summary == null)
			_summary.put(description, summary = new SlotSummary());
		
		summary.add(newSummary);
	}
}