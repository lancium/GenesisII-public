package edu.virginia.vcgr.genii.security.credentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;

/**
 * tracks the credentials that a client has sent to containers before, on a per container basis. this is the heart of the credential
 * streamlining record keeping for the client side.
 */
public class ClientCredentialTracker
{
	static private Log _logger = LogFactory.getLog(ClientCredentialTracker.class);

	// hmmm: these values below should come from config file!
	
	// how many containers we will track overall for the client.
	public static final int MAX_CONTAINERS_TRACKED = 100;

	// number of credentials we will remember per container. should vaguely suit the container's memory size.
	public static final int MAXIMUM_CREDS_TRACKED_PER_CONTAINER = 350;

	// how long we hope that the container will remember credentials.
	static public long CONTAINER_MEMORY_GUESSTIMATE = 1000 * 60 * 4; // should be shorter than container retention time.

	/**
	 * currently empty but could track more info about a previously used credential.
	 */
	public static class CredRecordStruct
	{}

	/**
	 * maps between trust credential GUIDs and the record for that credential.
	 */
	public static class PreviouslySeenCredentialsList extends TimedOutLRUCache<String, CredRecordStruct>
	{
		public PreviouslySeenCredentialsList()
		{
			super(MAXIMUM_CREDS_TRACKED_PER_CONTAINER, CredentialCache.CRED_CACHE_TIMEOUT_MS);
		}

		public PreviouslySeenCredentialsList(int maxElements, long defaultTimeoutMS)
		{
			super(maxElements, defaultTimeoutMS);
		}
	}

	/**
	 * this object maps a container GUID (as string) to a list of credentials that the container with that guid should have already seen.
	 */
	public static class StreamliningUsageTracker extends TimedOutLRUCache<String, PreviouslySeenCredentialsList>
	{
		public StreamliningUsageTracker(int maxElements, long defaultTimeoutMS)
		{
			super(maxElements, defaultTimeoutMS);
		}
	}

	// tracks the credentials previously seen by containers.
	private static StreamliningUsageTracker _tracker =
		new StreamliningUsageTracker(MAX_CONTAINERS_TRACKED, CredentialCache.CRED_CACHE_TIMEOUT_MS);

	/**
	 * returns true if the tracker remembers the container having seen this credential guid previously.
	 */
	public static boolean hasContainerSeenCred(String containerGUID, String credentialGUID)
	{
		synchronized (_tracker) {
			if (!doesContainerSupportStreamlining(containerGUID)) {
				// boink; we can't help this guy.
				return false;
			}

			// look up the records for the container EPI.
			PreviouslySeenCredentialsList containerList = _tracker.get(containerGUID);
			if (containerList == null) {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("we have never seen container '" + containerGUID + "' before, so it hasn't seen cred " + credentialGUID);
				return false;
			}

			// record that we accessed this container recently.
			_tracker.refresh(containerGUID);

			// look up the cred in that container's list.
			CredRecordStruct record = containerList.get(credentialGUID);
			if (record != null) {
				// future: use record somehow here? currently we only need to know that there was one.
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("container '" + containerGUID + "' saw this cred before: " + credentialGUID);
				// the container had seen this one before, so let them know.
				return true;
			}

			if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
				_logger.debug("container '" + containerGUID + "' has NOT seen this cred before: " + credentialGUID);
			return false;
		}
	}

	/**
	 * records that the container with that guid saw the credential with that GUID. this should only be getting invoked when credential
	 * streamlining is enabled for the client.
	 */
	public static void recordContainerSawCred(String containerGUID, String credentialGUID)
	{
		if (!doesContainerSupportStreamlining(containerGUID)) {
			// nope, this guy is not the remembering credentials type.
			return;
		}

		synchronized (_tracker) {
			// look up the records for the container EPI.
			PreviouslySeenCredentialsList containerList = _tracker.get(containerGUID);
			if (containerList == null) {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("container '" + containerGUID + "' has no records yet; adding one.");
				containerList = new PreviouslySeenCredentialsList();
				_tracker.put(containerGUID, containerList);
			}

			// record that we accessed this container recently.
			_tracker.refresh(containerGUID);

			// add the cred into that container's list.
			CredRecordStruct record = containerList.get(credentialGUID);
			if (record != null) {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("container '" + containerGUID + "' saw this cred before, so just refreshing: " + credentialGUID);
				containerList.refresh(credentialGUID);
				return;
			}

			record = new CredRecordStruct();
			containerList.put(credentialGUID, record);

			if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
				_logger.debug("container '" + containerGUID + "' had NOT seen this cred before, so we added it: " + credentialGUID);
		}
	}

	/**
	 * drops *all* tracking information. only really makes sense when credentials have thoroughly changed.
	 */
	public static void flushEntireTracker()
	{
		synchronized (_tracker) {
			_tracker.clear();
		}
	}

	/**
	 * drops any credential tracking information for the specified container. this could be used to handle a fault during recognition of a
	 * previously sent credential, but it is the big nuke since all of the container's records are removed. the better approach is to remove
	 * the specific faulting credential, once we can tell which one faulted.
	 */
	public static void flushContainerTracker(String containerGUID)
	{
		synchronized (_tracker) {
			// do the removal of all records for that container.
			_tracker.remove(containerGUID);
		}
	}

	/**
	 * removes the specific credential with that GUID from the tracking records for the container, such that we will be forced to send it
	 * again next time it's needed.
	 */
	public static void forgetCredentialForContainer(String containerGUID, String credentialGUID)
	{
		synchronized (_tracker) {
			// look up the records for the container EPI.
			PreviouslySeenCredentialsList containerList = _tracker.get(containerGUID);
			if (containerList == null) {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("container '" + containerGUID + "' has no records yet, so nothing to forget.");
				return;
			}

			// record that we accessed this container recently.
			_tracker.refresh(containerGUID);

			// toss the cred out of that container's list.
			containerList.remove(credentialGUID);
		}
	}

	// hmmm: perhaps separate out this implementation...
	// hmmm: or move the thing into the above code, since that also tracks containers by EPI! just the two states needed.

	// how many container streamlining support states should we keep track of?
	final static int SUPPORTING_CONTAINERS_TRACKED = 200;

	// how long should we remember if a container supports streamlining?
	final static int SUPPORTING_CONTAINER_MEMORY_DURATION = 1000 * 60 * 60; // one hour currently.

	public static class ContainerSupportLevel
	{
		boolean _supportsStreamlining = false;
		boolean _containerSaidSo = false;

		public ContainerSupportLevel()
		{
		}

		/**
		 * records if the container supports streamlining (first parameter) and if the container actually reported that (second parameter).
		 * knowing whether we guessed or whether the container actually said it supported streamlining is important initially before we've
		 * talked to the conatiner.
		 */
		public ContainerSupportLevel(boolean streamliningSupported, boolean containerSaidSo)
		{
			_supportsStreamlining = streamliningSupported;
			_containerSaidSo = containerSaidSo;
		}
	}

	private static TimedOutLRUCache<String, ContainerSupportLevel> _streamliningContainerList =
		new TimedOutLRUCache<String, ContainerSupportLevel>(SUPPORTING_CONTAINERS_TRACKED, SUPPORTING_CONTAINER_MEMORY_DURATION);

	/**
	 * returns true or false based on our memory of whether the container supports credential streamlining or not. the default for an unknown
	 * container is true, because we want to start streamlining right away. an older container will fault, and then we will need to retry
	 * without using streamlining.
	 */
	public static boolean doesContainerSupportStreamlining(String containerGUID)
	{
		synchronized (_streamliningContainerList) {
			ContainerSupportLevel memory = _streamliningContainerList.get(containerGUID);
			if (memory == null) {
				// record the default of true, but we don't say the container told us, since we don't know.
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("had to create a new memory record for container " + containerGUID);
				memory = new ContainerSupportLevel(true, false);
				_streamliningContainerList.put(containerGUID, memory);
			} else {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("found existing memory record for container " + containerGUID);
			}
			return memory._supportsStreamlining;
		}
	}

	/**
	 * returns true if the container itself told us its state, or false if we guessed.
	 */
	public static boolean didContainerActuallyAnswerStreamliningQuestion(String containerGUID)
	{
		synchronized (_streamliningContainerList) {
			ContainerSupportLevel memory = _streamliningContainerList.get(containerGUID);
			if (memory != null)
				return memory._containerSaidSo;
			return false;
		}
	}

	/**
	 * records that the container either supports or doesn't support based on the "doesSupport" parameter.
	 */
	public static void setContainerStreamliningSupport(String containerGUID, boolean doesSupport, boolean containerSaidSo)
	{
		synchronized (_streamliningContainerList) {
			ContainerSupportLevel memory = _streamliningContainerList.get(containerGUID);
			if (memory != null) {
					if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
						_logger.debug("updating existing memory record for container " + containerGUID + " setting <" + doesSupport + ", "
							+ containerSaidSo + ">");
					memory._supportsStreamlining = doesSupport;
					memory._containerSaidSo = containerSaidSo;
			} else {
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("adding new memory record for container " + containerGUID + " setting <" + doesSupport + ", "
						+ containerSaidSo + ">");
				_streamliningContainerList.put(containerGUID, new ContainerSupportLevel(doesSupport, containerSaidSo));
			}
		}
	}
}
