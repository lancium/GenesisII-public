package edu.virginia.vcgr.appmgr.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.util.ElementIterable;
import edu.virginia.vcgr.appmgr.util.XMLUtilities;

public class JarDescription
{
	static private Log _logger = LogFactory.getLog(JarDescription.class);

	static private final String NAMESPACE = "http://vcgr.cs.virginia.edu";

	static private final String BASEDIR_ATTR_NAME = "basedir";
	static private final String BASEDIR_ATTR_DEFAULT = ".";
	static private final String RECURSIVE_ATTR_NAME = "recursive";
	static private final String RECURSIVE_ATTR_DEFAULT = "true";

	static private final QName ROOT_QNAME = new QName(NAMESPACE, "jars");
	static private final QName JAR_FILES_QNAME = new QName(NAMESPACE, "jar-files");
	static private final QName JAR_FILE_QNAME = new QName(NAMESPACE, "jar-file");
	static private final QName JAR_DIR_QNAME = new QName(NAMESPACE, "jar-dir");

	private Collection<URL> _jarFiles = new LinkedList<URL>();

	private void parseJarFiles(Node jarFilesNode) throws IOException, SAXException
	{
		File basedir = new File(XMLUtilities.getAttribute(jarFilesNode, BASEDIR_ATTR_NAME, BASEDIR_ATTR_DEFAULT));

		for (Element child : new ElementIterable(jarFilesNode.getChildNodes())) {
			QName childQName = XMLUtilities.getQName(child);
			if (childQName.equals(JAR_FILE_QNAME)) {
				File jarFile = new File(XMLUtilities.getTextContent(child));
				if (!jarFile.isAbsolute())
					jarFile = new File(basedir, jarFile.getPath());
				_jarFiles.add(jarFile.toURI().toURL());
			} else
				throw new IOException(String.format("Expected:  %s.", JAR_FILE_QNAME));
		}
	}

	private void findJars(File jarDir, boolean isRecursive)
	{
		for (File testFile : jarDir.listFiles()) {
			if (testFile.isDirectory() && isRecursive)
				findJars(testFile, isRecursive);
			else if (testFile.getName().endsWith(".jar") && testFile.isFile()) {
				try {
					_jarFiles.add(testFile.toURI().toURL());
				} catch (MalformedURLException mue) {
					_logger.error("Got an unexpected malformed URL exception.", mue);
				}
			}
		}
	}

	private void parseJarDir(Element jarDirNode) throws IOException, SAXException
	{
		boolean isRecursive =
			Boolean.parseBoolean(XMLUtilities.getAttribute(jarDirNode, RECURSIVE_ATTR_NAME, RECURSIVE_ATTR_DEFAULT));

		File jarDir = new File(XMLUtilities.getTextContent(jarDirNode));
		findJars(jarDir, isRecursive);
	}

	private void initialize(Node node) throws IOException, SAXException
	{
		if (!XMLUtilities.getQName(node).equals(ROOT_QNAME))
			throw new IOException(String.format("Root element not correct -- expected %s.", ROOT_QNAME));

		for (Element child : new ElementIterable(node.getChildNodes())) {
			QName childQName = XMLUtilities.getQName(child);
			if (childQName.equals(JAR_FILES_QNAME))
				parseJarFiles(child);
			else if (childQName.equals(JAR_DIR_QNAME))
				parseJarDir(child);
			else
				throw new IOException(String.format("Unexpected XML element %s.", childQName));
		}
	}

	private void initialize(InputStream in) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		initialize(doc.getDocumentElement());
	}

	public JarDescription(String filepath) throws FileNotFoundException, IOException
	{
		this(new File(filepath));
	}

	public JarDescription(File file) throws FileNotFoundException, IOException
	{
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(file);
			initialize(fin);
		} catch (SAXException se) {
			throw new IOException(String.format("Unable to parse jar description file %s.", file), se);
		} catch (ParserConfigurationException pce) {
			throw new IOException(String.format("Unable to parse jar description file %s.", file), pce);
		} finally {
			IOUtils.close(fin);
		}
	}

	public JarDescription(InputStream in) throws IOException
	{
		try {
			initialize(in);
		} catch (SAXException se) {
			throw new IOException("Unable to parse jar description", se);
		} catch (ParserConfigurationException pce) {
			throw new IOException("Unable to parse jar description", pce);
		}
	}

	public ClassLoader createClassLoader() throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", new Class<?>[] { URL.class });
			method.setAccessible(true);
			for (URL u : _jarFiles)
				method.invoke(sysloader, new Object[] { u });

			return sysloader;
		} catch (Throwable cause) {
			throw new IOException("Unable to modify system class loader.", cause);
		}
	}
}