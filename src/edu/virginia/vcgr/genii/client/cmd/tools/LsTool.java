package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.File;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class LsTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dls";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uls";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/ls";

	private boolean _long = false;
	private boolean _all = false;
	private boolean _directory = false;
	private boolean _recursive = false;
	private boolean _epr = false;
	private boolean _certChain = false;
	private boolean _multiline = false;

	public LsTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({ "long", "l" })
	public void setLong()
	{
		_long = true;
	}

	@Option({ "all", "a" })
	public void setAll()
	{
		_all = true;
	}

	@Option({ "directory", "d" })
	public void setDirectory()
	{
		_directory = true;
	}

	@Option({ "recursive", "R" })
	public void setRecursive()
	{
		_recursive = true;
	}

	@Option({ "epr", "e" })
	public void setEpr()
	{
		_epr = true;
	}

	@Option({ "multiline", "m" })
	public void setMultiline()
	{
		_multiline = true;
	}

	@Option({ "cert-chain" })
	public void setCert_chain()
	{
		_certChain = true;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		boolean isLong = _long;
		boolean isAll = _all;
		boolean isDirectory = _directory;
		boolean isRecursive = _recursive;
		boolean isEPR = _epr;
		boolean isMultiline = _multiline;
		boolean isCertChain = _certChain;

		List<String> arguments = getArguments();
		ICallingContext ctxt = ContextManager.getExistingContext();
		if (arguments.size() == 0)
			arguments.add(".");
		ArrayList<RNSPath> targets = new ArrayList<RNSPath>();
		ArrayList<String> locals = new ArrayList<String>();

		for (String arg : arguments) {
			GeniiPath gPath = new GeniiPath(arg);
			if (!gPath.exists())
				throw new RNSPathDoesNotExistException(gPath.path());
			if (gPath.pathType() == GeniiPathType.Grid) {
				for (RNSPath path : ctxt.getCurrentPath().expand(gPath.path()))
					targets.add(path);
			} else
				locals.add(gPath.path());
		}

		// First, output the files specified on the command line.
		// Second, output the immediate contents of the directories specified on the command line.
		// If given the -d option, then output directory names as file names.
		ArrayList<RNSPath> dirs = new ArrayList<RNSPath>();
		for (RNSPath path : targets) {
			TypeInformation type = new TypeInformation(path.getEndpoint());
			if (isDirectory || !type.isRNS()) {
				printEntry(stdout, type, path, isLong, isAll, isEPR, isMultiline, isCertChain);
			} else
				dirs.add(path);
		}
		for (RNSPath path : dirs) {
			listDirectory(stdout, null, path, isLong, isAll, isEPR, isMultiline, isCertChain, isRecursive);
		}

		// Third, output the local files specified on the command line.
		if (locals.size() > 0 && targets.size() > 0)
			stdout.println("local:");
		for (String path : locals) {
			if (!isDirectory) {
				String name = path;
				while ((name.lastIndexOf("/") == name.length() - 1) && (name.length() > 1))
					name = name.substring(0, name.length() - 1);
				if (name.length() > 1)
					if (name.lastIndexOf("/") > 0)
						name = path.substring(path.lastIndexOf("/"));
					else
						name = path;
				stdout.println(name + ":");
			}
			File dir = new File(path);
			if (isDirectory || dir.isFile()) {
				printLocalEntry(stdout, dir, isLong, isAll);
			} else {
				File[] files = dir.listFiles();
				for (File cur : files) {
					printLocalEntry(stdout, cur, isLong, isAll);
				}
			}
			stdout.println();
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}

	static private void printLocalEntry(PrintWriter out, File path, boolean isLong, boolean isAll)
	{
		String name = path.getName();
		if (name.startsWith(".") && !isAll && (name.length() > 1))
			return;
		if (isLong) {
			String typeDesc = "";
			if (path.isDirectory())
				typeDesc = "[directory]";
			else
				typeDesc = new Long(path.length()).toString();
			out.format("%1$-16s%2$s", typeDesc, name);
			out.println();
		} else
			out.println(name);
	}

	static private void printEntry(PrintWriter out, TypeInformation type, RNSPath path, boolean isLong, boolean isAll,
		boolean isEPR, boolean isMultiline, boolean isCertChain) throws RNSException, ResourceException
	{
		String name = path.getName();
		if (name.startsWith(".") && !isAll)
			return;
		if (isLong) {
			String typeDesc = type.getTypeDescription();
			if (typeDesc != null) {
				if (!type.isByteIO())
					typeDesc = "[" + typeDesc + "]";
			} else
				typeDesc = "";

			out.format("%1$-16s", typeDesc);
		}
		out.println(name);
		if (isEPR) {
			out.println("\t"
				+ ObjectSerializer.toString(path.getEndpoint(), new QName(GenesisIIConstants.GENESISII_NS, "endpoint"), false));
		}
		if (isMultiline) {
			EndpointReferenceType epr = path.getEndpoint();
			out.println("address: " + epr.getAddress());
			AddressingParameters aps = new AddressingParameters(epr.getReferenceParameters());
			out.println("resource-key: " + aps.getResourceKey());
			WSName wsname = new WSName(epr);
			out.println("endpointIdentifier: " + wsname.getEndpointIdentifier());
			List<ResolverDescription> resolvers = wsname.getResolvers();
			if (resolvers != null) {
				for (ResolverDescription resolver : resolvers) {
					out.println("resolver: " + resolver.getEPR().getAddress());
				}
			}
			out.println();
		}
		if (isCertChain) {
			try {
				X509Certificate[] certs = EPRUtils.extractCertChain(path.getEndpoint());
				if (certs == null || certs.length == 0)
					out.println("No asscoiated certificates!");
				else
					for (X509Certificate cert : certs) {
						out.format("Certificate:  %s\n", cert);
					}
			} catch (GeneralSecurityException gse) {
				out.println("Unable to acquire cert chain:  " + gse);
			}
		}
	}

	static private void listDirectory(PrintWriter out, String prefix, RNSPath path, boolean isLong, boolean isAll,
		boolean isEPR, boolean isMultiline, boolean isCertChain, boolean isRecursive) throws RNSException, ResourceException
	{
		String name = path.getName();
		if (name == null)
			name = "/";
		if (prefix != null)
			name = prefix + "/" + name;
		out.println(name + ":");

		Collection<RNSPath> entries = path.listContents();
		ArrayList<RNSPath> subdirs = new ArrayList<RNSPath>();
		for (RNSPath entry : entries) {
			TypeInformation type = new TypeInformation(entry.getEndpoint());
			printEntry(out, type, entry, isLong, isAll, isEPR, isMultiline, isCertChain);
			if (type.isRNS())
				subdirs.add(entry);
		}
		out.println();
		if (isRecursive) {
			for (RNSPath entry : subdirs) {
				listDirectory(out, name, entry, isLong, isAll, isEPR, isMultiline, isCertChain, isRecursive);
			}
		}
	}
}
