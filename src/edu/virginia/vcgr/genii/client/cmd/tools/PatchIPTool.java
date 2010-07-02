package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;

import org.ggf.rns.List;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.RNSPortType;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.client.gpath.*;

public class PatchIPTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Patches a bunch of machines into the grid after they have been lost.";
	
	static private final String _USAGE =
		"patch-ip [--ignore=<ignore-file>] <ip-mask> <hostname-jsdl> <log-file> <output-file>";
	
	private String _ignoreFile = null;
	private RNSPath _containers;
	private RNSPath _besContainers;
	
	private boolean isWorthTrying(String ip)
	{
		try
		{
			URL url = new URL(String.format("http://%s:18080", ip));
			URLConnection urlConnection  = url.openConnection();
			urlConnection.setConnectTimeout(1000 * 15);
			urlConnection.connect();
			return true;
		}
		catch (Throwable cause)
		{
			return false;
		}
	}
	
	/*
	private boolean isWorthTrying(String ip)
	{
		HttpsURLConnection connection = null;
		
		try	
		{
			URL url = new URL(String.format("https://%s:18080", ip));
			URLConnection urlConnection  = url.openConnection();
			urlConnection.setConnectTimeout(1000 * 15);
			connection = (HttpsURLConnection)urlConnection;
			connection.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session)
				{
					System.err.println("Asked to verify.");
					return true;
				}
				
			});
			connection.connect();
		}
		catch (SSLHandshakeException she)
		{
			// This is OK
		}
		catch (Throwable cause)
		{
			return false;
		}
		finally
		{
			if (connection != null)
				connection.disconnect();
		}
		
		return true;
	}
	*/
	
	private EndpointReferenceType attachIP(String ip)
	{
		if (!isWorthTrying(ip))
			return null;
		
		try
		{
			String containerURL = String.format(
				"https://%s:18080/axis/services/VCGRContainerPortType", ip);
			
			containerURL = Hostname.normalizeURL(containerURL);
			ClientUtils.setDefaultTimeoutForThread(new Long(1000L * 15));
			OGSARP rp = (OGSARP)ResourcePropertyManager.createRPInterface(
				EPRUtils.makeEPR(containerURL), OGSARP.class);
			
			return rp.getResourceEndpoint();
		}
		catch (Throwable cause)
		{
			stdout.format("Unable to communicate with %s.\n", ip);
			return null;
		}
		finally
		{
			ClientUtils.setDefaultTimeoutForThread(null);
		}
	}
	
	private EndpointReferenceType createBES(EndpointReferenceType container) 
		throws RNSEntryNotDirectoryFaultType, RNSFaultType, 
			ResourceUnknownFaultType, RemoteException
	{
		RNSPortType rpt = ClientUtils.createProxy(
			RNSPortType.class, container);
		EndpointReferenceType services = rpt.list(new List("Services")).getEntryList()[0].getEntry_reference();
		rpt = ClientUtils.createProxy(RNSPortType.class, services);
		EndpointReferenceType besService = rpt.list(new List("GeniiBESPortType")).getEntryList()[0].getEntry_reference();
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, besService);
		return common.vcgrCreate(new VcgrCreate()).getEndpoint();
	}
	
	private String getHostname(EndpointReferenceType bes) 
		throws IOException, RNSException, InvalidToolUsageException
	{
		EndpointReferenceType job = null;
		ActivityState state = null;
		
		try
		{
			job = RunTool.submitJob(getArgument(1), bes, null, null);
			
			for (int lcv = 0; lcv < 10; lcv++)
			{
				state = null;
				state = RunTool.checkStatus(bes, job);
				if (state.isFinalState())
					break;
				try { Thread.sleep(1000L * 15); } catch (Throwable cause) {}
			}
		}
		finally
		{
			try
			{
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, job);
				common.destroy(new Destroy());
			}
			catch (Throwable cause)
			{
				// Ignore
			}
		}
		
		if (state == null || state.isFailedState())
			throw new RuntimeException(
				"Unable to run job to determine hostname.");
		
		RNSPath path = RNSPath.getCurrent();
		InputStream in = null;
		
		RNSPath file = null;
		try
		{
			GeniiPath gPath = new GeniiPath(getArgument(3));
			if(gPath.pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException("<output-file> must be a grid path. ");
			file = path.lookup(new GeniiPath(getArgument(3)).path());
			in = ByteIOStreamFactory.createInputStream(file);
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(in));
			String line = null;
			String lastLine = null;
			
			while ( (line = reader.readLine()) != null)
			{
				lastLine = line;
			}
			if (lastLine == null)
				throw new RuntimeException(
					"Unable to determine name of host for ip.\n");
			return lastLine.trim();
		}
		finally
		{
			StreamUtils.close(in);
			try
			{
				if (file != null)
					file.delete(); 
			}
			catch (Throwable cause)
			{
				// Do nothing
			}
		}
	}
	
	private PatchResult patchIP(String ip)
	{
		EndpointReferenceType bes = null;
		String hostname = null;
		
		try
		{
			stdout.format("Attempting to re-attach %s\n", ip);
			EndpointReferenceType container = attachIP(ip);
			if (container == null)
				return new PatchResult(ip, hostname, 
					new RuntimeException("Host did not respond."));
			
			bes = createBES(container);
			
			hostname = getHostname(bes);
			stdout.format("Attaching %s as %s.\n", ip, hostname);
			
			RNSPath containerPath = _containers.lookup(
				hostname, RNSPathQueryFlags.MUST_NOT_EXIST);
			RNSPath besPath = _besContainers.lookup(
				hostname, RNSPathQueryFlags.MUST_NOT_EXIST);
			
			containerPath.link(container);
			besPath.link(bes);
			return new PatchResult(ip, hostname, null);
		}
		catch (Throwable cause)
		{
			if (bes != null)
			{
				stderr.format("Destroying BES @ %s\n", ip);
				try
				{
					GeniiCommon common = ClientUtils.createProxy(
						GeniiCommon.class, bes);
					common.destroy(new Destroy());
				}
				catch (Throwable t)
				{
					stderr.format("Unable to destroy bes @ %s:  %s\n", ip, t);
				}
			}
			stderr.format("Error @ %s:  %s\n", ip, cause);
			return new PatchResult(ip, hostname, cause);
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		PrintWriter log = null;
		
		try
		{
			log = new PrintWriter(getArgument(2));
			
			RNSPath current = RNSPath.getCurrent();
			_containers = current.lookup("containers");
			_besContainers = current.lookup("bes-containers");
			
			IPRange range = new IPRange(getArgument(0),
				(_ignoreFile == null) ? null : new File(_ignoreFile));
			
			for (String ipAddr : range)
			{
				PatchResult result = patchIP(ipAddr);
				log.println(result);
				log.flush();
			}
			
			return 0;
		}
		finally
		{
			StreamUtils.close(log);
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 4)
			throw new InvalidToolUsageException();
	}
	
	public PatchIPTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	public void setIgnore(String ignoreFile)
	{
		_ignoreFile = ignoreFile;
	}
	
	static private class PatchResult
	{
		private String _ip;
		private String _hostname;
		private Throwable _failure;
		
		private PatchResult(String ip, String hostname, Throwable failure)
		{
			_ip = ip;
			_hostname = hostname;
			_failure = failure;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder(_ip);
			
			if (_hostname != null)
				builder.append(String.format(" -> %s", _hostname));
			
			if (_failure != null)
				builder.append(" [Failed]");
			else
				builder.append(" [Succeeded]");
			
			return builder.toString();
		}
	}
}