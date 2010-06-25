package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class SimpleSetterPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{
		String answer = JOptionPane.showInputDialog(context.ownerComponent(), 
			"What value would you like to set?", "Set Resource Fork Value",
			JOptionPane.QUESTION_MESSAGE);
		
		if (answer == null)
			return;
		
		Closeable contextToken = null;
		OutputStream out = null;
			
		try
		{
			contextToken = ContextManager.temporarilyAssumeContext(
				context.uiContext().callingContext());
			
			Collection<RNSPath> paths = 
				context.endpointRetriever().getTargetEndpoints();
			RNSPath path = paths.iterator().next();
			out = ByteIOStreamFactory.createOutputStream(path);
			PrintStream ps = new PrintStream(out);
			ps.println(answer);
			ps.flush();
			out.close();
			out = null;
		}
		catch (Throwable cause)
		{
			ErrorHandler.handleError(context.uiContext(),
				context.ownerComponent(), cause);
		}
		finally
		{
			StreamUtils.close(contextToken);
		}
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		TypeInformation typeInfo = 
			selectedDescriptions.iterator().next().typeInformation();
		
		if (typeInfo.isByteIO() && typeInfo.isResourceFork())
			return true;
		
		return false;
	}
}