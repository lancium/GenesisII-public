package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.client.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public abstract class AbstractReifier implements IJSDLReifier
{
	public JobDefinition_Type reifyJSDL(File deployDirectory, JobDefinition_Type jobDef) throws DeploymentException
	{
		JobDescription_Type description = jobDef.getJobDescription();
		Application_Type application = description.getApplication();
		if (application == null) {
			application = new Application_Type("Auto Generated", null, null, null);
			description.setApplication(application);
		}

		application = reifyApplication(deployDirectory, application);
		description.setApplication(application);

		// Mark Morgan we should really reify the resources as well

		return jobDef;
	}

	private Application_Type reifyApplication(File deployDirectory, Application_Type application) throws DeploymentException
	{
		try {
			Collection<MessageElement> elements = new ArrayList<MessageElement>();

			MessageElement[] any = application.get_any();
			if (any == null)
				any = new MessageElement[0];

			for (MessageElement element : any) {
				QName name = element.getQName();
				if (name.equals(JSDLPosixConstants.JSDL_POSIX_APPLICATION_QNAME))
					elements.add(reifyPOSIXApplication(deployDirectory, element));
				else if (name.equals(HPCConstants.HPC_APPLICATION_QNAME))
					elements.add(reifyHPCApplication(deployDirectory, element));
				else
					continue;
			}

			if (elements.size() == 0)
				elements.add(reifyPOSIXApplication(deployDirectory, new POSIXApplication_Type()));

			application.set_any(elements.toArray(new MessageElement[0]));
			return application;
		} catch (ResourceException re) {
			throw new DeploymentException("Unable to re-ify JSDL.", re);
		}
	}

	private MessageElement reifyPOSIXApplication(File deployDirectory, MessageElement element) throws ResourceException
	{
		POSIXApplication_Type posixApplication = ObjectDeserializer.toObject(element, POSIXApplication_Type.class);
		return reifyPOSIXApplication(deployDirectory, posixApplication);
	}

	private MessageElement reifyPOSIXApplication(File deployDirectory, POSIXApplication_Type posixApplication)
	{
		posixApplication = POSIXApplicationReifier.reifyApplication(deployDirectory, this, posixApplication);
		return new MessageElement(JSDLPosixConstants.JSDL_POSIX_APPLICATION_QNAME, posixApplication);
	}

	private MessageElement reifyHPCApplication(File deployDirectory, MessageElement element) throws ResourceException
	{
		HPCProfileApplication_Type hpcApplication = ObjectDeserializer.toObject(element, HPCProfileApplication_Type.class);
		hpcApplication = HPCApplicationReifier.reifyApplication(deployDirectory, this, hpcApplication);
		return new MessageElement(HPCConstants.HPC_APPLICATION_QNAME, hpcApplication);
	}

	public abstract String[] getAdditionalPaths(File deployDirectory);

	public abstract String[] getAdditionalLibraryPaths(File deployDirectory);

	public abstract String getBinaryName(File deployDirectory);

	public abstract String getCWD(File deployDirectory);
}