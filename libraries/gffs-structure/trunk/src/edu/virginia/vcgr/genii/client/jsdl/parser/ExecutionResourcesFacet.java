package edu.virginia.vcgr.genii.client.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;

public class ExecutionResourcesFacet extends DefaultResourcesFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding) throws JSDLException
	{
		JobRequest request = (JobRequest) parentUnderstanding;
		return request.getRestrictions();
	}
	
	@Override
	public void consumeIndividualCPUCount(Object currentUnderstanding, RangeExpression individualCPUCount) 	
	{
		
	}
	
	@Override
	public void consumeTotalCPUCount(Object currentUnderstanding, RangeExpression individualCPUCount)
	{
		
	}
	
	@Override
	public void consumeWallclockTimeLimit(Object currentUnderstanding, RangeExpression wallclockTimeLimit){}
	
	@Override
	public void consumeExclusiveExecution(Object currentUnderstanding, boolean exclusiveExecution) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "ExclusiveExecution"));
	}

	@Override
	public void consumeIndividualCPUSpeed(Object currentUnderstanding, RangeExpression individualCPUSpeed) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualCPUSpeed"));
	}

	@Override
	public void consumeIndividualCPUTime(Object currentUnderstanding, RangeExpression individualCPUTime) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualCPUTime"));
	}

	@Override
	public void consumeIndividualDiskSpace(Object currentUnderstanding, RangeExpression individualDiskSpace) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualDiskSpace"));
	}

	@Override
	public void consumeIndividualNetworkBandwidth(Object currentUnderstanding, RangeExpression individualNetworkBandwidth)
		throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualNetworkBandwidth"));
	}

	@Override
	public void consumeIndividualPhysicalMemory(Object currentUnderstanding, RangeExpression individualPhysicalMemory) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualPhsyicalMemory"));
	}

	@Override
	public void consumeIndividualVirtualMemory(Object currentUnderstanding, RangeExpression individualVirtualMemory) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualVirtualMemory"));
	}

	@Override
	public void consumeTotalCPUTime(Object currentUnderstanding, RangeExpression totalCPUTime) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalCPUTime"));
	}

	@Override
	public void consumeTotalDiskSpace(Object currentUnderstanding, RangeExpression totalDiskSpace) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalDiskSpace"));
	}

	@Override
	public void consumeTotalPhysicalMemory(Object currentUnderstanding, RangeExpression totalPhysicalMemory) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalPhysicalMemory"));
	}

	@Override
	public void consumeTotalResourceCount(Object currentUnderstanding, RangeExpression totalResourceCount) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalResourceCount"));
	}

	@Override
	public void consumeTotalVirtualMemory(Object currentUnderstanding, RangeExpression totalVirtualMemory) throws JSDLException
	{
		// throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalVirtualMemory"));
	}

}
