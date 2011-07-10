package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.activation.MimetypesFileTypeMap;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.externalapp.ApplicationDatabase;
import edu.virginia.vcgr.externalapp.EditableFile;
import edu.virginia.vcgr.externalapp.ExternalApplication;
import edu.virginia.vcgr.externalapp.ExternalApplicationToken;
import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class EditTool extends BaseGridTool
{
	static final private String DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dedit";
	static final private String USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uedit";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/edit";
	
	@Override
	final protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Edit requires a file argument.");
	}

	@Override
	final protected int runCommand() throws Throwable
	{
		String arg = getArgument(0);
		
		if (arg.startsWith("!"))
		{
			String []cLine;
			
			ArrayList<String[]> history = CommandLineRunner.history();
			if (history == null || history.size() == 0)
				throw new ToolException("No command line history to edit!");
			
			arg = arg.substring(1);
			
			if (arg.equals("!"))
			{
				if (history.size() == 1)
					throw new ToolException("No last command to edit!");
				
				cLine = history.get(history.size() - 2);
			} else
			{
				int index = Integer.parseInt(arg);
				cLine = history.get(index);
			}
			
			return editCommand(cLine);
		} else
		{
			GeniiPath path = new GeniiPath(getArgument(0));
			return editFile(path);
		}
	}
	
	public EditTool()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false,
				ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
		
	}
	
	final public int editFile(GeniiPath path) throws Throwable
	{
		String mimeType =
			MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
				new File(path.path()));
		
		ExternalApplication app = 
			ApplicationDatabase.database().getExternalApplication(mimeType);
		if (app == null)
		{
			stderr.format(
				"Unable to find registered application for file [%s] %s.\n",
				mimeType, path);
			return 1;
		}
		
		EditableFile file = null;
		try
		{
			file = EditableFile.createEditableFile(path);
			ExternalApplicationToken token = app.launch(file.file());
			token.getResult();
		}
		finally
		{
			file.close();
		}
		
		return 0;
	}
	
	final public int editCommand(String []cLine) throws Throwable
	{
		String mimeType =
			MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
				"gridedit.txt");
		ExternalApplication app =
			ApplicationDatabase.database().getExternalApplication(mimeType);
		if (app == null)
		{
			stderr.println(
				"Unable to find registered application for for text files.");
			return 1;
		}
		
		boolean first = true;
		File tmpFile = File.createTempFile(
			"gridedit", ".txt");
		tmpFile.deleteOnExit();
		
		PrintWriter writer = null;
		BufferedReader reader = null;
		FileReader fReader = null;
		
		try
		{
			writer = new PrintWriter(tmpFile);
			for (String item : cLine)
			{
				if (!first)
					writer.print(' ');
				first = false;
				
				if (item.matches("^.*\\s.*$"))
					writer.format("\"%s\"", item);
				else
					writer.print(item);
			}
			
			writer.close();
			writer = null;
			
			ExternalApplicationToken token;
			token = app.launch(tmpFile);
			File result = token.getResult();
			
			if (result != null)
			{
				reader = new BufferedReader(fReader = new FileReader(result));
				String line;
				StringBuilder builder = new StringBuilder();
				while ( (line = reader.readLine()) != null)
				{
					if (builder.length() > 0)
						builder.append(' ');
					builder.append(line);
				}
				
				stdout.format("Running:  %s\n", builder);
				String []newCLine = CommandLineFormer.formCommandLine(builder.toString());
				CommandLineRunner runner = new CommandLineRunner();
				return runner.runCommand(newCLine, stdout, stderr, stdin);
			}
		}
		finally
		{
			StreamUtils.close(writer);
			StreamUtils.close(fReader);
			tmpFile.delete();
		}
		
		return 0;
	}
}