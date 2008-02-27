package edu.virginia.vcgr.genii.client.appdesc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType;
import edu.virginia.vcgr.genii.appdesc.bin.BinDeploymentType;
import edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType;
import edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType;
import edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarDeploymentType;
import edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarSourceType;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class ApplicationDescriptionUtils
{
	static public URI DEPLOYMENT_TYPE_ZIPJAR;
	static public URI DEPLOYMENT_TYPE_BINARY;
	
	static
	{
		try
		{
			 DEPLOYMENT_TYPE_ZIPJAR = new URI(
			 	"http://vcgr.cs.virginia.edu/genii/" +
			 	"application-description/zip-jar");
			 DEPLOYMENT_TYPE_BINARY = new URI(
			 	"http://vcgr.cs.virginia.edu/genii/" +
			 	"application-description/bin");
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t.getLocalizedMessage(), t);
		}
	}
	
	static public QName ZIPJAR_DEPLOYMENT_ELEMENT_QNAME = new QName(
		DEPLOYMENT_TYPE_ZIPJAR.toString(), "zip-jar");
	static public QName BINARY_DEPLOYMENT_ELEMENT_QNAME = new QName(
		DEPLOYMENT_TYPE_BINARY.toString(), "binary");
	
	static public URI determineDeploymentType(
		DeploymentDocumentType deployment)
		throws DeploymentException
	{
		MessageElement []any = deployment.get_any();
		if (any == null || any.length != 1)
			throw new DeploymentException("Unknown deployment type.");
		
		QName name = any[0].getQName();
		if (name == null)
			throw new DeploymentException("Unknown deployment type.");
		
		if (name.equals(ZIPJAR_DEPLOYMENT_ELEMENT_QNAME))
			return DEPLOYMENT_TYPE_ZIPJAR;
		else if (name.equals(BINARY_DEPLOYMENT_ELEMENT_QNAME))
			return DEPLOYMENT_TYPE_BINARY;
		else
			throw new DeploymentException("Unknown deployment type.");
	}
	
	static public DeploymentDocumentType uploadLocalSources(
		File deployDirectory,
		DeploymentDocumentType deploymentDocument,
		IUploadProgressListener listener)
		throws IOException, RNSException, ConfigurationException
	{
		MessageElement []any = deploymentDocument.get_any();
		if (any == null || any.length != 1)
			throw new DeploymentException("Unknown deployment type.");
		
		QName name = any[0].getQName();
		if (name == null)
			throw new DeploymentException("Unknown deployment type.");
		
		if (name.equals(ZIPJAR_DEPLOYMENT_ELEMENT_QNAME))
		{
			ZipJarDeploymentType deploymentInfo =
				ObjectDeserializer.toObject(any[0], ZipJarDeploymentType.class);
			
			deploymentInfo = uploadLocalSources(deployDirectory, deploymentInfo,
				listener);
			deploymentDocument.set_any(new MessageElement[] {
				new MessageElement(ZIPJAR_DEPLOYMENT_ELEMENT_QNAME, 
					deploymentInfo)
			});
		} else if (name.equals(BINARY_DEPLOYMENT_ELEMENT_QNAME))
		{
			BinDeploymentType deploymentInfo =
				ObjectDeserializer.toObject(any[0], BinDeploymentType.class);
			
			deploymentInfo = uploadLocalSources(
				deployDirectory, deploymentInfo, listener);
			deploymentDocument.set_any(new MessageElement[] {
				new MessageElement(BINARY_DEPLOYMENT_ELEMENT_QNAME, 
					deploymentInfo)
			});
		} else
		{
			throw new DeploymentException("Unknown deployment type.");
		}
		
		return deploymentDocument;
	}
	
	static private ZipJarDeploymentType uploadLocalSources(
		File deployDirectory, ZipJarDeploymentType deploymentInfo,
		IUploadProgressListener listener)
			throws IOException, RNSException, ConfigurationException
	{
		ZipJarSourceType source = deploymentInfo.getSource();
		
		source.set_any(uploadLocalSource(
			deployDirectory, source.get_any(), listener));
		deploymentInfo.setSource(source);
		
		return deploymentInfo;
	}
	
	static private BinDeploymentType uploadLocalSources(
		File deployDirectory, BinDeploymentType deploymentInfo,
		IUploadProgressListener listener)
			throws IOException, RNSException, ConfigurationException
	{
		NamedSourceType []sources = deploymentInfo.getBinary();
		if (sources != null)
		{
			for (int lcv = 0; lcv < sources.length; lcv++)
				sources[lcv].set_any(uploadLocalSource(
					deployDirectory, sources[lcv].get_any(),
					listener));
			deploymentInfo.setBinary(sources);
		}
		
		sources = deploymentInfo.getSharedLibrary();
		if (sources != null)
		{
			for (int lcv = 0; lcv < sources.length; lcv++)
				sources[lcv].set_any(uploadLocalSource(
					deployDirectory, sources[lcv].get_any(),
					listener));
			deploymentInfo.setSharedLibrary(sources);
		}
		
		RelativeNamedSourceType []otherSources = deploymentInfo.getStaticFile();
		if (otherSources != null)
		{
			for (int lcv = 0; lcv < otherSources.length; lcv++)
				otherSources[lcv].set_any(uploadLocalSource(deployDirectory,
					otherSources[lcv].get_any(), listener));
			deploymentInfo.setStaticFile(otherSources);
		}
		
		return deploymentInfo;
	}
	
	static public QName LOCAL_FILE_SOURCE_NAME = new QName(
		ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_NS,
		"local-file");
	static public QName RNS_FILE_SOURCE_NAME = new QName(
		ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_NS,
		"rns-file");
	static public QName REMOTE_ENDPOINT_NAME = new QName(
		ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_NS,
		"remote-endpoint");
	
	static private MessageElement[] uploadLocalSource(
		File deployDirectory, MessageElement []any,
		IUploadProgressListener listener)
		throws IOException, RNSException, ConfigurationException
	{
		if (any == null)
			return null;
		
		for (int lcv = 0; lcv < any.length; lcv++)
		{
			QName name = any[lcv].getQName();
			if (name.equals(LOCAL_FILE_SOURCE_NAME))
			{
				String localPath = any[lcv].getValue();
				any[lcv] = new MessageElement(
					REMOTE_ENDPOINT_NAME,
					uploadLocalFile(deployDirectory, localPath, listener));
			}
		}
		
		return any;
	}
	
	static private EndpointReferenceType uploadLocalFile(
		File deployDirectory, String localPath,
		IUploadProgressListener listener)
		throws IOException
	{
		ByteIOOutputStream bos = null;
		EndpointReferenceType newFile = null;
		FileInputStream fin = null;
		EndpointReferenceType ret;
		
		File deployFile = new File(localPath);
		if (!deployFile.isAbsolute())
			deployFile = new File(deployDirectory, localPath);
		
		if (listener != null)
			listener.startingUpload(deployFile);
		
		try
		{
			fin = new FileInputStream(deployFile);
			newFile = ResourceCreator.createNewResource(
				"RandomByteIOPortType", null, null);
			bos = new ByteIOOutputStream(newFile);
			
			StreamUtils.copyStream(fin, bos);
			ret = newFile;
			newFile = null;
			
			return ret;
		}
		catch (ConfigurationException ce)
		{
			throw new IOException("Unable to create ByteIO Resource:  " +
				ce.getLocalizedMessage());
		}
		catch (CreationException ce)
		{
			throw new IOException("Unable to create ByteIO Resource:  " +
				ce.getLocalizedMessage());
		}
		finally
		{
			StreamUtils.close(fin);
			StreamUtils.close(bos);
			
			if (newFile != null)
			{
				try
				{
					GeniiCommon common = ClientUtils.createProxy(
					GeniiCommon.class, newFile);
					common.destroy(new Destroy());
				}
				catch (Throwable t)
				{
				}
			}
			
			if (listener != null)
				listener.finishedUpload(deployFile);
		}
	}
}