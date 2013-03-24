package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.morgan.util.io.DataTransferStatistics;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class StageInPhase extends AbstractExecutionPhase implements Serializable
{
	static final long serialVersionUID = 0L;

	static private final String STAGING_IN_STATE = "staging-in";

	private URI _source;
	private File _target;
	private CreationFlagEnumeration _creationFlag;
	private UsernamePasswordIdentity _credential;

	public StageInPhase(URI source, File target, CreationFlagEnumeration creationFlag, UsernamePasswordIdentity credential)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, STAGING_IN_STATE, false));

		_credential = credential;

		if (source == null)
			throw new IllegalArgumentException("Parameter \"source\" cannot be null.");

		if (target == null)
			throw new IllegalArgumentException("Parameter \"targetName\" cannot be null.");

		_source = source;
		_target = target;
		_creationFlag = (creationFlag == null) ? CreationFlagEnumeration.overwrite : creationFlag;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageIn);

		history.createInfoWriter("Staging in to %s", _target.getName()).format("Staging in from %s to %s.", _source, _target)
			.close();
		DataTransferStatistics stats;

		try {
			File target = _target;
			if (_creationFlag.equals(CreationFlagEnumeration.dontOverwrite)) {
				DownloadManagerContainerService service = ContainerServices.findService(DownloadManagerContainerService.class);
				stats = service.download(_source, target, _credential);
			} else
				stats = URIManager.get(_source, target, _credential);

			history.createTraceWriter("%s: %d Bytes Transferred", _target.getName(), stats.bytesTransferred())
				.format("%d bytes were transferred in %d ms.", stats.bytesTransferred(), stats.transferTime()).close();

		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Error staging in to %s", _target.getName())
				.format("Error staging in from %s to %s.", _source, _target).close();
			throw cause;
		}
	}
}