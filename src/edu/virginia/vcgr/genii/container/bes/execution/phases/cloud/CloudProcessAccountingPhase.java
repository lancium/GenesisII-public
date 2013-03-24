package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperException;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class CloudProcessAccountingPhase extends AbstractCloudExecutionPhase implements Serializable
{

	static final long serialVersionUID = 0L;
	private String _activityID;
	private String _besid;
	private String _remoteFile;
	private String _localFile;
	private BESConstructionParameters _constructionParameters;
	private Collection<String> _commandLine;

	static private Log _logger = LogFactory.getLog(CloudProcessAccountingPhase.class);

	public CloudProcessAccountingPhase(String activityID, String besid, String remoteFile, String localFile,
		Collection<String> commandLine, BESConstructionParameters constructionParameters)
	{

		_activityID = activityID;
		_besid = besid;
		_remoteFile = remoteFile;
		_localFile = localFile;
		_constructionParameters = constructionParameters;
		_commandLine = commandLine;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "processing-accounting-data", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID = tManage.aquireResource(_activityID);

		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Default);

		history.createInfoWriter("Processing Accounting Data").close();

		tryRecieveFile(resourceID, _localFile, _remoteFile, tManage);

		File resourceUsageFile = new File(_localFile);
		int exitCode;

		if (resourceUsageFile != null) {
			try {
				ExitResults eResults = ProcessWrapper.readResults(resourceUsageFile);
				exitCode = eResults.exitCode();

				_logger.info("Processing Accounting Data");
				history.createInfoWriter("Job exited with exit code " + exitCode).close();
				AccountingService acctService = ContainerServices.findService(AccountingService.class);
				if (acctService != null) {
					OperatingSystemNames osName = _constructionParameters.getResourceOverrides().operatingSystemName();

					ProcessorArchitecture arch = _constructionParameters.getResourceOverrides().cpuArchitecture();

					acctService.addAccountingRecord(context.getCallingContext(), context.getBESEPI(), arch, osName, null,
						_commandLine, exitCode, eResults.userTime(), eResults.kernelTime(), eResults.wallclockTime(),
						eResults.maximumRSS());
				}

			} catch (ProcessWrapperException pwe) {
				history.warn(pwe, "Error Acquiring Accounting Info");
				throw new IgnoreableFault("Error trying to read resource usage information.", pwe);
			}
		}

		_logger.info("Processed Accounting Data");

	}

	@Override
	protected Log getLog()
	{
		return _logger;
	}

	@Override
	protected String getPhase()
	{
		return "Processing Accounting Data";
	}
}