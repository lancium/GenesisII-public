package edu.virginia.vcgr.genii.container.q2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.Resources_Type;

import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiOrFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiPropertyFacet;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;

/**
 * This class matches job id's to bes id's.  It's used during the
 * scheduling phase.
 * 
 * @author mmm2a
 */
public class ResourceMatcher
{
	static private Log _logger = LogFactory.getLog(ResourceMatcher.class);
	
	static private boolean matchProperty(
		Map<String, Collection<String>> properties,
		MessageElement testProperty)
	{
		String propertyName = testProperty.getAttribute(
			GeniiPropertyFacet.PROPERTY_NAME_ATTRIBUTE);
		String propertyValue = testProperty.getAttribute(
			GeniiPropertyFacet.PROPERTY_VALUE_ATTRIBUTE);
		
		if (propertyName == null)
		{
			_logger.warn("Found a matching parameter property with no name.");
			return true;
		}
		
		if (propertyValue == null)
			_logger.trace("Found a matching parameter property with no " +
				"value...assuming it's a set test.");
		
		Collection<String> values = properties.get(propertyName);
		if (values == null || values.size() == 0)
			return false;
		
		if (propertyValue == null)
			return true;
		
		for (String value : values)
		{
			if (value.equals(propertyValue))
				return true;
		}
		
		return false;
	}
	
	static private boolean matchOr(
		Map<String, Collection<String>> properties,
		MessageElement orElement)
	{
		Iterator<?> iter = orElement.getChildElements();
		while (iter.hasNext())
		{
			MessageElement element = (MessageElement)iter.next();
			QName name = element.getQName();
			if (name.equals(GeniiPropertyFacet.PROPERTY_ELEMENT))
			{
				if (matchProperty(properties, element))
					return true;
			} else if (name.equals(GeniiOrFacet.OR_ELEMENT))
			{
				if (matchOr(properties, element))
					return true;
			}
		}
		
		return false;
	}
	
	static private boolean matchArch(BESInformation info, 
		CPUArchitecture_Type arch)
	{
		ProcessorArchitectureEnumeration pArch = arch.getCPUArchitectureName();
		if (pArch == null)
			return true;
		
		return info.getProcessorArchitecture().equals(pArch);
	}
	
	static private boolean matchOS(BESInformation info, 
		OperatingSystem_Type os)
	{
		OperatingSystemType_Type osType = os.getOperatingSystemType();
		if (osType != null)
		{
			OperatingSystemTypeEnumeration osTypeName = 
				osType.getOperatingSystemName();
			if (osTypeName != null && 
				!(osTypeName.equals(info.getOperatingSystemType())))
				return false;
		}
		
		String version = os.getOperatingSystemVersion();
		if (version != null && !version.equals(
			info.getOperatingSystemVersion()))
			return false;
		
		return true;
	}
	
	/**
	 * This operation indicates whether or not the given job can be
	 * run on the indicated bes container.
	 * 
	 * @param jobID The job to match against.
	 * @param besID The bes to match against.
	 * @return
	 */
	public boolean matches(JobDefinition_Type jsdl, BESInformation besInfo)
	{
		if (jsdl == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- no jsdl given.");
			return false;
		}
		
		if (besInfo == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- " +
				"no bes information available.");
			return false;
		}
		
		JobDescription_Type desc = jsdl.getJobDescription();
		if (desc == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- invalid jsdl given.");
			return false;
		}
		
		Resources_Type resources = desc.getResources();
		if (resources == null)
		{
			_logger.debug(
				"Matching jsdl to bes information -- no restrictions given.");
			return true;
		}
		
		CPUArchitecture_Type arch = resources.getCPUArchitecture();
		if (arch != null && !matchArch(besInfo, arch))
		{
			_logger.debug(
				"Cannot match jsdl to bes information -- Processor architectures don't match.");
			return false;
		}
		
		OperatingSystem_Type osType = resources.getOperatingSystem();
		if (osType != null && !matchOS(besInfo, osType))
		{
			_logger.debug(
				"Cannot match jsdl to bes information -- OS restrictinos don't match.");
			return false;
		}
		
		MessageElement []any = resources.get_any();
		if (any != null)
		{
			for (MessageElement element : any)
			{
				QName name = element.getQName();
				if (name.equals(GeniiPropertyFacet.PROPERTY_ELEMENT))
				{
					if (!matchProperty(
						besInfo.getMatchingParameters(), element))
						return false;
				} else if (name.equals(GeniiOrFacet.OR_ELEMENT))
				{
					if (!matchOr(
						besInfo.getMatchingParameters(), element))
						return false;
				}
			}
		}
		
		return true;
	}
}