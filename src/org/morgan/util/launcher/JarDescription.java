/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JarDescription
{
	static private final String _NAMESPACE = "http://vcgr.cs.virginia.edu";
	
	static private final String _BASEDIR_ATTR_NAME = "basedir";
	static private final String _BASEDIR_ATTR_DEFAULT = ".";
	static private final String _RECURSIVE_ATTR_NAME = "recursive";
	static private final String _RECURSIVE_ATTR_DEFAULT = "true";
	
	static private QName _ROOT_QNAME = new QName(_NAMESPACE, "jars");
	static private QName _JAR_FILES_QNAME = new QName(_NAMESPACE, "jar-files");
	static private QName _JAR_FILE_QNAME = new QName(_NAMESPACE, "jar-file");
	static private QName _JAR_DIR_QNAME = new QName(_NAMESPACE, "jar-dir");
	
	static private QName getQName(Node node)
	{
		return new QName(node.getNamespaceURI(), node.getLocalName());
	}
	
	private Vector<URL> _jarFiles = new Vector<URL>();
	
	private void parseJarFiles(Node jarFilesNode)
		throws IOException
	{
		File baseDir;
		
		NamedNodeMap attrs = jarFilesNode.getAttributes();
		Node attr = attrs.getNamedItem(_BASEDIR_ATTR_NAME);
		if (attr == null)
			baseDir = new File(_BASEDIR_ATTR_DEFAULT);
		else
			baseDir = new File(attr.getTextContent());
		
		NodeList children = jarFilesNode.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = getQName(child);
				if (childQName.equals(_JAR_FILE_QNAME))
				{
					Node textNode = child.getFirstChild();
					if (textNode.getNodeType() != Node.TEXT_NODE)
						throw new IOException(
							"Found jar file node without text content.");
					
					File jarFile = new File(baseDir, textNode.getTextContent());
					_jarFiles.add(jarFile.toURI().toURL());
				} else
					throw new IOException("Expected " + _JAR_FILE_QNAME);
			}
		}
	}
	
	private void findJars(File jarDir, boolean isRecursive)
		throws MalformedURLException
	{
		for (File testFile : jarDir.listFiles())
		{
			if (testFile.isDirectory() && isRecursive)
				findJars(testFile, isRecursive);
			else if (testFile.getName().endsWith(".jar") && testFile.isFile())
				_jarFiles.add(testFile.toURI().toURL());
		}
	}
	
	private void parseJarDir(Node jarDirNode)
		throws IOException
	{
		boolean isRecursive = Boolean.parseBoolean(_RECURSIVE_ATTR_DEFAULT);
		
		NamedNodeMap attrs = jarDirNode.getAttributes();
		Node attr = attrs.getNamedItem(_RECURSIVE_ATTR_NAME);
		if (attr != null)
			isRecursive = Boolean.parseBoolean(attr.getTextContent());
		
		Node textNode = jarDirNode.getFirstChild();
		if (textNode.getNodeType() != Node.TEXT_NODE)
			throw new IOException(
				"Found jar dir node without text content.");
		
		File jarDir = new File(textNode.getTextContent());
		findJars(jarDir, isRecursive);
	}
	
	private void initialize(Node node)
		throws IOException
	{
		if (!getQName(node).equals(_ROOT_QNAME))
			throw new IOException("Root element not correct -- expected " 
				+ _ROOT_QNAME);
		
		NodeList children = node.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = getQName(child);
				
				if (childQName.equals(_JAR_FILES_QNAME))
					parseJarFiles(child);
				else if (childQName.equals(_JAR_DIR_QNAME))
					parseJarDir(child);
				else
					throw new IOException("Unexpected XML element " + childQName);
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
	
	public JarDescription(String filepath)
		throws FileNotFoundException, IOException
	{
		this(new File(filepath));
	}
	
	public JarDescription(File file)
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
			throw new IOException(se.getLocalizedMessage());
		}
		catch (ParserConfigurationException pce)
		{
			throw new IOException(pce.getLocalizedMessage());
		}
		finally
		{
			if (fin != null)
			{
				try { fin.close(); } catch (IOException ioe) {}
			}
		}
	}
	
	public JarDescription(InputStream in)
		throws IOException
	{
		try
		{
			initialize(in);
		}
		catch (ParserConfigurationException pce)
		{
			throw new IOException(pce.getLocalizedMessage());
		}
		catch (SAXException se)
		{
			throw new IOException(se.getLocalizedMessage());
		}
	}
	
	public ClassLoader createClassLoader() throws IOException
	{
		/*
		URL []jars = new URL[_jarFiles.size()];
		_jarFiles.toArray(jars);
		return new URLClassLoader(jars,
			Thread.currentThread().getContextClassLoader());
		*/
		
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		
		try
		{
			Method method = sysclass.getDeclaredMethod("addURL", new Class<?> [] { URL.class} );
			method.setAccessible(true);
			for (URL u : _jarFiles)
			{
				method.invoke(sysloader, new Object[] { u });
			}
			
			return sysloader;
		} catch (Throwable t)
		{
			t.printStackTrace(System.err);
			throw new IOException("Unable to modify system class loader.");
		}
	}
}
