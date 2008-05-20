package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class LsTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Lists elements in context space.";
	static final private String _USAGE =
		"ls [-ldae] [--long] [--all] [--directory] [--epr] [<target> *]\n" +
		"\tWHERE -e means to show the EPR.";
	
	private boolean _long = false;
	private boolean _all = false;
	private boolean _directory = false;
	private boolean _epr = false;
	
	public LsTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setLong()
	{
		_long = true;
	}
	
	public void setL()
	{
		setLong();
	}
	
	public void setAll()
	{
		_all = true;
	}
	
	public void setA()
	{
		setAll();
	}
	
	public void setDirectory()
	{
		_directory = true;
	}
	
	public void setD()
	{
		setDirectory();
	}
	
	public void setEpr()
	{
		_epr = true;
	}
	
	public void setE()
	{
		setEpr();
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		boolean isLong = _long;
		boolean isDirectory = _directory;
		boolean isAll = _all;
		boolean isEPR = _epr;
		List<String> arguments = getArguments();
		ICallingContext ctxt = ContextManager.getCurrentContext();
		if (arguments.size() == 0)
			arguments.add(".");
		ArrayList<RNSPath> targets = new ArrayList<RNSPath>();
		
		for (String arg : arguments)
		{
			for (RNSPath path : ctxt.getCurrentPath().expand(arg))
				targets.add(path);
			
			/* Old code
			RNSPath path = ctxt.getCurrentPath().lookup(arg, 
				RNSPathQueryFlags.MUST_EXIST);
			targets.add(path);
			*/
		}
		
		if (isDirectory)
		{
			for (RNSPath path : targets)
				printEntry(stdout, path, isLong, isAll, isEPR);
		} else
		{
			ArrayList<RNSPath> dirs = new ArrayList<RNSPath>();
				
			for (RNSPath path : targets)
			{
				TypeInformation type = new TypeInformation(
					path.getEndpoint());
				if (!type.isRNS())
					printEntry(stdout, type, path, isLong, isAll, isEPR);
				else
				{
					dirs.add(path);
				}
			}
			
			for (RNSPath path : dirs)
			{
				if (path.getName() == null)
					stdout.println("/:");
				else
					stdout.println(path.getName() + ":");
		

				Collection<RNSPath> entries = null;
				entries = path.listContents();
				
				if (entries.size() > 0)
				{
					for (RNSPath entry : entries)
					{
						printEntry(stdout, entry, isLong, isAll, isEPR);
					}
				}
				
				stdout.println();
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
	

	static private void printEntry(PrintStream out, RNSPath path,
		boolean isLong, boolean isAll, boolean isEPR)
		throws RNSException, ResourceException
	{
		printEntry(out, new TypeInformation(path.getEndpoint()),
			path, isLong, isAll, isEPR);
	}
	
	static private void printEntry(PrintStream out, TypeInformation type,
		RNSPath path, boolean isLong, boolean isAll, boolean isEPR)
		throws RNSException, ResourceException
	{
		String name = path.getName();
		if (name.startsWith(".") && !isAll)
			return;
		
		if (isLong)
		{
			String typeDesc = type.getTypeDescription();
			if (typeDesc != null)
			{
				if (!type.isByteIO())
					typeDesc = "[" + typeDesc + "]";
			} else
				typeDesc = "";
			
			out.format("%1$-16s", typeDesc);
		}
		out.println(path.getName());
		if (isEPR)
			out.println("\t" + ObjectSerializer.toString(
				path.getEndpoint(),
				new QName(GenesisIIConstants.GENESISII_NS, "endpoint"), false));
	}
}