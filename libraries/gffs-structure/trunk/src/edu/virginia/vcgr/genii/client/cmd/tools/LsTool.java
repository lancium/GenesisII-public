package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPath.RNSPathApplyFunction;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class LsTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dls";
	static final private String _USAGE = "config/tooldocs/usage/uls";
	static final private String _MANPAGE = "config/tooldocs/man/ls";

	static private Log _logger = LogFactory.getLog(LsTool.class);

	private boolean _long = false;
	private boolean _all = false;
	private boolean _directory = false;
	private boolean _recursive = false;
	private boolean _epr = false;
	private boolean _certChain = false;
	private boolean _multiline = false;

	public LsTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
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

	@Option({ "recursive", "R", "r" })
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
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		boolean isLong = _long;
		boolean isAll = _all;
		boolean isDirectory = _directory;
		boolean isRecursive = _recursive;
		boolean isEPR = _epr;
		boolean isMultiline = _multiline;
		boolean isCertChain = _certChain;

		List<String> arguments = getArguments();
		if (arguments.size() == 0)
			arguments.add(".");
		ArrayList<RNSPath> targets = new ArrayList<RNSPath>();
		ArrayList<String> locals = new ArrayList<String>();

		int returnValue = 0;

		for (String arg : arguments) {
			Collection<GeniiPath.PathMixIn> paths = GeniiPath.pathExpander(arg);
			if (paths == null) {
				String msg = "Path does not exist or is not accessible: " + arg;
				stdout.println(msg);
				_logger.warn(msg);
				returnValue++;
				continue;
			}
			for (GeniiPath.PathMixIn path : paths) {
				if (path._rns != null) {
					targets.add(path._rns);
				} else {
					locals.add(path._file.toString());
				}
			}
		}

		// First, output the files specified on the command line.
		// Second, output the immediate contents of the directories specified on the command line.
		// If given the -d option, then output directory names as file names.
		ArrayList<RNSPath> dirs = new ArrayList<RNSPath>();
		for (RNSPath path : targets) {
			TypeInformation type = new TypeInformation(path.getEndpoint());
			if (isDirectory || !type.isRNS()) {
				printEntry(stdout, path, isLong, isAll, isEPR, isMultiline, isCertChain);
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
			File dir = new File(path);
			if (!isDirectory && !dir.isFile()) {
				String name = path;
				while ((name.lastIndexOf("/") == name.length() - 1) && (name.length() > 1))
					name = name.substring(0, name.length() - 1);
				if (name.length() > 1)
					if (name.lastIndexOf("/") > 0)
						name = path.substring(path.lastIndexOf("/") + 1);
					else
						name = path;
				stdout.println(name + ":");
			}
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
		return returnValue == 0 ? 0 : 1;
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

	static private void printEntry(PrintWriter out, RNSPath path, boolean isLong, boolean isAll, boolean isEPR,
		boolean isMultiline, boolean isCertChain) throws RNSException, ResourceException, AuthZSecurityException
	{
		String name = path.getName();
		if (name.startsWith(".") && !isAll)
			return;
		if (isLong) {
			TypeInformation type = new TypeInformation(path.getEndpoint());
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
			} catch (AuthZSecurityException gse) {
				out.println("Unable to acquire cert chain:  " + gse);
			}
		}
	}

	static public class DirLister implements RNSPathApplyFunction
	{
		PrintWriter _out;
		ArrayList<String> _subdirs;
		boolean _isLong;
		boolean _isAll;
		boolean _isEPR;
		boolean _isMultiline;
		boolean _isCertChain;

		DirLister(PrintWriter out, ArrayList<String> subdirs, boolean isLong, boolean isAll, boolean isEPR,
			boolean isMultiline, boolean isCertChain)
		{
			_out = out;
			_subdirs = subdirs;
			_isLong = isLong;
			_isAll = isAll;
			_isEPR = isEPR;
			_isMultiline = isMultiline;
			_isCertChain = isCertChain;
		}

		@Override
		public boolean applyToPath(RNSPath applyTo) throws RNSException, AuthZSecurityException
		{
			try {
				printEntry(_out, applyTo, _isLong, _isAll, _isEPR, _isMultiline, _isCertChain);
			} catch (ResourceException e) {
				throw new RNSException("failed to print entry due to resource exception", e);
			}
			if (applyTo.isRNS())
				_subdirs.add(applyTo.getName());
			return true;
		}

		@Override
		public boolean canWorkWithShortForm()
		{
			return !(_isLong || _isEPR || _isMultiline || _isCertChain);
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

		ArrayList<String> subdirs = new ArrayList<String>();
		DirLister dl = new DirLister(out, subdirs, isLong, isAll, isEPR, isMultiline, isCertChain);
		path.applyToContents(dl);

		out.println();
		if (isRecursive) {
			for (String entry : subdirs) {
				RNSPath sub = new RNSPath(path, entry, null, false);
				listDirectory(out, name, sub, isLong, isAll, isEPR, isMultiline, isCertChain, isRecursive);
			}
		}
	}
}
