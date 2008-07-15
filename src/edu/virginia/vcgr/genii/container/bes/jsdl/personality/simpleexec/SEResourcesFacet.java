package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.fuse.FuseUtils;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;
import edu.virginia.vcgr.genii.container.bes.OGRSHConstants;

public class SEResourcesFacet extends DefaultResourcesFacet
{
	static public final QName OGRSH_VERSION_QNAME = new QName(
		OGRSHConstants.OGRSH_NS, "OGRSHVersion");
	static public final QName FUSE_DIRECTORY_QNAME = new QName(
		"http://vcgr.cs.virginia.edu/gfuse", "FuseDirectory");
	
	@Override
	public void consumeAny(Object currentUnderstanding, MessageElement any)
			throws JSDLException
	{
		QName name = any.getQName();
		if (name.equals(OGRSH_VERSION_QNAME))
		{
			String version = any.getValue();
			for (String acceptedVersion : 
				Installation.getOGRSH().getInstalledVersions().keySet())
			{
				if (version.equals(acceptedVersion))
				{
					((SimpleExecutionUnderstanding)
						currentUnderstanding).setRequiredOGRSHVersion(version);
					return;
				}
			}
			
			throw new JSDLMatchException(name);
		} else if (name.equals(FUSE_DIRECTORY_QNAME))
		{
			String fuseDirectory = any.getValue();
			String msg = FuseUtils.supportsFuse();
			if (msg != null)
				throw new JSDLException(
					"Fuse is not supported on this system:  " +
					msg);
			((SimpleExecutionUnderstanding)
				currentUnderstanding).setFuseDirectory(fuseDirectory);
		} else
			super.consumeAny(currentUnderstanding, any);
	}
}