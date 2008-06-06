package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class UserConfig
{
	static public final String NAMESPACE =
		"http://vcgr.cs.virginia.edu/genii/2007/10/user-config-data";
	static public final String NAMESPACE_SHORT_CUT =
		"user-config";

	static public final String DEPLOYMENT_PATH_ELEMENT = "deployment-path";
	static public QName DEPLOYMENT_PATH_QNAME =
		new QName(NAMESPACE, DEPLOYMENT_PATH_ELEMENT);

	static public final String USER_CONFIG_ELEMENT = "configuration";
	static public QName USER_CONFIG_QNAME =
		new QName(NAMESPACE, USER_CONFIG_ELEMENT);

	private File _deploymentPath;
	
	static private final String [] _nonEscapedElementNames = {
		DEPLOYMENT_PATH_ELEMENT,
		USER_CONFIG_ELEMENT
	};

	public UserConfig(String deploymentPath)
	{
		_deploymentPath = new File(deploymentPath);
	}

	public UserConfig(File file)
		throws FileNotFoundException, IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			initialize(fin);
		}
		catch (SAXException se)
		{
			throw new ConfigurationException(se);
		}
		catch (ParserConfigurationException pce)
		{
			throw new ConfigurationException(pce);
		}
		finally
		{
			if (fin != null)
			{
				try { fin.close(); } catch (IOException ioe) {}
			}
		}
	}

	public UserConfig(InputStream in)
		throws IOException, ConfigurationException
	{
		try
		{
			initialize(in);
		}
		catch (ParserConfigurationException pce)
		{
			throw new ConfigurationException(pce);
		}
		catch (SAXException se)
		{
			throw new ConfigurationException(se);
		}
	}

	public UserConfig(Node node)
	{
		initialize(node);
	}
	
	public void store(File location)
	{
		if (_deploymentPath == null)
			throw new ConfigurationException("Cannot store UserConfig with empty deployment path");
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			doc.appendChild(createUserConfigElement(doc));
			
			FileOutputStream fos = new FileOutputStream(location);

			OutputFormat of = new OutputFormat(doc, "UTF-8",false);
			of.setMethod("XML");
			of.setIndent(1);
			of.setIndenting(true);
			of.setNonEscapingElements(_nonEscapedElementNames);
			XMLSerializer serializer = new XMLSerializer(fos,of);

			// As a DOM Serializer
			serializer.asDOMSerializer();
			doc.normalizeDocument();
			serializer.startNonEscaping();
			serializer.serialize(doc.getDocumentElement());
			fos.close();
		}
		catch(Throwable t)
		{
			throw new ConfigurationException(t);
		}
	}

	public File getDeploymentPath()
	{
		return _deploymentPath;
	}
	
	public void setDeploymentPath(File deploymentPath)
	{
		_deploymentPath = deploymentPath;
	}
	
	
	private void initialize(Node node)
	{
		QName rootNodeQName = XMLConfiguration.getQName(node);
		if (!rootNodeQName.equals(USER_CONFIG_QNAME))
			throw new ConfigurationException("Invalid root element.  Root element must be " + USER_CONFIG_ELEMENT + ".  Name is " + rootNodeQName.toString());

		NodeList children = node.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				QName nodeQName = XMLConfiguration.getQName(n);
				if (nodeQName.equals(DEPLOYMENT_PATH_QNAME))
					handleDeploymentPath(n);
				else
					throw new ConfigurationException("Invalid format - unrecognized XML element " + nodeQName.toString());
			}
		}
	}
	
	private void initialize(InputStream in)
		throws ParserConfigurationException, IOException, SAXException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		initialize(doc.getDocumentElement());
	}
	
	private void handleDeploymentPath(Node deployPathNode)
	{
		NodeList children = deployPathNode.getChildNodes();
		int length = children.getLength();
		
		if (length != 1)
			throw new ConfigurationException("Invalid format for " + DEPLOYMENT_PATH_ELEMENT + " element in user config XML file");
		Node deployText = children.item(0);
		if (deployText.getNodeType() != Node.TEXT_NODE)
			throw new ConfigurationException("Element " + DEPLOYMENT_PATH_ELEMENT + " must be a TEXT_NODE");
		
		_deploymentPath = new File(deployText.getTextContent());
	}
	
	private Element createUserConfigElement(Document doc)
	{
		// create root element
		Element rootElem = doc.createElementNS(NAMESPACE, USER_CONFIG_ELEMENT);
	
		rootElem.appendChild(createDeploymentPathElement(doc));

		return rootElem;
	}
	
	private Element createDeploymentPathElement(Document doc)
	{
		// create root element
		Element deployElem = doc.createElementNS(NAMESPACE, DEPLOYMENT_PATH_ELEMENT);

		Text deployPathValue = doc.createTextNode(_deploymentPath.getAbsolutePath());
		
		deployElem.appendChild(deployPathValue);

		return deployElem;
	}

/*
	static public void main(String [] args) throws ConfigurationException, FileNotFoundException, IOException
	{
		System.out.print("Trying to parse file " + args[0] + "\n");
		File userConfigFile = new File(args[0]);
		UserConfig testInConfig = new UserConfig(userConfigFile);
		System.out.print("Done parsing file " + args[0] + ".  Deployment path is " + testInConfig.getDeploymentPath() + "\n");
		File testFile = new File(testInConfig.getDeploymentPath());
		if (testFile.exists())
			System.out.print("File path works");
			
		UserConfig testOutConfig = new UserConfig(testInConfig.getDeploymentPath());
		File outFile = new File("C:\\workspace\\GenesisII\\testUserConfigOut.xml");
		testOutConfig.store(outFile);
	}
	*/
	
}