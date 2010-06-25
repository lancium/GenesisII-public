package edu.virginia.vcgr.genii.ui.plugins.cmdfunc;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.JavaCommandFunction;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class CommandFunctionPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
		throws UIPluginException
	{
		Closeable contextToken = null;
		OutputStream out = null;
			
		try
		{
			
			contextToken = ContextManager.temporarilyAssumeContext(
					context.uiContext().callingContext());
				
			Collection<RNSPath> paths = 
				context.endpointRetriever().getTargetEndpoints();
			RNSPath path = paths.iterator().next();
				
			TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
			
			JavaCommandFunction function = 
				CommandFunctionChoiceDialog.chooseCommandFunction(
					context.ownerComponent(), typeInfo.commandFunctions());
			if (function == null)
				return;
			
			String []parameterValues = null;
			
			if (function.parameters().length == 0)
			{
				int answer = JOptionPane.showConfirmDialog(
					context.ownerComponent(),
					String.format("Execute command function \"%s\"?",
						function),
					"Execute Command Function Confirmation", 
					JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION)
					parameterValues = new String[0];
			} else
			{
				parameterValues = CommandParameterDialog.fillInParameters(
					context.ownerComponent(), function);
			}
			
			if (parameterValues != null)
			{
				StringBuilder builder = new StringBuilder(function.name());
				for (String value : parameterValues)
				{
					builder.append(' ');
					builder.append(value);
				}
				
				out = ByteIOStreamFactory.createOutputStream(path);
				PrintStream ps = new PrintStream(out);
				ps.println(builder);
				ps.flush();
			}
		}
		catch (Throwable cause)
		{
			ErrorHandler.handleError(context.uiContext(),
				context.ownerComponent(), cause);
		}
		finally
		{
			StreamUtils.close(out);
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
		
		return (typeInfo.commandFunctions().size() > 0);
	}
}