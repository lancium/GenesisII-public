package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.DataTransferStatistics;

import edu.virginia.vcgr.genii.algorithm.compression.PackTar;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageInPhase.ArchiveDetails;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class StageOutPhase extends AbstractExecutionPhase implements Serializable
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(StageOutPhase.class);

	static private final String STAGING_OUT_STATE = "staging-out";

	private URI _target;
	private File _source;
	private UsernamePasswordIdentity _usernamePassword;
	private boolean _handleAsArchive;

	public StageOutPhase(File source, URI target, boolean handleAsArchive, UsernamePasswordIdentity usernamePassword)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, STAGING_OUT_STATE, false));
		_handleAsArchive = handleAsArchive;

		_usernamePassword = usernamePassword;

		if (source == null)
			throw new IllegalArgumentException("Parameter \"sourceName\" cannot be null.");

		if (target == null)
			throw new IllegalArgumentException("Parameter \"target\" cannot be null.");

		_source = source;
		_target = target;
	}

	/**
	 * attempts to treat the _source as an archive file and compress it.
	 * 
	 * basic scheme is that we are taking a directory and compressing it into an archive file.
	 * 
	 * the _source member will be a filename with the appropriate archive ending. this file will not exist yet.
	 * 
	 * the source directory name is computed from the archive name (stored in the _source member) by removing the archive suffix.
	 * 
	 * the created archive's contents will only contain relative paths starting _under_ the source directory; the prefix up to and including
	 * the source directory name is stripped off.
	 */
	public void compressArchive(HistoryContext history) throws JSDLException, IOException
	{
		if (_logger.isDebugEnabled()) {
			File parent = _source.getParentFile();
			_logger.debug("Parent dir of source is '" + parent + "'");
		}

		ArchiveDetails detail = StageInPhase.ArchiveDetails.isRecognizedArchiveType(_source);
		if (detail == null) {
			// this is a failure; we know we needed to figure out the structure of the name before here.
			throw new JSDLException(
				"failure to determine type of archive (due to HandleAsArchive flag) for file '" + _source.getAbsolutePath() + "'");
		}

		_logger.debug("into compressArchive with source path '" + _source + "' and eventual target '" + _target + "'");

		File compressedFile = new File(_source.getPath());

		switch (detail._type) {
			case TAR_FILE:
				PackTar.compressTar(compressedFile, detail._folder);
				break;
			case ZIP_FILE:
				PackTar.compressZip(compressedFile, detail._folder);
				break;
			case TAR_GZ_FILE: // intentional fall-through to TGZ.
			case TGZ_FILE:
				PackTar.compressTarGZ(compressedFile, detail._folder);
				break;
			default:
				throw new JSDLException("unanticipated file type when compressing for HandleAsArchive");
		}

		history.createTraceWriter(detail._baseName + " archive compressed from source " + detail._folder);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageOut);

		history.createInfoWriter("Staging %s out.", _source.getName()).format("Staging %s out to %s.", _source, _target).close();

		DataTransferStatistics stats;

		// before we transfer anything, see if they wanted us to handle this as an archive file.
		try {
			if (_handleAsArchive) {
				/*
				 * we're supposed to handle this as an archive, so we try to compress what should be a folder or large file before staging it
				 * out.
				 */
				compressArchive(history);
			}
		} catch (IOException ie) {
			_logger.error("caught IOException when trying to compress archive; source='" + _source + "' target='" + _target + "'", ie);
		}

		if (!_source.exists()) {
			history.createErrorWriter("Can't stage %s out.", _source.getName()).format("Source file (%s) does not seem to exist.", _source)
				.close();

			throw new ContinuableExecutionException(
				"Unable to locate source file \"" + _source.getName() + "\" for staging-out -- skipping it.");
		}

		try {
			stats = URIManager.put(_source, _target, _usernamePassword);
			history.createTraceWriter("%s: %d Bytes Transferred", _source.getName(), stats.bytesTransferred())
				.format("%d bytes were transferred in %d ms.", stats.bytesTransferred(), stats.transferTime()).close();
		} catch (Throwable cause) {
			history.error(cause, "Can't stage %s out.", _source.getName());

			throw new ContinuableExecutionException("A continuable exception has occurred while " + "running a BES activity.", cause);
		}
	}
}