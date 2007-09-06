package edu.virginia.vcgr.genii.container.deployment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.AxisEngine;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.container.IContainerManaged;

public class ServiceDeployer extends Thread
{
	static private final Log _logger = LogFactory.getLog(ServiceDeployer.class);
	
	static private Pattern _WSDD_FILE_PATTERN = Pattern.compile("^.*\\.wsdd$");
	
	static private Pattern _DEPLOYMENT_FILE_PATTERN = 
		Pattern.compile("^.*\\.gdb$");
	
	static private PatternFilenameFilter _FILTER = new PatternFilenameFilter(
		new Pattern[] { _DEPLOYMENT_FILE_PATTERN, _WSDD_FILE_PATTERN });
	
	private AxisEngine _axisEngine;
	private File _watchDirectory;
	private HashMap<String, DeploymentInformation> _deploymentInformation;
	
	static private class PatternFilenameFilter implements FileFilter
	{
		private Pattern []_patterns;
		
		public PatternFilenameFilter(Pattern []patterns)
		{
			_patterns = patterns;
		}

		public boolean accept(File pathname)
		{
			for (Pattern p : _patterns)
			{
				if (p.matcher(pathname.getName()).matches())
					return true;
			}

			return false;
		}
	}
	
	static private class DeploymentInformation
	{
		static final int _MAX_ATTEMPTS = 10;
		int _attempts;
		ClassLoader _loader;
		
		DeploymentInformation()
		{
			_attempts = 0;
			_loader = null;
		}
	}
	
	static public void startServiceDeployer(AxisEngine axisEngine, 
		File watchDirectory)
	{
		ServiceDeployer sd = new ServiceDeployer(axisEngine, watchDirectory);
		sd.setDaemon(true);
		sd.start();
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000);
				attemptDeployment();
			}
			catch (Throwable t)
			{
				_logger.debug(t.getLocalizedMessage(), t);
			}
		}
	}
	
	private ServiceDeployer(AxisEngine axisEngine, File watchDir)
	{
		_axisEngine = axisEngine;
		_watchDirectory = watchDir;
		_deploymentInformation = new HashMap<String, DeploymentInformation>();
		
		attemptDeployment();
	}
	
	private void attemptDeployment()
	{
		// First, find the files that are new that we haven't tried to load yet.
		File []files = _watchDirectory.listFiles(_FILTER);
		if (files == null)
			return;
		
		for (File file : files)
		{
			if (!_deploymentInformation.containsKey(file.getName()))
			{
				_deploymentInformation.put(file.getName(), 
					new DeploymentInformation());
			}
		}
		
		// Now, go through the deployment information
		for (String filename : _deploymentInformation.keySet())
		{
			String className = null;
			DeploymentInformation info = _deploymentInformation.get(filename);
			if (info._loader != null)
				continue;

			File file = new File(_watchDirectory, filename);
			if (info._attempts == -1)
			{
				// We've already given up on this deployment item
				continue;
			}
			
			if (++info._attempts > DeploymentInformation._MAX_ATTEMPTS)
			{
				_logger.error("Unable to deploy file \"" + 
					file.getAbsolutePath() + "\".");
				info._attempts = -1;
				continue;
			}
			
			if (_DEPLOYMENT_FILE_PATTERN.matcher(file.getName()).matches())
			{
				JarFile jFile = null;
				try
				{
					URLClassLoader loader = new URLClassLoader(
						new URL[] { file.toURI().toURL() },
						Thread.currentThread().getContextClassLoader());
					
					jFile = new JarFile(file);
					Enumeration<JarEntry> entries = jFile.entries();
					info._loader = loader;
					while (entries.hasMoreElements())
					{
						JarEntry entry = entries.nextElement();
						if (entry.isDirectory())
							continue;
						if (_WSDD_FILE_PATTERN.matcher(entry.getName()).matches())
						{
							className = attemptDeploy(jFile, entry);
							if (className != null)
								ClassUtils.setClassLoader(className, loader);
						}
					}
				}
				catch (ParserConfigurationException pce)
				{
					_logger.warn(pce.getMessage(), pce);
				}
				catch (SAXException se)
				{
					_logger.warn(se.getMessage(), se);
				}
				catch (IOException ioe)
				{
					_logger.warn(ioe);
				}
				finally
				{
					if (jFile != null)
						try { jFile.close(); } catch (Throwable t) {}
				}
			} else
			{
				InputStream in = null;
				try
				{
					in = new FileInputStream(file);
					Document element = XMLUtils.newDocument(in);
					info._loader = Thread.currentThread().getContextClassLoader();
					className = attemptDeploy(element);
				}
				catch (ParserConfigurationException pce)
				{
					_logger.warn(pce);
				}
				catch (SAXException se)
				{
					_logger.warn(se);
				}
				catch (IOException ioe)
				{
					_logger.warn(ioe);
				}
				finally
				{
					StreamUtils.close(in);
				}
			}
			
			if (className != null && info._loader != null)
			{
				try
				{
					Class<?> cl = info._loader.loadClass(className);
					if (IContainerManaged.class.isAssignableFrom(cl))
	        		{
	        			Constructor<?> cons = cl.getConstructor(new Class[0]);
	        			IContainerManaged base = 
	        				(IContainerManaged)cons.newInstance(new Object[0]);
	        			base.startup();
	        		}
				}
				catch (NoSuchMethodException nsme)
				{
					_logger.error(nsme);
				}
				catch (ClassNotFoundException cnfe)
				{
					_logger.error(cnfe);
				}
				catch (InstantiationException ia)
				{
					_logger.error(ia);
				}
				catch (IllegalAccessException iae)
				{
					_logger.error(iae);
				}
				catch (InvocationTargetException ite)
				{
					_logger.error(ite);
				}
			}
		}
	}
	
	private String attemptDeploy(Document element)
		throws IOException, SAXException, ParserConfigurationException
	{
		String className = findClassName(element);
		if (className == null)
			return null;
		
		element.getDocumentElement().setAttributeNS("http://vcgr.cs.virginia.edu/GenesisII/invoker", "genii:dumm", "dummy-value");
		element.normalizeDocument();
		NodeList list = element.getDocumentElement().getChildNodes();
		for (int lcv = 0; lcv < list.getLength(); lcv++)
		{
			Node child = list.item(lcv);
			if (child != null && child.getNodeType() == Node.ELEMENT_NODE &&
				child.getNodeName().equals("service"))
			{
				((Element)child).setAttributeNS(
					"http://vcgr.cs.virginia.edu/GenesisII/invoker",
					"dummy", "dummy-value");
				String prefix = ((Element)child).lookupPrefix(
					"http://vcgr.cs.virginia.edu/GenesisII/invoker");
				Attr attr = ((Element)child).getAttributeNode("provider");
				attr.setNodeValue(prefix + ":GAroundInvoker");
			}
		}
		
		WSDDDocument wsdd = new WSDDDocument(element);
		EngineConfiguration config = _axisEngine.getConfig();
		if (config instanceof WSDDEngineConfiguration)
		{
			WSDDDeployment deployment =
				((WSDDEngineConfiguration)config).getDeployment();
			wsdd.deploy(deployment);
			_axisEngine.refreshGlobalOptions();
		}

		_logger.info("Deployed \"" + className + "\".");
		return className;
	}
	
	private String attemptDeploy(JarFile jFile, JarEntry entry)
		throws IOException, SAXException, ParserConfigurationException
	{
		InputStream in = null;
		try
		{
			in = jFile.getInputStream(entry);
			Document element = XMLUtils.newDocument(in);
			return attemptDeploy(element);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private final String _WSDD_NS = "http://xml.apache.org/axis/wsdd/";
	
	static private String findClassName(Document element)
	{
		int sLen;
		NodeList serviceList = element.getElementsByTagNameNS(_WSDD_NS, "service");
		sLen = serviceList.getLength();
		for (int lcv1 = 0; lcv1 < sLen; lcv1++)
		{
			Node e = serviceList.item(lcv1);
			if (e.getNodeType() != Node.ELEMENT_NODE)
				continue;
			NodeList nlist = ((Element)e).getElementsByTagNameNS(
				_WSDD_NS, "parameter");
			int len = nlist.getLength();
			for (int lcv = 0; lcv < len; lcv++)
			{
				Node child = nlist.item(lcv);
				NamedNodeMap attrs = child.getAttributes();
				Node name = attrs.getNamedItem("name");
				if (name != null)
				{
					if (name.getTextContent().equals("className"))
					{
						Node cName = attrs.getNamedItem("value");
						if (cName != null)
						{
							return cName.getTextContent();
						}
					}
				}
			}
		}
		
		return null;
	}
}