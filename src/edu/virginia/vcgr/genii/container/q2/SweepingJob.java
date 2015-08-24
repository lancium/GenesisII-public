package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.feistymeow.process.ethread;
import org.morgan.util.io.StreamUtils;

import edu.virginia.cs.vcgr.genii.job_management.SubmitJobRequestType;
import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.logging.LoggingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.SweepListener;
import edu.virginia.vcgr.jsdl.sweep.SweepToken;
import edu.virginia.vcgr.jsdl.sweep.SweepUtility;

/**
 * an in-queue job that just submits other jobs, based on a jsdl file that has one or more variable sweeps.
 */
public class SweepingJob extends ethread
{
	static private Log _logger = LogFactory.getLog(SweepingJob.class);

	JobDefinition _jobDefinition;
	SubmitJobRequestType _submitJobRequest;
	SweepToken _token;
	SweepListenerImpl _listener;
	QueueManager _queueMgr;
	HistoryContext _history;
	Long _jobID; // given to us once the job is set up.
	Long _subJobCount = 0L;
	String _ownTicket;

	public SweepingJob(JobDefinition jobDefinition, SubmitJobRequestType submitJobRequest, QueueManager queueMgr)
		throws FileNotFoundException, SweepException, IOException
	{
		_jobDefinition = jobDefinition;
		_submitJobRequest = submitJobRequest;
		_queueMgr = queueMgr;
	}

	/**
	 * recreates a sweeping job from records in the database.
	 */
	public SweepingJob(String ownTicket)
	{
		// future: this function is by no means complete if we want to actually resume a sweeping job.

		_ownTicket = ownTicket;
		_logger.warn("check if this is right ticket id, deserialized from db: " + _ownTicket);
	}

	public boolean createSweep() throws FileNotFoundException, SweepException, IOException
	{
		if (_token != null) {
			_logger.error("failed to start sweep since it appears to have already been started.");
			return false;
		}
		// start the sweep. this should happen here since the queue call's working context is still available.
		_token = SweepUtility.performSweep(_jobDefinition, _listener = new SweepListenerImpl(_queueMgr, _submitJobRequest.getPriority()));

		return true;
	}

	/**
	 * used to associate the sweeping job object with its queued job, once the queued job exists.
	 */
	public void setJobId(Long jobId)
	{
		_jobID = jobId;
	}

	/**
	 * set once the ticket for this job's own identity within the queue is known.
	 */
	public void setOwnTicket(String ownTicket)
	{
		_ownTicket = ownTicket;
	}

	@Override
	public boolean performActivity()
	{
		if (_token == null) {
			_logger.error("thread for sweeping job was started before sweep was created.  failing out of sweep thread.");
			return false;
		}

		try {
			_logger.debug("pre-join on sweep.");
			_token.join();
			_logger.debug("post-join on sweep.");
		} catch (Exception e) {
			_logger.error("sweep token join caught an exception", e);
		}

		try {
			_logger.debug("marking sweep job as finished: " + _jobID);
			_queueMgr.getJobManager().finishJob(_jobID);
		} catch (Exception e) {
			_logger.error("caught an exception when attempting to finish sweep", e);
		}

		// if we got to here, we're done with the sweep whether it worked or not.
		return false;
	}

	public HistoryContext getHistory()
	{
		return _history;
	}

	public String getEncodedSweepState()
	{
		if (_token == null) {
			return "unstarted-sweep";
		}
		// a very simple synopsis of the sweep state which doesn't account for any variable values.
		return String.format("iter-%d", _subJobCount);
	}

	/**
	 * returns the list of job tickets that have already been submitted from the sweep. these are the child jobs or sub-jobs of the sweep.
	 */
	public String[] getTicketList()
	{
		if (_listener != null) {
			return (String[]) _listener.getTicketList().toArray();
		}

		return new String[0];
	}

	public void setHistory(HistoryContext history)
	{
		_history = history;
	}

	/**
	 * the real worker that submits the sub-jobs from the parameter sweep. this iteratively substitutes the next values for the sweep into the
	 * jsdl and kicks off a new job. the underlying mechanism is actually a recursive invocation of the sweeping process, but this function is
	 * hit in a simple manner that allows us to track the number of sub-jobs here.
	 */
	private class SweepListenerImpl implements SweepListener
	{
		private WorkingContext _workingContext;
		private ICallingContext _callingContext;
		private QueueManager _queueManager;
		private Collection<String> _tickets;
		private short _priority;
		private LoggingContext _context;

		private SweepListenerImpl(QueueManager queueManager, short priority) throws FileNotFoundException, IOException
		{
			_queueManager = queueManager;
			_tickets = new LinkedList<String>();
			_priority = priority;
			_callingContext = ContextManager.getExistingContext();
			_workingContext = (WorkingContext) WorkingContext.getCurrentWorkingContext().clone();
			try {
				_context = (LoggingContext) LoggingContext.getCurrentLoggingContext().clone();
			} catch (ContextException e) {
				_context = new LoggingContext();
			}
		}

		/**
		 * returns the list of tickets that have already been submitted as sub-jobs from the sweep.
		 */
		public Collection<String> getTicketList()
		{
			return _tickets;
		}

		@Override
		public void emitSweepInstance(JobDefinition jobDefinition) throws SweepException
		{
			Closeable assumedContextToken = null;

			try {
				LoggingContext.assumeLoggingContext(_context);
				WorkingContext.setCurrentWorkingContext(_workingContext);
				assumedContextToken = ContextManager.temporarilyAssumeContext(_callingContext);
				synchronized (_tickets) {

					/*
					 * future: sweeping job also needs to update its state regularly! maybe not for every single job, but certainly either
					 * periodically or for each Nth job. anyway, here is a good place to update the state, since we are acting on the state!
					 * unfortunately, the record we keep is in the calling context, so we'd be doing way too much work if we reloaded modified
					 * and stored the context back every time. instead, we should do it like every 10th job or something, and definitely at
					 * the completion of the sweep.
					 */

					_subJobCount++; // we're hitting a new job.
					// we make an identifier using the guid of the sweeper with our sub-job counter attached.
					String ticken = _ownTicket + String.format("%d", _subJobCount);
					// drop the job into the queue using our pre-arranged ticket id.
					_queueManager.submitJob(_priority, JSDLUtils.convert(jobDefinition), ticken);
					// write a history record that records that we put this new job into play.
					_history.createInfoWriter("Adding Job [" + ticken + "]").format("Adding parameter-sweep-based job [%s]", ticken).close();
					_tickets.add(ticken);
					_tickets.notifyAll();
				}
				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Submitted job %d from a parameter sweep.", _subJobCount));
			} catch (JAXBException je) {
				throw new SweepException("Unable to convert JAXB type to Axis type.", je);
			} catch (ResourceException e) {
				throw new SweepException("Unable to submit job.", e);
			} catch (SQLException e) {
				throw new SweepException("Unable to submit job.", e);
			} catch (IOException e) {
				throw new SweepException("Unable to submit job.", e);
			} finally {
				StreamUtils.close(assumedContextToken);
				WorkingContext.setCurrentWorkingContext(null);
			}
		}
	}

}
