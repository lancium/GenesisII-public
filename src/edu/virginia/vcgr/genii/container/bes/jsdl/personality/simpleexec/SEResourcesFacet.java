package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;
import edu.virginia.vcgr.genii.container.bes.OGRSHConstants;
import edu.virginia.vcgr.genii.container.bes.OGRSHUtils;

public class SEResourcesFacet extends DefaultResourcesFacet
{
	static public final QName OGRSH_VERSION_QNAME = new QName(
		OGRSHConstants.OGRSH_NS, "OGRSHVersion");
	
	@Override
	public void consumeAny(Object currentUnderstanding, MessageElement any)
			throws JSDLException
	{
		QName name = any.getQName();
		if (name.equals(OGRSH_VERSION_QNAME))
		{
			String version = any.getValue();
			for (String acceptedVersion : OGRSHUtils.ogrshVersionsSupported())
			{
				if (version.equals(acceptedVersion))
				{
					((SimpleExecutionUnderstanding)
						currentUnderstanding).setRequiredOGRSHVersion(version);
					return;
				}
			}
			
			throw new JSDLMatchException(name);
		} else
			super.consumeAny(currentUnderstanding, any);
	}
}