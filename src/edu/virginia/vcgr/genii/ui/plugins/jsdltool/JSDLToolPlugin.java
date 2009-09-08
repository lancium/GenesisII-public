package edu.virginia.vcgr.genii.ui.plugins.jsdltool;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.jsdltool.exthooks.ExternalHooks;
import edu.virginia.vcgr.genii.jsdltool.exthooks.ExternalJSDLSink;
import edu.virginia.vcgr.genii.jsdltool.gui.JSDLTool;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class JSDLToolPlugin extends AbstractCombinedUIMenusPlugin
{
	static private final String JOB_NAME_TEMPLATE =
		"JSDL Tool Job %d";
	
	static private OutputStream openSinkToQueue(RNSPath targetPath, 
		EndpointReferenceType targetEPR) throws IOException
	{
		try
		{
			AddressingParameters ap = new AddressingParameters(
				targetEPR.getReferenceParameters());
			targetEPR.setReferenceParameters(
				ap.stripResourceForkInformation().toReferenceParameters());
		}
		catch (ResourceException re)
		{
			// Not a Genii endpoint, just leave it alone
		}
		
		EnhancedRNSPortType port = ClientUtils.createProxy(
			EnhancedRNSPortType.class, targetEPR);
		ListResponse response = port.list(new List("submission-point"));
		EntryType[] list = response.getEntryList();
		if (list.length != 1)
			throw new FileNotFoundException(String.format(
				"Unable to find submission point for %s.", targetPath.pwd()));
		
		return ByteIOStreamFactory.createOutputStream(
			list[0].getEntry_reference());
	}
	
	static private OutputStream openSinkToBES(RNSPath besPath, 
		EndpointReferenceType besEPR) throws IOException
	{
		try
		{
			RNSPath newJob = besPath.lookup(String.format(
				JOB_NAME_TEMPLATE, System.currentTimeMillis()));
			return ByteIOStreamFactory.createOutputStream(newJob);
		}
		catch (RNSPathDoesNotExistException rpdnee)
		{
			throw new FileNotFoundException(String.format(
				"Unable to find bes %s.", besPath.pwd()));
		}
		catch (RNSException rne)
		{
			throw new IOException(String.format(
				"Unable to open stream to BES %s.", besPath.pwd()));
		}
	}
	
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{
		ExternalHooks externalHooks = new ExternalHooks();
		
		Collection<RNSPath> paths = 
			context.endpointRetriever().getTargetEndpoints();
		externalHooks.setExternalJSDLSink(new ExternalJSDLSinkImpl(
			context.uiContext().callingContext(), paths.iterator().next()));
		
		JSDLTool.showJSDLTool(SwingUtilities.getWindowAncestor(
			context.ownerComponent()), externalHooks);
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		for (EndpointDescription desc : selectedDescriptions)
		{
			if (desc.typeInformation().isBES())
				return true;
			else if (desc.typeInformation().isQueue())
				return true;
		}
		
		return false;
	}
	
	static private class ExternalJSDLSinkImpl implements ExternalJSDLSink
	{
		private ICallingContext _callingContext;
		private RNSPath _target;
		
		private ExternalJSDLSinkImpl(ICallingContext callingContext, 
			RNSPath target)
		{
			_callingContext = callingContext;
			_target = target;
		}
		
		@Override
		public OutputStream openSink() throws IOException
		{
			Closeable assumedContextToken = null;
			
			try
			{
				assumedContextToken = ContextManager.temporarilyAssumeContext(
					_callingContext);
				
				EndpointReferenceType target = _target.getEndpoint();
				TypeInformation typeInfo = new TypeInformation(target);
				if (typeInfo.isQueue())
					return openSinkToQueue(_target, target);
				else
					return openSinkToBES(_target, target);
			}
			catch (RNSPathDoesNotExistException rpdnee)
			{
				throw new FileNotFoundException(String.format(
					"Couldn't find RNS path \"%s\".", _target.pwd()));
			}
			finally
			{
				StreamUtils.close(assumedContextToken);
			}
		}

		@Override
		public boolean supportsBatch()
		{
			try
			{
				return new TypeInformation(_target.getEndpoint()).isQueue();
			}
			catch (Throwable cause)
			{
				return false;
			}
		}
	}
}