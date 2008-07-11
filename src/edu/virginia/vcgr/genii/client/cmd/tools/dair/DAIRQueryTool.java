package edu.virginia.vcgr.genii.client.cmd.tools.dair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceRequest;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceResponse;
import edu.virginia.vcgr.genii.genesis_dair.SQLAccessCombinedPortType;

public class DAIRQueryTool extends BaseGridTool {
	static private final String _DESCRIPTION =
		"You can use this tool to query tables through the GenesisII DAIR services";
	static private final String _USAGE =
		"dair-query <DAIR service> <password> <resourceName>";
	
	public DAIRQueryTool() {
		super(_DESCRIPTION, _USAGE, false);
	}

	/**
	 * @throws Throwable
	 */
	@Override
	protected int runCommand() throws Throwable {
		query(new String(getArgument(0)), 
				new String(getArgument(1)), 
				new String(getArgument(2)));
		return 0;
	}

	/**
	 * @throws ToolException
	 */
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
		
	}

	/**
	 * @param databaseDriverPath
	 * @param servicePath
	 * @param query - the query to be executed
	 * @param username 
	 * @param password
	 * @param resourceName - the name of the new DAIR resource
	 * @throws Throwable
	 */
	private void query ( String servicePath, String query, String resourceName) 
		throws Throwable
	{
		
		RNSPath path = RNSPath.getCurrent().lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);

		/*
		 * preparing the parameters to be passed on
		 */
		
		SQLAccessCombinedPortType SQLAccess = ClientUtils.createProxy(
				SQLAccessCombinedPortType.class, path.getEndpoint());
		CreateDataResourceRequest myRequest = new CreateDataResourceRequest(); 
		myRequest.setQuery(query);
		myRequest.setResourceName(resourceName);
		CreateDataResourceResponse result = (CreateDataResourceResponse) SQLAccess.createDataResource(myRequest);
		System.out.println(result.getEPR());
		
		
		/*
		 * creating the new directory for the results
		 */

		@SuppressWarnings("unused")
		RNSPath resultsDirRNS = createResultDir(path);
		String trying = resultsDirRNS.pwd();
		System.out.println(trying);
		
		RNSPath filePath = new RNSPath(resultsDirRNS, resourceName, null, false);
		String file = filePath.pwd();
		System.out.println(file);
		EndpointReferenceType fileEPR = filePath.createNewFile();
		
		InputStream inStream = null;
		OutputStream outStream = null;
		
		
		EndpointReferenceType EPR = result.getEPR();
		String EPRtoString = ObjectSerializer.toString(EPR, new QName(GenesisIIConstants.GENESISII_NS, "endpoint"), false);
		inStream = new ByteArrayInputStream(EPRtoString.getBytes());
		outStream = ByteIOStreamFactory.createOutputStream(fileEPR);
		copy (inStream, outStream);
		
		
		
		StreamUtils.close(inStream);
		StreamUtils.close(outStream);
		
		
		/*File dataResource = new File (resultsDirRNS.toString(), resourceName+".txt");
		dataResource.canWrite();
		dataResource.canRead();
		dataResource.createNewFile(); */
		
		
		/*
		File dataResource = new File (resultsDirRNS + File.separator + resourceName +".txt");
	//	String filepath = dataResource.getCanonicalPath();
		BufferedWriter output = new BufferedWriter(new FileWriter(dataResource));
		output.write(result);
		output.close();
		*/
	}
	/**
	 * @throws RNSException
	 * 
	 * creates the directory where the results will be stored
	 */
	static public RNSPath createResultDir(RNSPath path) throws RNSException
	{
		String resultsDirRNSPath = path.pwd() + "-results";
		RNSPath resultsDirRNS = RNSPath.getCurrent().lookup(resultsDirRNSPath, RNSPathQueryFlags.DONT_CARE);
		if (!resultsDirRNS.exists())
		{
			resultsDirRNS.mkdir();
		}
		return resultsDirRNS;
	}
	
	static private final int _BLOCK_SIZE = ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE;
	
	static private void copy(InputStream in, OutputStream out)
	throws IOException
	{
		byte []data = new byte[_BLOCK_SIZE];
		int r;
		
		while ( (r = in.read(data)) >= 0){
			out.write(data, 0, r);
		}
	}

}
