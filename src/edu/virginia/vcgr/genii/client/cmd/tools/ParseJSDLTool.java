package edu.virginia.vcgr.genii.client.cmd.tools;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.ggf.jsdl.JobDefinition_Type;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;
import edu.virginia.vcgr.genii.container.jsdl.parser.ExecutionProvider;
import edu.virginia.vcgr.genii.context.ContextType;


public class ParseJSDLTool extends BaseGridTool{

	static private final String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dparseJSDL";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uparseJSDL";
	static private final String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/parseJSDL";

	public ParseJSDLTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE),
				false,ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		
		
		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}
		
		OutputStream out = null;
		InputStream in = null;
		
		GeniiPath source = new GeniiPath(getArgument(0));
		GeniiPath dest = new GeniiPath(getArgument(1));
		if (!source.exists())
			throw new FileNotFoundException(String.format(
				"Unable to find source file %s!", source));
		if (!source.isFile())
			throw new IOException(String.format(
				"Source path %s is not a file!", source));
		
		in = source.openInputStream();
		out = dest.openOutputStream();
		
		
		//Parse jsdl
		JobDefinition_Type jsdl =
			(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(in), JobDefinition_Type.class);
		PersonalityProvider provider = new ExecutionProvider();
		JobRequest tJob = 
			(JobRequest)JSDLInterpreter.interpretJSDL(provider, jsdl);
		
		ObjectOutputStream oOut = new ObjectOutputStream(out);
		oOut.writeObject(tJob);

		oOut.close();
		out.close();
		in.close();
		
		
		stdout.println("Job written to file");
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	
		
	
}
