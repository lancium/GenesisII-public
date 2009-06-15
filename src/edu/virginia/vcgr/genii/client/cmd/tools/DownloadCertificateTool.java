package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class DownloadCertificateTool extends BaseGridTool
{
	static private final String USAGE =
		"download-certificate <rns-path-to-idp> <target-local-file>";
	static private final String DESCRIPTION =
		"Downloads the public certificate from a remote IDP instance.";
	
	static private void writeFile(InputStream in, File outputFile)
		throws IOException
	{
		FileOutputStream fos = null;
		
		try
		{
			fos = new FileOutputStream(outputFile);
			StreamUtils.copyStream(in, fos);
		}
		finally
		{
			StreamUtils.close(fos);
		}
	}
	
	public DownloadCertificateTool()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath target = current.lookup(getArgument(0), 
			RNSPathQueryFlags.MUST_EXIST);
		TypeInformation typeInfo = new TypeInformation(
			target.getEndpoint());
		
		InputStream in = null;
		
		try
		{
			if (typeInfo.isByteIO())
				in = ByteIOStreamFactory.createInputStream(target);
			else
			{
				EndpointReferenceType epr = target.getEndpoint();
				if (EPRUtils.isUnboundEPR(epr))
					epr = ResolverUtils.resolve(epr);

				X509Certificate[] chain = EPRUtils.extractCertChain(epr);
				if (chain == null || chain.length < 1)
					stderr.println(
						"Remote endpoint does not contain a " +
						"valid certificate chain.");
				in = new ByteArrayInputStream(
					chain[0].getEncoded());
			}
			
			writeFile(in, new File(getArgument(1)));
		}
		finally
		{
			StreamUtils.close(in);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}