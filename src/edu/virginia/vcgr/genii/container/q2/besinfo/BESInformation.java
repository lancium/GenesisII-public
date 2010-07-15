package edu.virginia.vcgr.genii.container.q2.besinfo;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.BasicResourceAttributesDocumentType;
import org.ggf.bes.factory.FactoryResourceAttributesDocumentType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.q2.matching.DefaultMatchingParameter;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParameters;

public class BESInformation
{
	static private Log _logger = LogFactory.getLog(BESInformation.class);
	
	private MatchingParameters _matchingParameters;
	
	private ProcessorArchitectureEnumeration _processorArchitecture;
	private OperatingSystemTypeEnumeration _operatingSystemType;
	private String _operatingSystemVerison;
	private Double _physicalMemory;
	
	private boolean _isAcceptingNewActivites;
	private long _numContainedActivities;
	
	public BESInformation(GetFactoryAttributesDocumentResponseType attrs)
	{
		_matchingParameters = new MatchingParameters();
		
		_numContainedActivities = -1L;
		_isAcceptingNewActivites = false;
		
		FactoryResourceAttributesDocumentType d1 =
			attrs.getFactoryResourceAttributesDocument();
		if (d1 != null)
		{
			_numContainedActivities = d1.getTotalNumberOfActivities();
			_isAcceptingNewActivites = d1.isIsAcceptingNewActivities();
			
			BasicResourceAttributesDocumentType d2 =
				d1.getBasicResourceAttributesDocument();
			if (d2 != null)
			{
				CPUArchitecture_Type d3 = d2.getCPUArchitecture();
				if (d3 != null)
					_processorArchitecture = d3.getCPUArchitectureName();
				
				OperatingSystem_Type d4 = d2.getOperatingSystem();
				if (d4 != null)
				{
					OperatingSystemType_Type d5 = d4.getOperatingSystemType();
					if (d5 != null)
						_operatingSystemType = d5.getOperatingSystemName();
					
					_operatingSystemVerison = d4.getOperatingSystemVersion();
				}	
				
				Double d6 = d2.getPhysicalMemory();
				if (d6 != null)
				{
					_physicalMemory = d6;
				}
			}
			
			MessageElement []any = d1.get_any();
			if (any != null)
			{
				for (MessageElement a : any)
				{
					QName elementName = a.getQName();
					if (elementName.equals(
						GenesisIIBaseRP.MATCHING_PARAMTER_ATTR_QNAME))
					{
						try
						{
							MatchingParameter mp = ObjectDeserializer.toObject(
								a, MatchingParameter.class);
						
							//Need to create new matching parameter
							edu.virginia.vcgr.genii.container.q2.matching.MatchingParameter tParam;
							tParam = new DefaultMatchingParameter(mp.getName(), mp.getValue(), false);
							_matchingParameters.add(tParam);
									
						}
						catch (Throwable cause)
						{
							_logger.warn(
								"Unable to parse matching parameters " +
								"from BES information.");
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		
		pw.format("%s Version %s on %s\n", 
			_operatingSystemType, _operatingSystemVerison,
			_processorArchitecture);
		pw.format("%s bytes of physical memory available\n",
				_physicalMemory);
		pw.format(
			"%d activites contained; accepting new ones?  %s\n",
			_numContainedActivities, _isAcceptingNewActivites);
		
		pw.println(_matchingParameters);
			
		pw.close();
		StreamUtils.close(writer);
		return writer.toString();
	}
	
	final public MatchingParameters getMatchingParameters()
	{
		return _matchingParameters;
	}
	
	final public ProcessorArchitectureEnumeration getProcessorArchitecture()
	{
		return _processorArchitecture;
	}
	
	final public OperatingSystemTypeEnumeration getOperatingSystemType()
	{
		return _operatingSystemType;
	}
	
	final public String getOperatingSystemVersion()
	{
		return _operatingSystemVerison;
	}
	
	final public Double getPhysicalMemory()
	{
		return _physicalMemory;
	}
	
	final public boolean isAcceptingNewActivities()
	{
		return _isAcceptingNewActivites;
	}
	
	final public long getNumberOfContainedResources()
	{
		return _numContainedActivities;
	}
}