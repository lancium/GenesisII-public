package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.fuse.FuseUtils;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;
import edu.virginia.vcgr.genii.container.bes.OGRSHConstants;

public class CommonResourcesFacet extends DefaultResourcesFacet
{
	static public final QName OGRSH_VERSION_QNAME = new QName(OGRSHConstants.OGRSH_NS, "OGRSHVersion");
	static public final QName FUSE_DIRECTORY_QNAME = new QName("http://vcgr.cs.virginia.edu/gfuse", "FuseDirectory");

	@Override
	public void consumeAny(Object currentUnderstanding, MessageElement any) throws JSDLException
	{
		QName name = any.getQName();
		if (name.equals(OGRSH_VERSION_QNAME)) {
			String version = any.getValue();
			for (String acceptedVersion : Installation.getOGRSH().getInstalledVersions().keySet()) {
				if (version.equals(acceptedVersion)) {
					((CommonExecutionUnderstanding) currentUnderstanding).setRequiredOGRSHVersion(version);
					return;
				}
			}

			throw new JSDLMatchException(name);
		} else if (name.equals(FUSE_DIRECTORY_QNAME)) {
			String fuseDirectory = any.getValue();
			String msg = FuseUtils.supportsFuse();
			if (msg != null)
				throw new JSDLException("Fuse is not supported on this system:  " + msg);
			((CommonExecutionUnderstanding) currentUnderstanding).setFuseMountDirectory(fuseDirectory);
		} else
			super.consumeAny(currentUnderstanding, any);
	}

	@Override
	public void consumeTotalPhysicalMemory(Object currentUnderstanding, RangeExpression totalPhysicalMemory) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setTotalPhysicalMemory(totalPhysicalMemory.describe().getUpperBound());
	}

	@Override
	public void consumeWallclockTimeLimit(Object currentUnderstanding, RangeExpression wallclockTimeLimit) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setWallclockTimeLimit(wallclockTimeLimit.describe().getUpperBound());
	}

	@Override
	public void consumeTotalCPUCount(Object currentUnderstanding, RangeExpression totalCPUCount) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setTotalCPUCount(totalCPUCount.describe().getUpperBound());
	}

	@Override
	public void consumeIndividualCPUCount(Object currentUnderstanding, RangeExpression individualCPUCount) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setIndividualCPUCount(individualCPUCount.describe().getUpperBound());
	}

	@Override
	public void consumeGPUCountPerNode(Object currentUnderstanding, RangeExpression GPUCountPerNode) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setGPUCountPerNode(GPUCountPerNode.describe().getUpperBound());
	}

	@Override
	public void consumeGPUMemoryPerNode(Object currentUnderstanding, RangeExpression GPUMemoryPerNode) throws JSDLException
	{
		((CommonExecutionUnderstanding) currentUnderstanding).setGPUMemoryPerNode(GPUMemoryPerNode.describe().getUpperBound());
	}
}
