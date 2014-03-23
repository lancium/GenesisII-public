package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class DownloadCertificateTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(DownloadCertificateTool.class);

	static private final String USAGE = "config/tooldocs/usage/udownload-certificate";
	static private final String DESCRIPTION = "config/tooldocs/description/ddownload-certificate";

	static private void writeFile(InputStream in, File outputFile) throws IOException
	{
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(outputFile);
			StreamUtils.copyStream(in, fos);
		} finally {
			StreamUtils.close(fos);
		}
	}

	public DownloadCertificateTool()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), true, ToolCategory.SECURITY);
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<rns-path-to-idp> must be a grid path. ");
		GeniiPath localPath = new GeniiPath(getArgument(1));
		if (localPath.pathType() != GeniiPathType.Local)
			throw new InvalidToolUsageException("<target-local-file> must be a local path begining with 'local:' ");
		RNSPath target = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);
		TypeInformation typeInfo = new TypeInformation(target.getEndpoint());

		InputStream in = null;

		try {
			if (typeInfo.isByteIO())
				in = ByteIOStreamFactory.createInputStream(target);
			else {
				EndpointReferenceType epr = target.getEndpoint();
				if (EPRUtils.isUnboundEPR(epr))
					epr = ResolverUtils.resolve(epr);

				X509Certificate[] chain = EPRUtils.extractCertChain(epr);
				if (chain == null || chain.length < 1)
					stderr.println("Remote endpoint does not contain a " + "valid certificate chain.");
				else
					in = new ByteArrayInputStream(chain[0].getEncoded());
			}
			if (in != null)
				writeFile(in, new File(localPath.path()));
		} catch (CertificateEncodingException e) {
			String msg = "a creation exception occurred during the operation: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			throw new AuthZSecurityException(msg, e);
		} catch (AuthZSecurityException e) {
			String msg = "a security exception occurred during the operation: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			throw new AuthZSecurityException(msg, e);
		} finally {
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