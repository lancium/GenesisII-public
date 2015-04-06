package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class MetadataSearchTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dmst";
	static final private String _USAGE = "config/tooldocs/usage/umst";
	static final private String _MANPAGE = "config/tooldocs/man/mst";

	static private int _count = 0;

	public MetadataSearchTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	private boolean _long = false;
	private boolean _all = false;
	private boolean _directory = false;
	private boolean _recursive = true;
	private boolean _existence = false;
	private boolean _compare = false;
	private boolean _attributes = true;
	private boolean _display = true;
	private boolean _xquery = false;
	private long sttime, endtime;

	@Option({ "all", "a" })
	public void setAll()
	{
		_all = true;
	}

	@Option({ "existence", "e" })
	public void setAttributes()
	{
		_existence = true;
		_display = false;
	}

	@Option({ "compare", "c" })
	public void setCompare()
	{
		_compare = true;
		_display = false;
	}

	@Option({ "xquery", "x" })
	public void setXquery()
	{
		_xquery = true;
		_display = false;
	}

	@Override
	protected int runCommand() throws FileNotFoundException, IOException, RNSException
	{
		sttime = Calendar.getInstance().getTimeInMillis();
		boolean isLong = _long;
		boolean isAll = _all;
		boolean isDirectory = _directory;
		boolean isRecursive = _recursive;
		boolean isexistence = _existence;
		boolean isCompare = _compare;
		boolean isAttributes = _attributes;
		boolean isDisplay = _display;
		boolean isXquery = _xquery;
		String xpathq = "";
		_count = 0;

		List<String> arguments = getArguments();
		ArrayList<String> args = new ArrayList<String>();
		ICallingContext ctxt = ContextManager.getCurrentContext();
		if (arguments.size() > 0) {
			xpathq = getArgument(0);
		}
		args.add(".");
		ArrayList<RNSPath> targets = new ArrayList<RNSPath>();
		ArrayList<String> locals = new ArrayList<String>();

		for (String arg : args) {
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
				if (isXquery)
					;
				// printEntryforXquery(stdout, type, path, isLong, isAll, isexistence, isXquery,
				// isCompare, isAttributes, isDisplay, xpathq);
				else
					printEntry(stdout, type, path, isLong, isAll, isexistence, isXquery, isCompare, isAttributes, isDisplay, xpathq);
			} else
				dirs.add(path);
		}
		for (RNSPath path : dirs) {
			listDirectory(stdout, null, path, isLong, isAll, isexistence, isXquery, isCompare, isRecursive, isAttributes, isDisplay, xpathq);
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

		endtime = Calendar.getInstance().getTimeInMillis();
		stdout.println("\nNumber of file searched =" + _count + "\nExecution Time is = " + (endtime - sttime) + "ms");
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

	/*
	 * static private void printEntryforXquery(PrintWriter out, TypeInformation type, RNSPath path, boolean isLong, boolean isAll, boolean
	 * isexistence, boolean isXquery, boolean isCompare, boolean isAttributes, boolean isDisplay, String xpath1) throws RNSException,
	 * ResourceException { try{ XQDataSource xqs = new SednaXQDataSource(); xqs.setProperty("serverName", "localhost");
	 * xqs.setProperty("databaseName", "tempdb");
	 * 
	 * XQConnection conn = xqs.getConnection("SYSTEM", "MANAGER"); XQExpression xqe = conn.createExpression(); _count++; String xpathq = "",
	 * second = ""; RNSPath rpath = null; String name = path.getName(); //System.out.println(name); if (name.startsWith(".") && !isAll)
	 * return; GeniiPath gPath = new GeniiPath(path); try{ if ( gPath.pathType() != GeniiPathType.Grid) throw new
	 * InvalidToolUsageException("<target> must be a grid path. "); rpath = lookup(gPath, RNSPathQueryFlags.MUST_EXIST); GeniiCommon common =
	 * ClientUtils.createProxy(GeniiCommon.class, rpath.getEndpoint()); GetResourcePropertyDocumentResponse resp =
	 * common.getResourcePropertyDocument( new GetResourcePropertyDocument()); MessageElement document = new MessageElement( new
	 * QName(GenesisIIConstants.GENESISII_NS, "attributes")); for (MessageElement child : resp.get_any()) {
	 * 
	 * document.addChild(child);
	 * 
	 * } DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance(); xmlFact.setNamespaceAware(true); DocumentBuilder builder =
	 * xmlFact.newDocumentBuilder(); //org.w3c.dom.Document docs = builder.parse(ctxtTemplate); Document docs = document.getAsDocument(); try{
	 * TransformerFactory tFactory = TransformerFactory.newInstance(); Transformer transformer = tFactory.newTransformer();
	 * 
	 * DOMSource source = new DOMSource(document); StreamResult stresult = new StreamResult(new
	 * File("/home/anindya/"+gPath.getName()+"RP.xml")); transformer.transform(source, stresult); }catch(Exception ex){ ex.printStackTrace();
	 * }
	 * 
	 * }catch(Exception ex){ ex.printStackTrace(); } }catch(Exception ex){ ex.printStackTrace(); } }
	 */
	static private void printEntry(PrintWriter out, TypeInformation type, RNSPath path, boolean isLong, boolean isAll, boolean isexistence,
		boolean isMultiline, boolean isCompare, boolean isAttributes, boolean isDisplay, String xpath1) throws RNSException,
		ResourceException
	{
		_count++;
		String xpathq = "", second = "";
		RNSPath rpath = null;
		String name = path.getName();
		// System.out.println(name);
		if (name.startsWith(".") && !isAll)
			return;

		if (isAttributes) {
			GeniiPath gPath = new GeniiPath(path);
			try {
				if (gPath.pathType() != GeniiPathType.Grid)
					throw new InvalidToolUsageException("<target> must be a grid path. ");
				rpath = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, rpath.getEndpoint());
				GetResourcePropertyDocumentResponse resp = common.getResourcePropertyDocument(new GetResourcePropertyDocument());
				MessageElement document = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, "attributes"));
				for (MessageElement child : resp.get_any()) {

					document.addChild(child);

				}

				final PrefixResolver resolver = new PrefixResolverDefault(document.getAsDocument().getDocumentElement());
				NamespaceContext ctx = new NamespaceContext()
				{
					public String getNamespaceURI(String prefix)
					{
						return resolver.getNamespaceForPrefix(prefix);
					}

					public Iterator<?> getPrefixes(String val)
					{
						return null;
					}

					// Dummy implemenation - not used!
					public String getPrefix(String uri)
					{
						return null;
					}
				};

				Timestamp t2 = null;
				int _param, _result;
				DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
				xmlFact.setNamespaceAware(true);
				// DocumentBuilder builder = xmlFact.newDocumentBuilder();
				// org.w3c.dom.Document docs = builder.parse(ctxtTemplate);
				Document docs = document.getAsDocument();
				// if(isCompare){
				StringTokenizer stok = new StringTokenizer(xpath1, "<>=!");
				xpathq = stok.nextToken();
				if (stok.hasMoreTokens()) {
					second = stok.nextToken();
					// out.println("Token = " + second);
				}
				XPathFactory xpathFact = XPathFactory.newInstance();
				XPath xpath = xpathFact.newXPath();
				xpath.setNamespaceContext(ctx);
				// if (xpath1.equals("*") || xpath1.equals("")) {
				// xpathq = "ns1:attributes//*";
				// } else
				// xpathq = "ns1:attributes//*[local-name()=\'" + xpathq + "\']";
				if (xpath1.equals("*") || xpath1.equals("")) {
					xpathq = "ns1:resource-properties//*";
				} else
					xpathq = "ns1:resource-properties//*[local-name()=\'" + xpathq + "\']";
				XPathExpression expr = xpath.compile(xpathq);

				Object result = expr.evaluate(docs, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				// System.out.println(result.toString());
				if (isexistence) {
					if (xpath1.contains("!")) {
						if (nodes.getLength() == 0) {
							out.println(rpath.getParent() + "/" + rpath.getName());
						}
					} else {
						if (nodes.getLength() > 0) {
							out.println(rpath.getParent() + "/" + rpath.getName());
						}
					}
					return;
				}
				for (int i = 0; i < nodes.getLength(); i++) {
					try {
						t2 = Timestamp.valueOf(second);

						if (nodes.item(i).getTextContent().equals("")) {
							if (isDisplay)
								out.println(nodes.item(i).getLocalName() + " of (" + rpath.getParent() + "/" + rpath.getName() + ") = nil");
							continue;
						}
						if (isDisplay)
							out.println(nodes.item(i).getLocalName() + " of (" + rpath.getParent() + "/" + rpath.getName() + ") = "
								+ nodes.item(i).getTextContent());
						// out.println("result is =" + result);

						StringTokenizer str = new StringTokenizer(nodes.item(i).getTextContent(), "TZ");
						String s = "";
						while (str.hasMoreTokens()) {
							s = s + " " + str.nextToken();
						}
						Timestamp t = Timestamp.valueOf(s);
						long milis = t.getTime();
						long milis2 = t2.getTime();
						if (isCompare) {
							if (xpath1.contains("<>")) {
								if (milis != milis2)
									out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
								return;
							}
							if (xpath1.contains("=")) {
								if (milis == milis2)
									out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
								return;
							}
							if (xpath1.contains("<")) {
								if (milis < milis2)
									out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
								return;
							}
							if (xpath1.contains(">")) {
								if (milis < milis2)
									out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
							}
						}

					} catch (Exception e) {
						// Not a time
						try {
							_result = Integer.parseInt(second);
							_param = Integer.parseInt(nodes.item(i).getTextContent());
							if (isCompare) {
								if (xpath1.contains("<>")) {
									if (_param != _result)
										out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
									return;
								}
								if (xpath1.contains("=")) {
									if (_param == _result)
										out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
									return;
								}
								if (xpath1.contains("<")) {
									if (_param < _result)
										out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
									return;
								}
								if (xpath1.contains(">")) {
									if (_param < _result)
										out.println(rpath.getParent() + "/" + rpath.getName() + ": " + nodes.item(i).getTextContent());
								}
							}
						} catch (Exception ex) {
							// Not an integer
							if (isDisplay)
								if (!nodes.item(i).getTextContent().equals(""))
									out.println(nodes.item(i).getLocalName() + " of (" + rpath.getParent() + "/" + rpath.getName() + ") = "
										+ nodes.item(i).getTextContent());
								else
									out.println(nodes.item(i).getLocalName() + " of (" + rpath.getParent() + "/" + rpath.getName()
										+ ") = nil");
							if (isCompare) {
								if (xpath1.contains("=")) {
									if (nodes.item(i).getTextContent().equals(second)) {
										out.println(rpath.getParent() + "/" + rpath.getName());
									}
								} else if (xpath1.contains("<>")) {
									if (!nodes.item(i).getTextContent().equals(second)) {
										out.println(rpath.getParent() + "/" + rpath.getName());
									}
								}
							}
						}
					}

				}
			} catch (Exception e) {
				// e.printStackTrace();
				System.err.println("No Permission for " + rpath.getParent() + "/" + rpath.getName());
			}
		}

	}

	/*
	 * static private long getSecondVal(String str){ StringTokenizer stok = new StringTokenizer(str,":"); long val[] = {0,0,0,0,0}; int i = 0;
	 * while(stok.hasMoreTokens()){ val[i++] = Long.parseLong(stok.nextToken()); } return val[0]*24*60*60*1000 + val[1]*60*60*1000 +
	 * val[2]*60*1000 + val[3]*1000 + val[4]; }
	 */

	static private void listDirectory(PrintWriter out, String prefix, RNSPath path, boolean isLong, boolean isAll, boolean isexistence,
		boolean isMultiline, boolean isCompare, boolean isRecursive, boolean isAttributes, boolean isDisplay, String xpathq)
		throws RNSException, ResourceException
	{
		String name = path.getName();
		if (name == null)
			name = "/";
		if (prefix != null)
			name = prefix + "/" + name;
		out.println(name + ":");
		Collection<RNSPath> entries = null;
		try {
			entries = path.listContents();
		} catch (Exception e) {
			System.out.println("No permission for " + name);
			return;
		}
		ArrayList<RNSPath> subdirs = new ArrayList<RNSPath>();
		for (RNSPath entry : entries) {
			// out.println("Current Entry is = " + entry.getName());
			// out.println();
			TypeInformation type = new TypeInformation(entry.getEndpoint());
			printEntry(out, type, entry, isLong, isAll, isexistence, isMultiline, isCompare, isAttributes, isDisplay, xpathq);
			// if (entry.getName().equals("resources")) continue;
			// System.out.println("Parent is:" + entry.getParent().getName());
			if (entry.getParent().getName().equals("Services"))
				continue;
			if (type.isRNS())
				subdirs.add(entry);
		}
		// out.println();
		if (isRecursive) {
			for (RNSPath entry : subdirs) {
				listDirectory(out, name, entry, isLong, isAll, isexistence, isMultiline, isCompare, isRecursive, isAttributes, isDisplay,
					xpathq);
			}
		}
	}
}
