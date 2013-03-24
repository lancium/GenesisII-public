package edu.virginia.vcgr.genii.container.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.byteio.SByteIOResource;

public class SByteIOFactory implements Closeable
{
	private OutputStream _stream = null;
	private String _serviceURL = null;
	private File _file;

	protected void finalize() throws Throwable
	{
		try {
			StreamUtils.close(this);
		} finally {
			super.finalize();
		}
	}

	static protected File chooseFile() throws IOException
	{
		File userDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
		GuaranteedDirectory sbyteiodir = new GuaranteedDirectory(userDir, "sbyteio");
		return File.createTempFile("sbyteio", ".dat", sbyteiodir);
	}

	SByteIOFactory(String serviceURL) throws IOException
	{
		_file = chooseFile();
		_serviceURL = serviceURL;
		_stream = new FileOutputStream(_file);
	}

	public OutputStream getCreationStream()
	{
		return _stream;
	}

	synchronized public void close() throws IOException
	{
		if (_stream != null) {
			_stream.close();
			_stream = null;
			_file.delete();
		}
	}

	public EndpointReferenceType create() throws ResourceCreationFaultType, GenesisIISecurityException, ResourceException,
		RemoteException, IOException
	{
		synchronized (this) {
			_stream.flush();
			StreamUtils.close(_stream);
			_stream = null;
		}

		StreamableByteIOPortType sbyteio = ClientUtils.createProxy(StreamableByteIOPortType.class,
			EPRUtils.makeEPR(_serviceURL));

		MessageElement[] createRequest = new MessageElement[2];
		createRequest[0] = new MessageElement(RByteIOResource.FILE_PATH_PROPERTY, _file.getAbsolutePath());
		createRequest[1] = new MessageElement(SByteIOResource.MUST_DESTROY_PROPERTY, Boolean.TRUE.toString());

		EndpointReferenceType epr = sbyteio.vcgrCreate(new VcgrCreate(createRequest)).getEndpoint();

		return epr;
	}
}