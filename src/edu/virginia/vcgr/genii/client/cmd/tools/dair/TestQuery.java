package edu.virginia.vcgr.genii.client.cmd.tools.dair;


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

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
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceRequest;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceResponse;
import edu.virginia.vcgr.genii.genesis_dair.SQLAccessCombinedPortType;


public class TestQuery extends BaseGridTool{
	
	static private final String _DESCRIPTION =
		"You can use this tool to query tables through the GenesisII DAIR services";
	static private final String _USAGE =
		"dair-query <DAIR service> <query> <resourceName>";
	
	public TestQuery() {
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
	 * @param servicePath
	 * @param query - the query to be executed
	 * @param resourceName - the name of the new DAIR resource
	 * @throws Throwable
	 */
	private void query (String servicePath, String query, String resourceName) 
		throws Throwable
	{
		RNSPath path = RNSPath.getCurrent().lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		
		//preparing the parameters to be passed on
		
		SQLAccessCombinedPortType SQLAccess = ClientUtils.createProxy(
				SQLAccessCombinedPortType.class, path.getEndpoint());
		CreateDataResourceRequest myRequest = new CreateDataResourceRequest(); 
		myRequest.setQuery(query);
		myRequest.setResourceName(resourceName);
		CreateDataResourceResponse result =  (CreateDataResourceResponse) SQLAccess.createDataResource(myRequest);
		
		createFileStructure(path, result, resourceName);

	}
	
	public void createFileStructure(RNSPath path,CreateDataResourceResponse result, 
			String resourceName) throws RNSException, FileNotFoundException, RemoteException, IOException{
		System.out.println(result.getEPR());
		
		
		//creating the new directory for the EPR results
		RNSPath EPRDirRNS = createEPRDir(path);
		String trying = EPRDirRNS.pwd();
		System.out.println(trying);
		
		RNSPath filePath = new RNSPath(EPRDirRNS, resourceName, null, false);
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
		
		/*
		 * creating a new directory for the data results
		 */
		
		RNSPath resultsDirRNS = createResultsDir(path);
		String resultstrying = resultsDirRNS.pwd();
		System.out.println(resultstrying);
		
		RNSPath resultsFilePath = new RNSPath(resultsDirRNS, resourceName, null, false);
		String resultsfile = resultsFilePath.pwd();
		System.out.println(resultsfile);
		EndpointReferenceType resultsFileEPR = resultsFilePath.createNewFile();
		
		String sqlQueryResult = result.getResultset();
		
		inStream = new ByteArrayInputStream(sqlQueryResult.getBytes());
		outStream = ByteIOStreamFactory.createOutputStream(resultsFileEPR);
		copy (inStream, outStream);
		StreamUtils.close(inStream);
		StreamUtils.close(outStream);
		
		
		/*
		 * creating a new directory for the schemas
		 */
		
		RNSPath schemasDirRNS = createSchemasDir(path);
		trying = schemasDirRNS.pwd();
		System.out.println(trying);
		
		int numberOfTables = result.getTableName().length;
		for (int i = 0; i< numberOfTables; i++)
		{
			String tableSchema = result.getTableName(i);
			int tableNameLength = tableSchema.indexOf("\n");
			String tableName = tableSchema.substring(0, tableNameLength);
			
			RNSPath schemaPath = new RNSPath(schemasDirRNS, tableName, null, false);
			String schema = schemaPath.pwd();
			System.out.println(schema);
			try {
			EndpointReferenceType schemaEPR = schemaPath.createNewFile();
			
			inStream = new ByteArrayInputStream(("table " +tableSchema).getBytes());
			outStream = ByteIOStreamFactory.createOutputStream(schemaEPR);
			copy (inStream, outStream);
			StreamUtils.close(inStream);
			StreamUtils.close(outStream);
			}
			catch (RNSPathAlreadyExistsException e)
			{
				/*
				 * Don't do anything. If the path already exists then we've simply added the 
				 * table schema before.
				 */
				
			}
		}
		
	}
	
	/**
	 * @param path
	 * @return resutlsDirRNS - the path to the new directory where the table resulting EPRs will be kept
	 * @throws RNSException
	 * creates the directory where the results will be stored
	 */
	static public RNSPath createEPRDir(RNSPath path) throws RNSException
	{
		String EPRDirRNSPath = path.getParent().pwd() + "SQLQueries-EPRs";
		RNSPath EPRDirRNS = RNSPath.getCurrent().lookup(EPRDirRNSPath, RNSPathQueryFlags.DONT_CARE);
		if (!EPRDirRNS.exists())
		{
			EPRDirRNS.mkdir();
		}
		return EPRDirRNS;
	}
	
	/**
	 * @param path
	 * @return schemasDirRNS - the path to the new directory where the table schemas will be kept
	 * @throws RNSException
	 */
	
	static public RNSPath createSchemasDir(RNSPath path) throws RNSException
	{
		String schemasDirRNSPath = path.getParent().pwd() + "SQLQueries-schemas";
		RNSPath schemasDirRNS = RNSPath.getCurrent().lookup(schemasDirRNSPath, RNSPathQueryFlags.DONT_CARE);
		if (!schemasDirRNS.exists())
		{
			schemasDirRNS.mkdir();
		}
		return schemasDirRNS;
	}
	
	static public RNSPath createResultsDir(RNSPath path) throws RNSException
	{
		String resultsDirRNSPath = path.getParent().pwd() + "SQLQueries-results";
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
