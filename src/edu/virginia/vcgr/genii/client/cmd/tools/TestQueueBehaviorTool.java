package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.GetActivityStatusesResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.IdentityAttribute;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class TestQueueBehaviorTool extends BaseGridTool {

	static private final String _DESCRIPTION2 = 
		"A tool that runs jobs in a way similar to the way that the queue does.";
	static private final String _USAGE = "test-queue-behavior <bes-path> <jsdl> <iters>";

	private Blob _storedCallingContext;
	private Blob _storedIdentities;

	public TestQueueBehaviorTool()
	{
		super(_DESCRIPTION2, _USAGE, false);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		String sBesPath = getArgument(0);
		String sJsdlPath = getArgument(1);
		int iters = Integer.parseInt(getArgument(2));
		
		EndpointReferenceType besEPR = RNSPath.getCurrent().lookup(
			sBesPath, RNSPathQueryFlags.MUST_EXIST).getEndpoint();
		JobDefinition_Type jsdl = readJSDL(sJsdlPath);
		
		storeInformation();
		
		for (int lcv = 0; lcv < iters; lcv++)
		{
			stdout.format("[%d] Creating activity on container.\n", lcv);
			GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, besEPR,
				loadInformation());
			EndpointReferenceType job = bes.createActivity(
				new CreateActivityType(
					new ActivityDocumentType(jsdl, null), null)).getActivityIdentifier();
			while (true)
			{
				bes = ClientUtils.createProxy(GeniiBESPortType.class, besEPR,
					loadInformation());
				GetActivityStatusesResponseType resp =
					bes.getActivityStatuses(new GetActivityStatusesType(
						new EndpointReferenceType[] { job }, null));;
				ActivityState state = new ActivityState(
					resp.getResponse(0).getActivityStatus());
				stdout.format("[%d] %s\n", lcv, state);
				if (state.isFinalState())
					break;
			}
			
			stdout.format("[%d] Garbage collecting the job.\n", lcv);
			bes = ClientUtils.createProxy(GeniiBESPortType.class, besEPR,
				loadInformation());
			bes.terminateActivities(new TerminateActivitiesType(new EndpointReferenceType[] { job }, null) );
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
	}
	
	private JobDefinition_Type readJSDL(String jsdlPath)
		throws IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(jsdlPath);
			JobDefinition_Type jobDef = 
				(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobDefinition_Type.class);
			return jobDef;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	private void storeInformation()
		throws SQLException, IOException, ConfigurationException
	{
		ICallingContext callingContext = ContextManager.getCurrentContext();
		Collection<Identity> identities = getCallerIdentities();
		
		_storedCallingContext = DBSerializer.toBlob(callingContext);
		_storedIdentities = DBSerializer.toBlob(identities);
	}
	
	private ICallingContext loadInformation()
		throws SQLException, IOException, ClassNotFoundException
	{
		DBSerializer.fromBlob(_storedIdentities);
		return (ICallingContext)DBSerializer.fromBlob(_storedCallingContext);
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Identity> getCallerIdentities()
		throws AuthZSecurityException
	{
		try
		{
			Collection<Identity> ret = new ArrayList<Identity>();
			
			/* Retrieve the current calling context */
			ICallingContext callingContext = 
				ContextManager.getCurrentContext(false);
			
			if (callingContext == null)
				throw new AuthZSecurityException(
					"Error processing GAML credential: No calling context");
			
			/* The caller's identities are kept in the "transient" credentials 
			 * space for the calling context.
			 */
			ArrayList<GamlCredential> callerCredentials = (ArrayList<GamlCredential>)
				callingContext.getTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY);
			for (GamlCredential cred : callerCredentials) 
			{
				/* If the cred is an Identity, then we simply add that idendity
				 * to our identity list.
				 */
				if (cred instanceof Identity) 
				{
					ret.add((Identity)cred);
				} else if (cred instanceof SignedAssertion) 
				{
					/* If the cred is a signed assertion, then we have to
					 * get the identity out of the assertion.
					 */
					SignedAssertion signedAssertion = (SignedAssertion)cred;
					
					// if its an identity assertion, check it against our ACLs
					if (signedAssertion.getAttribute() 
						instanceof IdentityAttribute) 
					{
						IdentityAttribute identityAttr = 
							(IdentityAttribute) signedAssertion.getAttribute();
	
						ret.add(identityAttr.getIdentity());
					}
				}
			}
			
			return ret;
		}
		catch (ConfigurationException ce)
		{
			throw new AuthZSecurityException("Unable to load current context.",
				ce);
		}
		catch (IOException ioe)
		{
			throw new AuthZSecurityException("Unable to load current context.", 
				ioe);
		}
	}
}