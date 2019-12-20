package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;
import edu.virginia.vcgr.jsdl.JSDLConstants;

public class DefaultResourcesFacet extends DefaultPersonalityFacet implements ResourcesFacet
{
	static private Log _logger = LogFactory.getLog(DefaultResourcesFacet.class);
	
	@Override
	public void consumeExclusiveExecution(Object currentUnderstanding, Boolean exclusiveExecution) throws JSDLException
	{
		_logger.info("---EXExecution: ----" + exclusiveExecution);
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "ExclusiveExecution"));
	}

	@Override
	public void consumeIndividualCPUCount(Object currentUnderstanding, RangeExpression individualCPUCount) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualCPUCount"));
	}

	@Override
	public void consumeGPUCountPerNode(Object currentUnderstanding, RangeExpression GPUCountperNode) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "GPUCountPerNode"));
	}

	@Override
	public void consumeGPUMemoryPerNode(Object currentUnderstanding, RangeExpression gpuMemoryperNode) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "GPUMemoryPerNode"));
	}

	@Override
	public void consumeIndividualCPUSpeed(Object currentUnderstanding, RangeExpression individualCPUSpeed) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualCPUSpeed"));
	}

	@Override
	public void consumeIndividualCPUTime(Object currentUnderstanding, RangeExpression individualCPUTime) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualCPUTime"));
	}

	@Override
	public void consumeIndividualDiskSpace(Object currentUnderstanding, RangeExpression individualDiskSpace) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualDiskSpace"));
	}

	@Override
	public void consumeIndividualNetworkBandwidth(Object currentUnderstanding, RangeExpression individualNetworkBandwidth)
		throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualNetworkBandwidth"));
	}

	@Override
	public void consumeIndividualPhysicalMemory(Object currentUnderstanding, RangeExpression individualPhysicalMemory) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualPhsyicalMemory"));
	}

	@Override
	public void consumeIndividualVirtualMemory(Object currentUnderstanding, RangeExpression individualVirtualMemory) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "IndividualVirtualMemory"));
	}

	@Override
	public void consumeTotalCPUCount(Object currentUnderstanding, RangeExpression totalCPUCount) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalCPUCount"));
	}

	@Override
	public void consumeTotalCPUTime(Object currentUnderstanding, RangeExpression totalCPUTime) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalCPUTime"));
	}

	@Override
	public void consumeTotalDiskSpace(Object currentUnderstanding, RangeExpression totalDiskSpace) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalDiskSpace"));
	}

	@Override
	public void consumeTotalPhysicalMemory(Object currentUnderstanding, RangeExpression totalPhysicalMemory) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalPhysicalMemory"));
	}

	@Override
	public void consumeTotalResourceCount(Object currentUnderstanding, RangeExpression totalResourceCount) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalResourceCount"));
	}

	@Override
	public void consumeTotalVirtualMemory(Object currentUnderstanding, RangeExpression totalVirtualMemory) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "TotalVirtualMemory"));
	}

	@Override
	public void consumeWallclockTimeLimit(Object currentUnderstanding, RangeExpression wallclockTimeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName("http://vcgr.cs.virginia.edu/jsdl/genii", "WallclockTime"));
	}
}
