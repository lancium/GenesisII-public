package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.appdesc.SourceElementType;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionUtils;
import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public abstract class AbstractDeploymentProvider implements IDeployerProvider
{
	protected EndpointReferenceType _deploymentDescriptionEPR;

	private DeploySnapshot _snapshot = null;

	protected AbstractDeploymentProvider(EndpointReferenceType depDescEPR)
	{
		_deploymentDescriptionEPR = depDescEPR;
	}

	public DeploySnapshot getSnapshot() throws DeploymentException
	{
		synchronized (this) {
			if (_snapshot == null)
				_snapshot = figureOutSnapshot();
		}

		return _snapshot;
	}

	static private EndpointReferenceType getSourceEndpoint(SourceElementType source) throws DeploymentException
	{
		MessageElement[] sourceElements = source.get_any();
		if (sourceElements == null || sourceElements.length != 1)
			throw new DeploymentException("Invalid source element found.");

		QName name = sourceElements[0].getQName();
		if (name.equals(ApplicationDescriptionUtils.RNS_FILE_SOURCE_NAME))
			return getSourceEndpoint(sourceElements[0].getValue());
		else if (name.equals(ApplicationDescriptionUtils.REMOTE_ENDPOINT_NAME)) {
			try {
				return ObjectDeserializer.toObject(sourceElements[0], EndpointReferenceType.class);
			} catch (ResourceException re) {
				throw new DeploymentException("Unable to get source.", re);
			}
		} else
			throw new DeploymentException("Invalid source element found.");
	}

	static private EndpointReferenceType getSourceEndpoint(String rnsPath) throws DeploymentException
	{
		try {
			RNSPath path = RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_EXIST);
			return path.getEndpoint();
		} catch (RNSException rne) {
			throw new DeploymentException("Unable to lookup deployment path.", rne);
		}
	}

	static protected DeployFacet getDeployFacet(SourceElementType source) throws DeploymentException
	{
		EndpointReferenceType endpoint = getSourceEndpoint(source);
		return new DeployFacet(new WSName(endpoint).getEndpointIdentifier().toString(), getModificationTime(endpoint));
	}

	static protected InputStream openSource(SourceElementType source) throws DeploymentException, IOException
	{
		EndpointReferenceType epr = getSourceEndpoint(source);
		return ByteIOStreamFactory.createInputStream(epr);
	}

	static protected void downloadFile(SourceElementType source, File target, boolean makeReadOnly, boolean makeExecutable)
		throws DeploymentException
	{
		InputStream in = null;
		OutputStream out = null;

		try {
			try {
				in = openSource(source);
				out = new FileOutputStream(target);
				StreamUtils.copyStream(in, out);
				out.flush();
			} finally {
				StreamUtils.close(in);
				StreamUtils.close(out);
			}

			if (makeReadOnly)
				target.setReadOnly();
			if (makeExecutable)
				FileSystemUtils.makeExecutable(target);
		} catch (IOException ioe) {
			throw new DeploymentException("Unable to deploy component to " + target, ioe);
		}
	}

	static protected DeployFacet getDeploymentDescriptionFacet(EndpointReferenceType depDescEPR) throws DeploymentException
	{
		String deployID = new WSName(depDescEPR).getEndpointIdentifier().toString();
		return new DeployFacet(deployID, getModificationTime(depDescEPR));
	}

	static private Timestamp getModificationTime(EndpointReferenceType target) throws DeploymentException
	{
		try {
			RandomByteIORP rp = (RandomByteIORP) ResourcePropertyManager.createRPInterface(target, RandomByteIORP.class);

			Calendar modTime = rp.getModificationTime();

			return new Timestamp(modTime.getTime().getTime());
		} catch (ResourcePropertyException re) {
			throw new DeploymentException("Can't get source attributes.", re);
		}
	}

	protected abstract DeploySnapshot figureOutSnapshot() throws DeploymentException;
}