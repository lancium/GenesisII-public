package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Vector;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

class RemoteIdentityLookupTask extends AbstractTask<X509Identity[]>
{
	private Collection<RNSPath> _targets;
	
	private X509Identity readFromByteIO(EndpointReferenceType target)
		throws FileNotFoundException, RemoteException, IOException, CertificateException
	{
		InputStream in = null;
		
		try
		{
			in = ByteIOStreamFactory.createInputStream(target);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
			X509Certificate []chain = { cert };
			return new X509Identity(chain);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	RemoteIdentityLookupTask(Collection<RNSPath> targets)
	{
		_targets = targets;
	}
	
	@Override
	public X509Identity[] execute(TaskProgressListener progressListener)
			throws Exception
	{
		Collection<X509Identity> result = new Vector<X509Identity>(
			_targets.size());
		
		for (RNSPath target : _targets)
		{
			progressListener.updateSubTitle("Getting remote type information.");
			TypeInformation typeInfo = new TypeInformation(target.getEndpoint());
			if (typeInfo.isByteIO())
			{
				progressListener.updateSubTitle(
					"Reading certificate information from ByteIO.");
				result.add(readFromByteIO(target.getEndpoint()));
			} else
			{
				progressListener.updateSubTitle(
					"Getting resource identity.");
				EndpointReferenceType epr = target.getEndpoint();
				if (EPRUtils.isUnboundEPR(epr))
					epr = ResolverUtils.resolve(epr);
				
				X509Certificate []chain = EPRUtils.extractCertChain(epr);
				result.add(new X509Identity(chain));
			}
		}
		
		return result.toArray(new X509Identity[result.size()]);
	}
}