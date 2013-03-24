package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ActivityStatusType;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.Option;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class QSlotManagerTool extends BaseGridTool
{
	// Will need to write up man page and usage when we finish, not for prototype
	final static private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dqslotmgr";
	final static private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uqslotmgr";
	final static private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/qslotmgr";

	private static final String[] WAITING_JOB_STATUSES = { "Pending", "Queued", "Held", "Zombie" };
	private static final String _SLOTS = "Slots:  ";
	private static final String _BES_DIR = "resource-management";
	private static final String _QUEUE_SLOT_FILE_DIR = "/etc/queue-slots";

	private String _QUEUE_MAX_FILE = null;
	private boolean RUN_ONCE = false;
	private int THRESHOLD_TIME = 60; // in minutes [1 hr]
	private int TIMEOUT = 24; // in hours [24 hr]
	private int POLLING_PERIOD = 10; // in minutes [10 min]

	@Option({ "maxfile" })
	public void setMaxFile(String file_name)
	{
		_QUEUE_MAX_FILE = file_name;
	}

	@Option({ "polling_period" })
	public void setPollingPeriod(String polling_period)
	{
		POLLING_PERIOD = Integer.parseInt(polling_period);
	}

	@Option({ "runtime" })
	public void setRuntime(String runtime)
	{
		TIMEOUT = Integer.parseInt(runtime);
	}

	@Option({ "threshold" })
	public void setThresholdtime(String thresholdTime)
	{
		THRESHOLD_TIME = Integer.parseInt(thresholdTime);
	}

	@Option({ "runonce" })
	public void setRunOnce()
	{
		RUN_ONCE = true;
	}

	/*
	 * We could use the JobInformation data types from the stat call to track waiting jobs, but that
	 * timing information may not always be available, so we keep a list in memory for estimating
	 * the wait time when we can't get it exactly.
	 */
	private class JobTimer
	{
		public JobTimer(int runtime, boolean seen, String bes)
		{
			this.runtime = runtime;
			this.seen = seen;
			this.bes = bes;
		}

		public int runtime;
		public boolean seen;
		public String bes;
	}

	private HashMap<JobTicket, JobTimer> watchList;

	/*
	 * Each BES has some data associated with it that we need as we modify the slot counts
	 * throughout execution. This keeps that data in one place and easily accessible via the BES
	 * name.
	 */
	private class BESStats
	{
		public BESStats(Integer current, Integer max)
		{
			this.current = current;
			this.max = max;
		}

		// Slots
		public int current;
		public int max;
		public boolean reduced;

		// Jobs
		public int scheduled;
		public int waiting;
	}

	private HashMap<String, BESStats> statTable;

	// Standard grid Tool constructor
	public QSlotManagerTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}

	// This is essentially your main() method, it loops for some time period
	// periodically checking the jobs on a Queue and adjusting as needed
	@Override
	protected int runCommand() throws Throwable
	{
		// timeout is the time to run the tool in hours, default is 24 hours
		// runtime is the amount of time in minutes the program has been running so far
		int runtime = 0;

		String queuePath = getArgument(0);
		QueueManipulator manipulator = new QueueManipulator(queuePath);

		if (_QUEUE_MAX_FILE == null)
			setMaxFile(_QUEUE_SLOT_FILE_DIR + queuePath);

		populateSlotTable(queuePath);

		watchList = new HashMap<JobTicket, JobTimer>();
		if (RUN_ONCE) {
			recycleSlots(manipulator);
		} else {
			while (runtime < TIMEOUT * 60) { // runtime (minutes) < TIMEOUT (hours)
				stdout.println("Starting next loop...");
				recycleSlots(manipulator);
				Thread.sleep(POLLING_PERIOD * 60 * 1000); // POLLING_PERIOD minutes
				runtime += POLLING_PERIOD;
			}
		}
		return 0;
	}

	/*
	 * Based on the code Ashwin wrote to find BES Slot counts; there are two sets of slot counts:
	 * current and max. Current is whatever the queue has at the moment, max is the highest we're
	 * allowed to set current. The max counts should be stored in a centralized location on the GFFS
	 * so our tool can access it wherever it runs; by convention, we will store them in
	 * /etc/queue-slots/<queue-path> (e.g. "/etc/queue-slots/queues/grid-queue"). The user may
	 * specify a different file path on the command line.
	 */
	private void populateSlotTable(String queuePath) throws IOException, RNSException
	{
		statTable = new HashMap<String, BESStats>();

		// Get the path to the queue's resource directory
		String resourcePath = queuePath;
		if (queuePath.charAt(queuePath.length() - 1) != '/')
			resourcePath += "/";
		resourcePath += _BES_DIR;
		GeniiPath gPath = new GeniiPath(resourcePath);

		// make sure the queue exists
		if (!gPath.exists() || gPath.pathType() != GeniiPathType.Grid)
			throw new ResourceException("Path does not exist or does not reference a Queue.");

		// Convert to RNSPath for listing contents
		ICallingContext ctxt = ContextManager.getExistingContext();
		RNSPath targetResource = ctxt.getCurrentPath().expandSingleton(gPath.path());

		// get the contents of the resource directory
		Collection<RNSPath> entries = targetResource.listContents();

		// get the slot counts for each BES
		if (entries.size() > 0) {
			for (RNSPath entry : entries) {
				Integer currentSlots = getCurrentBESSlots(entry);
				if (currentSlots != null) {
					statTable.put(entry.getName(), new BESStats(currentSlots, -1));
				}
			}
			populateMaxBESSlots();
		}
	}

	/*
	 * This should read a file (which already exists on GFFS) to determine the max slot counts for
	 * the BESes on the target queue. This file will either be in a fixed location for each queue,
	 * or specified by the user at run time.
	 */
	private void populateMaxBESSlots() throws IOException, RNSException
	{
		GeniiPath qPath = new GeniiPath(_QUEUE_MAX_FILE);
		if (!qPath.exists()) {
			throw new ResourceException("No file for max slot counts at " + _QUEUE_MAX_FILE);
		} else {
			InputStream inStream = null;
			try {
				inStream = qPath.openInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
				String line;
				while ((line = in.readLine()) != null) {
					if (line.length() == 0)
						continue;
					int separator = line.indexOf(":");
					if (separator < 0)
						throw new IOException("Input file " + qPath + ": Wrong format");

					// break up the line
					String BES = line.substring(0, separator);
					int slotCount = Integer.parseInt(line.substring(separator + 1));

					// set the max for this BES
					statTable.get(BES).max = slotCount;
				}
			} finally {
				StreamUtils.close(inStream);
			}
		}
	}

	// Based on the code Ashwin wrote to find BES Slot counts
	private Integer getCurrentBESSlots(RNSPath entry) throws IOException
	{
		InputStream in = null;
		Integer slotCount = null;

		try {
			String line;

			// open a reader for the pseudo-file for this bes
			GeniiPath path = new GeniiPath(entry.pwd());
			in = path.openInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// read each line in the file
			while ((line = br.readLine()) != null) {
				// if it says "Slots"
				if (line.contains(_SLOTS)) {
					// get the boundaries of the slot count
					int start = line.indexOf(_SLOTS) + _SLOTS.length();
					// get the slot count
					slotCount = Integer.parseInt(line.substring(start));
					// print it along with the BES name
					stdout.println(entry.getName() + " " + slotCount);
				}
			}
		} finally {
			StreamUtils.close(in);
		}
		return slotCount;
	}

	// This is the body of the main loop, separated out in case we use --runonce
	private void recycleSlots(QueueManipulator manipulator) throws Throwable
	{
		clearSlotStats();
		rescheduleJobs(manipulator);
		incrementSlots(manipulator);
		cleanupWatchlist(manipulator);
	}

	/*
	 * Each iteration we refresh this data, so we need to reset it at the beginning of the main loop
	 */
	private void clearSlotStats()
	{
		for (BESStats bes : statTable.values()) {
			bes.scheduled = 0;
			bes.waiting = 0;
			bes.reduced = false;
		}
	}

	/*
	 * This function is the primary reschedule logic. It goes through the jobs from the queue,
	 * determines the state of each job, and decides if it should be rescheduled. When a job is
	 * chosen for reschedule, the BES that is currently hosting that job loses slots, and the job is
	 * re-queued without penalty.
	 */
	private void rescheduleJobs(QueueManipulator manipulator) throws RemoteException
	{

		Iterator<JobInformation> stats = manipulator.status(null);
		boolean needReschedule = false;

		HashMap<JobTicket, String> rescheduleJobs = new HashMap<JobTicket, String>();

		while (stats.hasNext()) {
			JobInformation stat = stats.next();

			if (!stat.getJobState().equals(QueueStates.RUNNING))
				continue;

			// record that this job is on the BES
			BESStats besStats = statTable.get(stat.getScheduledOn());
			besStats.scheduled++;

			// get the jobs ticket number and BES-level status
			ActivityStatusType besStatus = stat.besActivityStatus();
			JobTicket ticket = stat.getTicket();
			if (besStatus == null) {
				stdout.print("Job with ticket " + ticket);
				stdout.println(" is labeled running, but has no BES status");
				continue;
			}

			if (isWaitingJob(besStatus)) {
				JobTimer timer = null;
				besStats.waiting++;

				// If it's not in the watchlist, it's a new waiting job
				if (!watchList.containsKey(ticket)) {
					// try to get an initial wait time, else set the time to 0
					timer = new JobTimer(getWaitingTime(stat), true, stat.getScheduledOn());
					watchList.put(ticket, timer);
					stdout.println("   New waiting job: " + ticket.toString());
				} else {
					// If it's already in the list, it has been waiting
					timer = watchList.get(ticket);

					// try to get the actual wait time from the bes (in seconds)
					int waitingTime = getWaitingTime(stat);
					if (waitingTime == 0) {
						// but if we can't, then just estimate
						timer.runtime += POLLING_PERIOD;
					} else {
						timer.runtime = waitingTime / 60; // runtime is in minutes
					}
				}

				if (timer.runtime >= THRESHOLD_TIME) {
					rescheduleJobs.put(ticket, timer.bes);
					stdout.println("   Wait time on job " + ticket.toString() + " exceeded threshold");
					needReschedule = true;
				} else {
					timer.seen = true;
				}
			}
		}

		if (needReschedule) {
			String[] tickets = new String[rescheduleJobs.size()];
			int ii = 0;
			for (JobTicket ticket : rescheduleJobs.keySet()) {
				tickets[ii] = ticket.toString();
				watchList.remove(ticket);
				ii++;

				String bes = rescheduleJobs.get(ticket);
				BESStats besStats = statTable.get(bes);
				besStats.reduced = true;
				besStats.scheduled--;
				besStats.waiting--;
				stdout.println("   Rescheduling " + ticket.toString());
			}

			for (String bes : statTable.keySet()) {
				BESStats besStats = statTable.get(bes);
				if (besStats.reduced) {
					// Only make one call to change slots for each BES
					manipulator.configure(bes, besStats.scheduled);
					besStats.current = besStats.scheduled;
				}
			}

			// Reschedule all the jobs at the same time
			manipulator.rescheduleJobs(tickets);
		}
	}

	/*
	 * This is the method YAN designed to read in a BES-level job status Returns true if the job is
	 * Waiting on a BES somewhere
	 */
	private boolean isWaitingJob(ActivityStatusType status)
	{
		if (status.getState() == ActivityStateEnumeration.Pending)
			return true;
		MessageElement[] metadata = status.get_any();
		if (metadata != null) {
			for (MessageElement element : metadata) {
				final QName qName = element.getQName();
				// Only works on G2 BES
				if (qName.getNamespaceURI().equals(ActivityState._GENII_NS)) {
					String fineGrainedStatus = qName.getLocalPart();
					for (String waitingStatus : WAITING_JOB_STATUSES) {
						if (waitingStatus.equalsIgnoreCase(fineGrainedStatus))
							return true;
					}
				}
			}
		}
		return false;
	}

	// returns the on-BES waiting time of a job
	private int getWaitingTime(JobInformation jobInformation)
	{
		Calendar submitTimeInBES = jobInformation.getStartTime();
		if (submitTimeInBES == null)
			return 0;
		long waitingTimeInMillis = System.currentTimeMillis() - submitTimeInBES.getTimeInMillis();
		long waitingTimeInSeconds = waitingTimeInMillis / 1000;
		return (int) waitingTimeInSeconds;
	}

	/*
	 * We should increase the slot count if: a) all the current slots are full, and b) all the
	 * current jobs are running (not waiting), and c) the current slot count is less than the
	 * maximum We can either increase by one at a time, or we can go straight to the max; currently
	 * it's one at a time.
	 */
	private void incrementSlots(QueueManipulator manipulator) throws RemoteException
	{
		for (String key : statTable.keySet()) {
			BESStats bes = statTable.get(key);
			stdout.println("   BES " + key + ": using " + bes.current + " of " + bes.max);
			stdout.print("      " + bes.scheduled + " scheduled jobs, ");
			stdout.println(bes.waiting + " waiting");

			if (bes.reduced) {
				stdout.println("      this BES slot count was reduced this time");
			} else if (bes.current < bes.max && bes.scheduled >= bes.current && bes.waiting == 0) {
				int newSlots = bes.current + 1;
				manipulator.configure(key, newSlots);
				bes.current = newSlots;
				stdout.println("      increasing slot count to " + newSlots);
			}
		}
	}

	// Just a simple function to remove jobs that aren't waiting anymore
	private void cleanupWatchlist(QueueManipulator manipulator)
	{
		for (JobTicket j : watchList.keySet()) {
			JobTimer t = watchList.get(j);
			if (t.seen) {
				t.seen = false;
			} else {
				watchList.remove(j);
			}
		}
	}

	// Checks for the right number of arguments from the command line
	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException("Must supply a queue path.\n");
	}
}
