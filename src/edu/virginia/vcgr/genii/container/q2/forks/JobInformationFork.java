package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.cs.vcgr.genii.job_management.JobInformationType;
import edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType;
import edu.virginia.cs.vcgr.genii.job_management.ReducedJobInformationType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.queue.QueueUtils;
import edu.virginia.vcgr.genii.client.queue.ReducedJobInformation;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.AdvertisedSize;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.StreamableFactoryConfiguration;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@StreamableFactoryConfiguration(notifyOnDestroy = false)
@AdvertisedSize
public class JobInformationFork extends AbstractStreamableByteIOFactoryResourceFork
{
	static private final String _FORMAT = "%1$-36s   %2$tH:%2$tM %2$tZ %2$td %2$tb %2$tY   %3$-4d   %4$s";

	static private Pattern JOB_FORK_PATH_PATTERN = Pattern
		.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");

	static public String determineJobTicketFromForkPath(String forkPath) throws RemoteException
	{
		Matcher matcher = JOB_FORK_PATH_PATTERN.matcher(forkPath);
		if (!matcher.find())
			throw new RemoteException(String.format("Can't find job ticket in fork path \"%s\"!", forkPath));

		return matcher.group();
	}

	static private void printJobInfo(PrintStream out, JobInformation jobInfo)
	{
		out.println(String.format("%1$-36s   %2$-21s   %3$-4s   %4$-8s", "Ticket", "Submit Time", "Tries", "State"));

		String stateString = jobInfo.getScheduledOn();
		if (stateString != null)
			stateString = String.format("On %s", stateString);
		else
			stateString = String.format("%s", jobInfo.getJobState());

		TimeZone tz = TimeZone.getDefault();
		Calendar submitTime = jobInfo.getSubmitTime();
		submitTime.setTimeZone(tz);

		out.println(String.format(_FORMAT, jobInfo.getTicket(), submitTime, jobInfo.getFailedAttempts(), stateString));
	}

	static private void printJobInfo(PrintStream out, ReducedJobInformation jobInfo)
	{
		if (jobInfo instanceof JobInformation) {
			printJobInfo(out, (JobInformation) jobInfo);
			return;
		}

		out.printf("%1$-36s   %2$-8s   %3$-30s\n", "Ticket", "State", "Owner Identities");

		out.printf("%1$-36s   %2$-8s", jobInfo.getTicket(), jobInfo.getJobState());
		Collection<Identity> owners = jobInfo.getOwners();
		if (owners.size() <= 0)
			out.printf("\n");
		else {
			boolean first = true;
			for (Identity identity : owners) {
				if (first) {
					first = false;
					out.printf("   %1$-30s\n", identity);
				} else {
					out.printf("%1$-50s%2$-30s\n", "", identity);
				}
			}
		}
	}

	public JobInformationFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public void destroy() throws ResourceException
	{
		super.destroy();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the state of a job.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		boolean isMine = false;
		String jobTicket = determineJobTicketFromForkPath(getForkPath());

		ReducedJobInformation jInfo = null;
		ResourceKey rKey = getService().getResourceKey();

		try {
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());

			if (getForkPath().startsWith("/jobs/mine")) {
				isMine = true;
				for (JobInformationType jit : mgr.getJobStatus(new String[] { jobTicket })) {
					JobTicket ticket = new JobTicket(jit.getJobTicket());
					byte[][] owners = jit.getOwner();
					Collection<Identity> identities;
					try {
						identities = QueueUtils.deserializeIdentities(owners);
					} catch (Exception e) {
						throw new RuntimeException("Unable to deserialize owner identities.", e);
					}

					jInfo = new JobInformation(ticket, jit.getJobName(), identities, QueueStates.fromQueueStateType(jit
						.getJobStatus()), (int) jit.getPriority(), jit.getSubmitTime(), jit.getStartTime(),
						jit.getFinishTime(), jit.getAttempts().intValue(), jit.getBesStatus(), jit.getScheduledOn());
				}
			} else {
				for (ReducedJobInformationType rjit : mgr.listJobs(jobTicket)) {
					if (rjit.getJobTicket().equals(jobTicket)) {
						JobTicket ticket = new JobTicket(rjit.getJobTicket());
						JobStateEnumerationType state = rjit.getJobStatus();
						byte[][] ownerBytes = rjit.getOwner();
						Collection<Identity> identities;
						try {
							identities = QueueUtils.deserializeIdentities(ownerBytes);
						} catch (Exception e) {
							throw new RuntimeException("Unable to deserialize owner identities.", e);
						}

						jInfo = new ReducedJobInformation(ticket, identities, QueueStates.fromQueueStateType(state));
					}
				}
			}

			PrintStream pStream = new PrintStream(sink);
			printJobInfo(pStream, jInfo);

			if (isMine) {
				pStream.println();
				pStream.println("JSDL:");
				JobDefinition_Type jsdl = null;
				OutputStreamWriter writer = null;

				try {
					jsdl = mgr.getJSDL(jobTicket);
					if (jsdl != null) {
						writer = new OutputStreamWriter(pStream);
						ObjectSerializer.serialize(writer, jsdl, new QName(GenesisIIConstants.JSDL_NS, "JobDefinition"));
					} else {
						pStream.println("Can't find JSDL for job.");
					}
				} catch (Throwable cause) {
					pStream.println("Can't find JSDL for job.");
				} finally {
					if (writer != null)
						writer.flush();
				}
			}

			pStream.flush();
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to stat job in queue.", sqe);
		}
	}
}
