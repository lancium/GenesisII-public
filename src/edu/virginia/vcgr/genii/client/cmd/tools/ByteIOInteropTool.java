package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.*;

import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedInt;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rp_2.*;
import org.oasis_open.docs.wsrf.r_2.*;
import org.ggf.schemas.byteio._2006._07.interop.*;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.RandomByteIOInteropPortType;
import org.ggf.sbyteio.StreamableByteIOInteropPortType;
import java.rmi.RemoteException; 
import org.ggf.rbyteio.*;
import org.ggf.sbyteio.*;
import org.ggf.byteio.*;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class ByteIOInteropTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Runs WSRF byteIO interop tests.";
	static final private String _USAGE =
		"byteio " +
		"[--Repr-url=<Repr-url>] [--Repr-file=<Repr-file>]" +
		"[--Sepr-url=<Sepr-url>] [--Sepr-file=<Sepr-file>]" +
		"[--all]  [--t41] [--t42] [--t43] [--t44] [--t45] [--t46] " +
		"[--t51] [--t52] [--t53] " +
		"[--allExtra] [--queryR] [--queryS] [--multiR] [--multiS]";
	
	protected static final byte[] TEST4_5 = new byte[] {
		(byte)'+',(byte)'+',(byte)'+',
		(byte)'+',(byte)'+',(byte)'+'};
	
	protected static final byte[] TEST4_6 = new byte[] {
		(byte)'+',(byte)'+',(byte)'+',
		(byte)'+',(byte)'+',(byte)'+',
		(byte)'+',(byte)'+',(byte)'+',(byte)'+'};
	
	protected static final byte[] TEST4_7 = new byte[] {
		(byte)'?',(byte)'?',(byte)'?',
		(byte)'?',(byte)'?',(byte)'?',
		(byte)'+',(byte)'+',(byte)'+',
		(byte)'+',(byte)'+',(byte)'+'};

	private RandomByteIOInteropPortType factoryR;
	private RandomByteIOInteropPortType rBio;
	private EndpointReferenceType rBioEpr;
	
	private StreamableByteIOInteropPortType factoryS;
	private StreamableByteIOInteropPortType sBio;
	private EndpointReferenceType sBioEpr;
	
	private Boolean _all = false;
	
	//RByteIO Tests
	private Boolean _t41 = false;
	private Boolean _t42 = false;
	private Boolean _t43 = false;
	private Boolean _t44 = false;
	private Boolean _t45 = false;
	private Boolean _t46 = false;
	private Boolean _t47 = false;
	private Boolean _t48 = false;
	private Boolean _t49 = false;
	
	//SByteIO Tests
	private Boolean _t51 = false;
	private Boolean _t52 = false;
	private Boolean _t53 = false;
	
	//removed tests
	private Boolean _allExtra = false;
	private Boolean _queryR = false;
	private Boolean _queryS = false;
	private Boolean _multiR = false;
	private Boolean _multiS = false;
	
	//EPR specification
	private String _ReprFile = null;
	private String _ReprURL = null;
	private String _SeprFile = null;
	private String _SeprURL = null;
	
	public ByteIOInteropTool() {
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setAll(){
		_all = true;
	}
	
	public void setT41(){
		_t41 = true;
	}
	
	public void setT42(){
		_t42 = true;
	}
	
	public void setT43(){
		_t43 = true;
	}
	
	public void setT44(){
		_t44 = true;
	}
	
	public void setT45(){
		_t45 = true;
	}
	
	public void setT46(){
		_t46 = true;
	}
	
	public void setT47(){
		_t47 = true;
	}
	
	public void setT48(){
		_t48 = true;
	}
	
	public void setT49(){
		_t49 = true;
	}
	
	public void setT51(){
		_t51 = true;
	}
	
	public void setT52(){
		_t52 = true;
	}
	
	public void setT53(){
		_t53 = true;
	}
	
	public void setAllExtra(){
		_allExtra = true;
	}
	public void setQueryR(){
		_queryR = true;
	}
	
	public void setQueryS(){
		_queryS = true;
	}
	
	public void setMultiR(){
		_multiR = true;
	}
	
	public void setMultiS(){
		_multiS = true;
	}

	public void setRepr_file(String ReprFile)
	{
		_ReprFile = ReprFile;
	}
	
	public void setRepr_url(String ReprURL)
	{
		_ReprURL = ReprURL;
	}
	
	public void setSepr_file(String SeprFile)
	{
		_SeprFile = SeprFile;
	}
	
	public void setSepr_url(String SeprURL)
	{
		_SeprURL = SeprURL;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if(_all){
			_t41 = true;
			_t42 = true;
			_t43 = true;
			_t44 = true;
			_t45 = true;
			_t46 = true;
			_t47 = true;
			_t48 = true;
			_t49 = true;
						
			_t51 = true;
			_t52 = true;
			_t53 = true;	
		}
		
		if(_allExtra){
			_queryR = true;
			_queryS = true;
			_multiR = true;
			_multiS = true;
		}
			
		
		//Run tests
		if(_t41){
			createRByteIO(); //setup world
			Test4_1();  //run test
			destoryRByteIO();  //destroy world
		}
		if(_t42){
			createRByteIO(); //setup world
			Test4_Read(20,12,1,0,"4.2");  //run test
			destoryRByteIO();  //destroy world
		}
		if(_t43){
			createRByteIO(); //setup world
			Test4_Read(2,2,5,4,"4.3");  //run test
			destoryRByteIO();  //destroy world
		}
		if(_t44){
			createRByteIO(); //setup world
			Test4_Read(0,12,2,6,"4.4");  //run test
			destoryRByteIO();  //destroy world
		}
		if(_t45){
			createRByteIO(); //setup world
			Test4_Write(20, 6, 0, TEST4_5, "4.5"); //run test
			Test4_Read(0,60,1,0,"4.5-Check");  //run check
			destoryRByteIO();  //destroy world
		}
		if(_t46){
			createRByteIO(); //setup world
			Test4_Write(22, 2, 4, TEST4_6, "4.6");  //run test
			Test4_Read(0,60,1,0,"4.6-Check");  //run check
			destoryRByteIO();  //destroy world
		}
		if(_t47){
			createRByteIO(); //setup world
			Test4_Write(0, 6, 3, TEST4_7, "4.7");  //run test
			Test4_Read(0,60,1,0,"4.6-Check");  //run check
			destoryRByteIO();  //destroy world
		}
		if(_t48){
			createRByteIO(); //setup world
			Test4_Append(TEST4_5, "4.8");  //run test
			Test4_Read(0,66,1,0,"4.8-Check");  //run check
			destoryRByteIO();  //destroy world
		}
		if(_t49){
			createRByteIO(); //setup world
			Test4_TruncAppend(TEST4_5, "4.9");  //run test
			Test4_Read(0,36,1,0,"4.9-Check");  //run check
			destoryRByteIO();  //destroy world
		}
		if(_t51){
			createSByteIO();  //Setup world
			Test5_1();  //run test
			destorySByteIO();  //destroy world
		}
		if(_t52){
			createSByteIO();  //Setup world
			Test5_SeekRead(20,ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI,
					new UnsignedInt(12),"5.2");  //run test
			destorySByteIO();  //destroy world
		}
		if(_t53){
			createSByteIO();  //Setup world
			Test5_SeekWrite(20,ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI,
					TEST4_5,"5.3"); //run test
			Test5_SeekRead(0,ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI,
					new UnsignedInt(60),"5.3-Check");  //run check
			destorySByteIO();  //destroy world
		}
		
		//extra tests
		if(_queryR){
			createRByteIO(); //setup world
			TestQuery(true, "/*/rbyteio:ModificationTime");//run test
			destoryRByteIO();  //destroy world
		}
		if(_queryS){
			createSByteIO();  //Setup world
			TestQuery(false, "/*/sbyteio:Writeable");  //run test
			destorySByteIO();  //destroy world
		}
		if(_multiR){
			createRByteIO(); //setup world
			TestMultiR();  //run test
			destoryRByteIO();  //destroy world
		}
		if(_multiS){
			createSByteIO();  //Setup world
			TestMultiS();  //run test
			destorySByteIO();  //destroy world
		}
		
		stdout.println("***Testing completed***");
		
		return 0;
	}
	
	/**
	 * Create RByteIO via interop factory
	 * @throws Throwable
	 */

	protected void createRByteIO()
		throws Throwable
	{
		//get epr for factory from command line info
		EndpointReferenceType factoryEpr = null;

		//epr file was specified
		if (_ReprFile != null){
			FileInputStream fin = null;
			try{
				fin = new FileInputStream(_ReprFile);
				factoryEpr = (EndpointReferenceType)ObjectDeserializer.deserialize(
					new InputSource(fin), EndpointReferenceType.class);
			}
			finally{
				StreamUtils.close(fin);
			}
		}
		//url was specified
		else{
			String epr;
			if (_ReprURL != null)
				epr = _ReprURL;
			//by default assume localhost running on port 18080
			else {
				epr = "http://localhost:18080/axis/services/RandomByteIOInteropPortType";
				stdout.println("Using defaults for service epr.");
			}
			
			//create epr to specified rbyteio interop factory
			factoryEpr = EPRUtils.makeEPR(epr, false);
		}
		
		// create a factory proxy to the factory epr for rbyteio 
		factoryR = ClientUtils.createProxy(
						RandomByteIOInteropPortType.class,
						factoryEpr);
		
        // create a instance of interop rbyteio
		CreateResourceResponse createResponse = 
			factoryR.createResource(new CreateResource());
		rBioEpr = createResponse.getEndpointReference();
		
		// create a rbyteio proxy to the new rbyteio instance's epr
		rBio = ClientUtils.createProxy(
					RandomByteIOInteropPortType.class,
					rBioEpr);
		stdout.println("Successfully created rbyteio.");
		
		//create check
		//Test4_Read(0,60,1,0,"Initial-Read");
		
		return;
	}
	
	/**
	 * Create SByteIO via interop factory
	 * @throws Throwable
	 */
	
	protected void createSByteIO() 
		throws Throwable
	{
		//get epr for factory from command line info
		EndpointReferenceType factoryEpr = null;

		//epr file was specified
		if (_SeprFile != null){
			FileInputStream fin = null;
			try{
				fin = new FileInputStream(_SeprFile);
				factoryEpr = (EndpointReferenceType)ObjectDeserializer.deserialize(
					new InputSource(fin), EndpointReferenceType.class);
			}
			finally{
				StreamUtils.close(fin);
			}
		}
		//url was specified
		else{
			String epr;
			if (_SeprURL != null)
				epr = _SeprURL;
			//by default assume localhost running on port 18080
			else {
				epr = "http://localhost:18080/axis/services/StreamableByteIOInteropPortType";
				stdout.println("Using defaults for service epr.");
			}
			
			//create epr to specified sbyteio interop factory
			factoryEpr = EPRUtils.makeEPR(epr, false);
		}
		
		//create a factory proxy to the factory epr for sbyteio
		factoryS = 
			ClientUtils.createProxy(
					StreamableByteIOInteropPortType.class,
					factoryEpr);
		
		//create a instance of interop sbyteio
		CreateResourceResponse createResponseS = 
			factoryS.createResource(new CreateResource());
		sBioEpr = createResponseS.getEndpointReference();
		
		//create a sbyteio proxy to the new sbyteio instance's epr
		sBio = 
			ClientUtils.createProxy(
					StreamableByteIOInteropPortType.class,
					sBioEpr);
		
		stdout.println("Successfully created sbyteio.");
		
		//create check
		//Test5_SeekRead(0,ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI,
		//		new UnsignedInt(60),"Initial-Read");
		return;
	}
	
	/**
	 * Destroy RByteIO via interop factory
	 * @throws Throwable
	 */
	protected void destoryRByteIO() 
		throws Throwable
	{
		try{
			factoryR.deleteResource(new DeleteResource(rBioEpr));
		}
		catch(Exception e){
			stdout.println("Problems with rbyteio deletion: " + e.toString());
			return;
		}
		stdout.println("Successfully deleted rbytio.");

		//Test4_Read(0,60,1,0,"Destroy-Check");
		return;
	}
	
	/**
	 * Destroy SByteIO via interop factory
	 * @throws Throwable
	 */
	
	protected void destorySByteIO() 
		throws Throwable
	{
		//delete a instance of interop sbyteio
		try{
			factoryS.deleteResource(new DeleteResource(sBioEpr));
		}
		catch(Exception e){
			stdout.println("Problems with sbyteio deletion: " + e.toString());
			return;
		}
		stdout.println("Successfully deleted sbyteio.");

		//Test5_SeekRead(0,ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI,new UnsignedInt(60),"Destroy-Check");
		return;
	}
	
	/***
	 * Test 4.1 - Get SIZE resource property for RByteIO 	
	 * @return
	 * @throws RemoteException
	 * @throws InvalidResourcePropertyQNameFaultType
	 * @throws ResourceUnavailableFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test4_1() 
		throws RemoteException, InvalidResourcePropertyQNameFaultType,
		ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		//perform operation
		GetResourcePropertyResponse rpResponse;
		try{
			rpResponse = rBio.getResourceProperty(
							new QName(
								"http://schemas.ggf.org/byteio/2005/10/random-access", 
								"Size"));
		}
		catch(IOException ioe){
			stdout.println("Test 4.1 operation failed: " + ioe.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(rpResponse.get_any(), true, null, false, false, "4.1"));
	}
	
	/**
	 * Get multiple properties of RByteio 
	 * Removed from interop fiesta
	 * 
	 * @return
	 * @throws RemoteException
	 * @throws InvalidResourcePropertyQNameFaultType
	 * @throws ResourceUnavailableFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean TestMultiR()
		throws RemoteException,	InvalidResourcePropertyQNameFaultType, 
		ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		//get multiple properties
		GetMultipleResourcePropertiesResponse rmpResponse;
		try{
			rmpResponse = 
					rBio.getMultipleResourceProperties(new QName[]{
						new QName(
							"http://schemas.ggf.org/byteio/2005/10/random-access", 
							"Readable"),
						new QName(
							"http://schemas.ggf.org/byteio/2005/10/random-access", 
							"Writeable"),
						new QName(
							"http://schemas.ggf.org/byteio/2005/10/random-access", 
							"TransferMechanism")});
		}
		catch(IOException ioe){
			stdout.println("Test of GetMultipleResourceProperties_R failed: " 
					+ ioe.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(rmpResponse.get_any(), true, 
				null, false, false, "of GetMultipleResourceProperties_R"));
	}
	
	
	/**
	 * Tests 4.2-4.4 RByteIO Read 
	 * 4.2 Consecutive Chunk
	 * 4.3 NonOverlapping NonConsecutive Chunks
	 * 4.4 Ovrlapping NonConsecutive Chunks
	 * 
	 * @param startOffset
	 * @param bytesPerBlock
	 * @param numBlock
	 * @param stride
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws ReadNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test4_Read(
			int startOffset, int bytesPerBlock, int numBlock,
			int stride, String testNum)
	throws RemoteException, CustomFaultType, 
	ReadNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		//create read request
		Read readReq = new Read(
				startOffset,bytesPerBlock,numBlock,stride,
		           new TransferInformationType(null,
							ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		
		//perform read
		ReadResponse readResp;
		try{
			readResp = rBio.read(readReq);
		}
		catch(ResourceUnknownFaultType r){
			stdout.println("Test "+testNum+" operation failed " +
					"ResourceUnknownFaultType" + r.toString());
			return false;
		}
		catch(IOException ioe){
			stdout.println("Test "+testNum+" operation failed: " + ioe.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(null, false, 
					readResp.getTransferInformation(), true, true, testNum));
	}
	
	/**
	 * Test 4.5-4.7 RByteIO Write 
	 * 4.5 Consecutive Chunk
	 * 4.6 NonOverlapping NonConsecutive Chunks
	 * 4.7 Ovrlapping NonConsecutive Chunks
	 * 
	 * @param startOffset
	 * @param bytesPerBlock
	 * @param stride
	 * @param data
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws WriteNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test4_Write(
			int startOffset, int bytesPerBlock, int stride, 
			byte[] data, String testNum)
			throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		//create write request
		Write writeReq = new Write(
				startOffset,bytesPerBlock,stride,
				new TransferInformationType(
						new MessageElement[] { new MessageElement(
			     					ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data)},
						ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		
		//perform write operation
		WriteResponse writeResp;
		try{
			writeResp = rBio.write(writeReq);
		}
		catch(Exception e){
			stdout.println("Test "+testNum+" operation failed: " + e.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(null, false, 
				writeResp.getTransferInformation(),true, false, "Test"+testNum));
	}
	
	/** Test 4.8 RByteIO Append 
	 * Append chunk to end of resource
	 * @param data
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws WriteNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test4_Append(byte[] data, String testNum)
			throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		//create append request
		Append appReq = new Append(new TransferInformationType(
	     		   new MessageElement[] { new MessageElement(
	     					ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data)},
	     		   ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		
		//perform append operation
		AppendResponse appResp;
		try{
			appResp = rBio.append(appReq);
		}
		catch(Exception e){
			stdout.println("Test 4."+testNum+" operation failed: " + e.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(null, false, 
				appResp.getTransferInformation(),true, false, "Test"+testNum));
	}
	
	/** Test 4.9 RByteIO TruncAppend 
	 * Append before end of resource
	 * 
	 * @param data
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws WriteNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test4_TruncAppend(byte[] data, String testNum)
			throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		//create trunc append request
		TruncAppend tappReq = new TruncAppend(30, new TransferInformationType(
					new MessageElement[] { new MessageElement(
	     					ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data)},
					ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		
		//perform trunc append operation
		TruncAppendResponse tappResp;
		try{
			tappResp = rBio.truncAppend(tappReq);
		}
		catch(Exception e){
			stdout.println("Test 4."+testNum+" operation failed: " + e.toString());
			return false;
		}
		
		//process result and return true if all as expected
		return(processResponse(null, false, 
				tappResp.getTransferInformation(),true, false, "Test"+testNum));
	}
	
	
	/***
	 * Test 5.1 - Get READABLE resource property for SByteIO 	
	 * @return
	 * @throws RemoteException
	 * @throws InvalidResourcePropertyQNameFaultType
	 * @throws ResourceUnavailableFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test5_1() 
		throws RemoteException, InvalidResourcePropertyQNameFaultType,
		ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		//get resrouce property
		GetResourcePropertyResponse rpResponseS;
		try{
			rpResponseS = sBio.getResourceProperty(
							new QName(
								"http://schemas.ggf.org/byteio/2005/10/streamable-access", 
								"Readable"));
		}
		catch(IOException ioe){
			stdout.println("Test 5.1 operation failed: " + ioe.toString());
			return false;
		}

		MessageElement []elementsRPS = rpResponseS.get_any();
		if (elementsRPS != null && elementsRPS.length >= 1) {
			stdout.println("Test 5.1 succeeded:");
			stdout.println("\tGetResourceProperty:" + elementsRPS[0]);
			return true;
		}
		else{
			stdout.println("Test 5.1 failed: invalid response");
			return false;
		}
	}
	
	
	/**
	 * Test 2.2 - Get multiple properties of SByteio 
	 * @return
	 * @throws RemoteException
	 * @throws InvalidResourcePropertyQNameFaultType
	 * @throws ResourceUnavailableFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean TestMultiS()
		throws RemoteException,	InvalidResourcePropertyQNameFaultType, 
		ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		//get multiple resource properties
		GetMultipleResourcePropertiesResponse rmpResponseS;
		try{
			rmpResponseS = 
					sBio.getMultipleResourceProperties(new QName[] {
						new QName(
								"http://schemas.ggf.org/byteio/2005/10/streamable-access", 
								"Seekable"),
						new QName(
								"http://schemas.ggf.org/byteio/2005/10/streamable-access", 
								"TransferMechanism"),
						new QName(
								"http://schemas.ggf.org/byteio/2005/10/streamable-access", 
								"EndOfStream")});	
		}
		catch(IOException ioe){
			stdout.println("Test of GetMultipleResourceProperties_S operation failed: "
					+ ioe.toString());
			return false;
		}

		//process result and return true if all as expected
		return(processResponse(rmpResponseS.get_any(), true, 
				null, false, false, "of GetMultipleResourceProperties_S"));
	}
	
	/**
	 * Query (R or S) ByteIO
	 * Test removed from interop fiesta
	 * 
	 * @param isRByteIO - true if RByteIO, false if SByteIO
	 * @param myQuery - the query
	 * @return
	 * @throws RemoteException
	 * @throws InvalidResourcePropertyQNameFaultType
	 * @throws InvalidQueryExpressionFaultType
	 * @throws QueryEvaluationErrorFaultType
	 * @throws ResourceUnavailableFaultType
	 * @throws ResourceUnknownFaultType
	 * @throws UnknownQueryExpressionDialectFaultType
	 */
	protected Boolean TestQuery(Boolean isRByteIO, String myQuery) 
		throws RemoteException, InvalidResourcePropertyQNameFaultType, 
			InvalidQueryExpressionFaultType, QueryEvaluationErrorFaultType, 
			ResourceUnavailableFaultType, ResourceUnknownFaultType, 
			UnknownQueryExpressionDialectFaultType
	{
		QueryResourcePropertiesResponse qrpResponse;
		try {
			//create query
			QueryExpressionType exp =  new QueryExpressionType(
					new MessageElement [] { new  MessageElement(
							new Text(myQuery))},
					new URI("http://www.w3.org/TR/1999/REC-xpath-19991116"));
			QueryResourceProperties qReq = new QueryResourceProperties(exp);
			
			//perform query
			if (isRByteIO)
				qrpResponse = rBio.queryResourceProperties(qReq);
			else
				qrpResponse = sBio.queryResourceProperties(qReq);
		}
		catch(IOException ioe){
			stdout.println("Test of Query operation failed: " + ioe.toString());
			return false;
		}

		//process result and return if all as expected
		return(processResponse(qrpResponse.get_any(), true, 
				null, false, false, "of Query"));
	}
	
	/**Test 5.2 RByteIO SeekRead 
	 * 5.2 Consecutive Chunk
	 * 
	 * @param startOffset
	 * @param seekOrigin
	 * @param numBytes
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws ReadNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test5_SeekRead(
			long startOffset, URI seekOrigin, 
			UnsignedInt numBytes, String testNum)
	throws RemoteException, CustomFaultType, ReadNotPermittedFaultType,
	SeekNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		//create seek read request
		SeekRead seekReadReq = new SeekRead(
				startOffset,seekOrigin,numBytes,
		        new TransferInformationType(null,
							ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		//perform seek read
		SeekReadResponse seekReadResp;
		try{
			seekReadResp = sBio.seekRead(seekReadReq);
		}
		catch(IOException ioe){
			stdout.println("Test "+testNum+" operation failed: " + ioe.toString());
			return false;
		}
		
		//process result and return if all as expected
		return(processResponse(null, false, 
				seekReadResp.getTransferInformation(), true, true, testNum));
	}
	

	/**
	 * Test 5.3 RByteIO SeekWrite 
	 * Consecutive Chunk
	 * 
	 * @param startOffset
	 * @param bytesPerBlock
	 * @param stride
	 * @param data
	 * @param testNum
	 * @return
	 * @throws RemoteException
	 * @throws CustomFaultType
	 * @throws WriteNotPermittedFaultType
	 * @throws UnsupportedTransferFaultType
	 * @throws ResourceUnknownFaultType
	 */
	protected Boolean Test5_SeekWrite(
			long startOffset, URI seekOrigin, byte[] data, String testNum)
			throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		//create seek write request
		SeekWrite seekWriteReq = new SeekWrite(
				startOffset, seekOrigin, 
				new TransferInformationType(
			     		   new MessageElement[] { 
			     				new MessageElement(
			     					ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data)},
			     		   ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		
		//perform seek write
		SeekWriteResponse seekWriteResp;
		try{
			seekWriteResp = sBio.seekWrite(seekWriteReq);
		}
		catch(Exception e){
			stdout.println("Test "+testNum+" operation failed: " + e.toString());
			return false;
		}
		
		//process result and return if all as expected
		return(processResponse(null, false, 
				seekWriteResp.getTransferInformation(),true, false, testNum));
	}
	
	
	/**
	 * Processes resonse message from ByteIO operations
	 *  
	 * @param message - MessageElement reponse
	 * @param processMessage - 
	 *   true to output elements in MessageElement reponse
	 * @param transferInfo - TransferInfromationType reponse
	 * @param processTransfer -
	 * 	 true to output transfer mechanism from TransferInfromationType reponse
	 * @param anyShouldExist -
	 *   true to output _any (ie data) from TransferInfromationType reponse
	 * @param testNum - number of test for output printing (ie "4.3")
	 * @return
	 */
	protected Boolean processResponse(
			MessageElement []message, Boolean processMessage,
			TransferInformationType transferInfo, Boolean processTransfer,
			Boolean anyShouldExist, String testNum)
	{
		//check that response contains info
		if (((processTransfer) && (transferInfo == null)) || 
			((processMessage) && (message == null))){
			stdout.println("Test "+testNum+" failed: response info null");
			return false;
		}
		
		//process transfer information response
		if (processTransfer){
			MessageElement []transferAny = transferInfo.get_any();
			
			//check for _any info (ie <data>) in transfer info
			if (transferAny == null){
				if (anyShouldExist){
					stdout.println("Test "+testNum+" failed: _any contents null");
					return false;
				}
			}
			else{
				if (!anyShouldExist){
				stdout.println("Test "+testNum+" failed: _any contents not null");
				return false;
				}
			}
			
			//proceed to output info
			String anyString, transferMech;
			try{
				stdout.println("Test "+testNum+" succeeded:");				
				if (anyShouldExist){
					anyString = new String((byte[])transferAny[0].getValueAsType(new QName(
							"http://www.w3.org/2001/XMLSchema", "base64Binary")));
					stdout.println("\t_All as String:" + anyString);
					stdout.println("\t_All Encoded:" + transferAny[0].toString());
				}
				transferMech = transferInfo.getTransferMechanism().toString();
				stdout.println("\tTransfer Mechanism:" + transferMech);
				return true;
			}
			catch (Exception e){
				stdout.println("Test "+testNum+" failed: " + e.toString());
				return false;
			}
		}
		//process MessageElement response
		else {
			stdout.println("Test "+testNum+" succeeded:");
			for(MessageElement out : message){
				stdout.println("\tResponse" + out);
			}
			return true;
		}
	}
		
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 0) {
			stdout.println("Args:"+numArguments());
			stdout.println("No parameters specifed");
			throw new InvalidToolUsageException();
		}
	}
}



























