package edu.virginia.vcgr.genii.container.deployer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType;
import edu.virginia.vcgr.genii.appdesc.SupportDocumentType;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionUtils;
import edu.virginia.vcgr.genii.client.deployer.AppDeployerConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;

public class ApplicationDeployerAttributeHandler extends AbstractAttributeHandler
{
	public ApplicationDeployerAttributeHandler(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	public Collection<MessageElement> getSupportDocumentAttributes() throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();

		PlatformDescriptionType[] platform = new PlatformDescriptionType[] { new PlatformDescriptionType(
			new CPUArchitecture_Type[] { JSDLUtils.getLocalCPUArchitecture() },
			new OperatingSystem_Type[] { JSDLUtils.getLocalOperatingSystem() }, null) };

		ret.add(new MessageElement(AppDeployerConstants.DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME, new SupportDocumentType(platform,
			null, ApplicationDescriptionUtils.DEPLOYMENT_TYPE_BINARY)));
		ret.add(new MessageElement(AppDeployerConstants.DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME, new SupportDocumentType(platform,
			null, ApplicationDescriptionUtils.DEPLOYMENT_TYPE_ZIPJAR)));

		return ret;
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(AppDeployerConstants.DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME, "getSupportDocumentAttributes");
	}
}