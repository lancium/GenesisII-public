package edu.virginia.vcgr.genii.ui.plugins.jsdltool;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.gjt.JobDefinitionListener;
import edu.virginia.vcgr.genii.gjt.JobTool;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.jsdl.JobDefinition;

public class JSDLToolPlugin extends AbstractCombinedUIMenusPlugin
{
	static private Logger _logger = Logger.getLogger(JSDLToolPlugin.class);

	static private void submitToBES(EndpointReferenceType targetEPR, JobDefinition jobDefinition) throws IOException
	{
		try {
			GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, targetEPR);
			bes.createActivity(new CreateActivityType(new ActivityDocumentType(JSDLUtils.convert(jobDefinition), null), null));
		} catch (JAXBException e) {
			throw new IOException("Unable to convert from JAXB Type to Axis type.", e);
		}
	}

	static private void submitToQueue(EndpointReferenceType targetEPR, JobDefinition jobDefinition) throws IOException
	{
		try {
			QueueManipulator manip = new QueueManipulator(targetEPR);
			manip.submit(JSDLUtils.convert(jobDefinition), 0);
		} catch (JAXBException e) {
			throw new IOException("Unable to convert from JAXB Type to Axis type.", e);
		}
	}

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		try {
			Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
			JobTool.launch(null, new JobDefinitionListenerImpl(context.uiContext().callingContext(), paths.iterator().next()),
				null);
		} catch (IOException ioe) {
			throw new UIPluginException("Unable to run Grid Job Tool.", ioe);
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		for (EndpointDescription desc : selectedDescriptions) {
			if (desc.typeInformation().isBES())
				return true;
			else if (desc.typeInformation().isQueue())
				return true;
		}

		return false;
	}

	static private class JobDefinitionListenerImpl implements JobDefinitionListener
	{
		private ICallingContext _callingContext;
		private RNSPath _target;

		private JobDefinitionListenerImpl(ICallingContext callingContext, RNSPath target)
		{
			_callingContext = callingContext;
			_target = target;
		}

		@Override
		public void jobDefinitionGenerated(JobDefinition jobDefinition)
		{
			Closeable assumedContextToken = null;

			try {
				assumedContextToken = ContextManager.temporarilyAssumeContext(_callingContext);

				EndpointReferenceType target = _target.getEndpoint();
				TypeInformation typeInfo = new TypeInformation(target);
				if (typeInfo.isQueue())
					submitToQueue(target, jobDefinition);
				else
					submitToBES(target, jobDefinition);
			} catch (Throwable e) {
				_logger.error("Unable to submit JSDL.", e);
			} finally {
				StreamUtils.close(assumedContextToken);
			}
		}
	}
}