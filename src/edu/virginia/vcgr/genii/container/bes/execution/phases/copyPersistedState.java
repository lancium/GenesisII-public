package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.DataTransferStatistics;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.ws.addressing.EndpointReferenceType;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat; // Apache 2.0 License

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.CopyMachine;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class copyPersistedState extends AbstractExecutionPhase implements Serializable {
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(copyPersistedState.class);

	static private final String COPY_PERSISTED_STATE = "copyPersistedState";
	static private final long CLOCK_SHIFT_MARGIN = 600000; // 10 minutes

	

	private EndpointReferenceType _src=null;

	public copyPersistedState(EndpointReferenceType src) {
		super(new ActivityState(ActivityStateEnumeration.Running, COPY_PERSISTED_STATE));
		// 2020-09-01 ASG. This copies the persisted data of a job from a source EPR to the current container.
		_src=src;
	}

	@Override
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageIn);
		history.createInfoWriter("Entered copy-persisted-state");
		// 2020-09-01 ASG. Copying persisted state. Basically we copy the EPR/JWD and EPR/Accounting directories from whereever
		// they are right now to this BES. Need to check to make sure this BES is not sharing state with the OLD BES ... if they 
		// are using the same state, we do not have to copy. Not clear why we would be doing that though.
		// We also need to consider what to do with history ... I have not taken care of that yet.
		
		// First, verify that we can talk to the endpoint
		if (_src==null) {
			throw new Throwable("CopyPersistentState: EPR of source job is null");
		}
		TypeInformation typeInfo = new TypeInformation(_src);
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _src);
		RNSPath srcDir= new RNSPath(_src);
		if (!srcDir.exists() || !srcDir.isRNS()) {
			throw new Throwable("CopyPersistentState: EPR of job does not exist, or is not an RNS");
		}
		
		RNSPath workDir=srcDir.lookup("working-dir");
		if (!workDir.exists() || !workDir.isRNS()){
			throw new Throwable("CopyPersistentState: working-dir of job does not exist, or is not an RNS");
		}
		
		RNSPath accountingDir=srcDir.lookup("accounting-dir");
		if (!accountingDir.exists() || !accountingDir.isRNS()){
			throw new Throwable("CopyPersistentState: accounting-dir of job does not exist, or is not an RNS");
		}
		
	
		
		// Ok, we have established that the source RNSPaths exist and are directories, and we have created directories in the local
		// file system where we are going to copy the data. Now copy it. Note that I have created a new doStageinPhase that takes 
		// an RNSPath as the source rather than a path to the source, as there is no path. That change required adding a new 
		// "download" function that takes the source as an RNS path.

		PathOutcome result;
		// First copy the job working directory
		CopyMachine mimeo = new CopyMachine(workDir.getEndpoint(), activity.getActivityCWD().getWorkingDirectory().getAbsolutePath());		
		result=mimeo.copyTree();
		if (result != PathOutcome.OUTCOME_SUCCESS) {
			throw new Throwable("CopyPersistentState: problem copying the job working directory, " + PathOutcome.outcomeText(result));
		}
		// Now copy the accounting directory tree
		mimeo = new CopyMachine(accountingDir.getEndpoint(), activity.getActivityCWD().getWorkingDirectory().getAbsolutePath());		
		result=mimeo.copyTree();
		if (result != PathOutcome.OUTCOME_SUCCESS) {
			throw new Throwable("CopyPersistentState: problem copying the accounting directory, " + PathOutcome.outcomeText(result));
		}

		// Note: we have not handled the case where we fail. Not sure what to do.
		
	
	}
	
}
