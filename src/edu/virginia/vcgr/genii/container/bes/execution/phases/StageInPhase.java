package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.morgan.util.io.DataTransferStatistics;

import edu.virginia.vcgr.genii.algorithm.compression.UnpackTar;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class StageInPhase extends AbstractExecutionPhase implements Serializable
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(StageInPhase.class);

	static private final String STAGING_IN_STATE = "staging-in";

	private URI _source;
	private File _target;
	private CreationFlagEnumeration _creationFlag;
	private UsernamePasswordIdentity _usernamePassword;
	private boolean _handleAsArchive;

	public StageInPhase(URI source, File target, CreationFlagEnumeration creationFlag, boolean handleAsArchive,
		UsernamePasswordIdentity usernamePassword)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, STAGING_IN_STATE, false));

		_usernamePassword = usernamePassword;

		if (source == null)
			throw new IllegalArgumentException("Parameter \"source\" cannot be null.");

		if (target == null)
			throw new IllegalArgumentException("Parameter \"targetName\" cannot be null.");

		_source = source;
		_target = target;
		_creationFlag = (creationFlag == null) ? CreationFlagEnumeration.overwrite : creationFlag;
		_handleAsArchive = handleAsArchive;
	}

	// hmmm: move this elsewhere.
	/**
	 * the types of compressed files that we know how to handle.
	 */
	public enum CompressedFileTypes {
		UNKNOWN,
		TAR_FILE,
		TAR_GZ_FILE,
		TGZ_FILE,
		ZIP_FILE,
	};

	/**
	 * packages up the work done by isRecognizedArchiveType, which provides the type that was recognized and the suffix length for the
	 * extension of that type.
	 */
	static public class ArchiveDetails
	{
		public CompressedFileTypes _type = CompressedFileTypes.UNKNOWN; // from constructor.
		public Integer _suffixLength = -1; // from constructor.
		public String _baseName;// calculated in isRecognizedArchiveType.
		File _folder; // calculated in isRecognizedArchiveType.

		ArchiveDetails(CompressedFileTypes type, Integer suffixLength)
		{
			_type = type;
			_suffixLength = suffixLength;
		}

		@Override
		public String toString()
		{
			return "type=" + _type + " suffix length=" + _suffixLength + " basename='" + _baseName + "' folder='" + _folder + "'";
		}

		/**
		 * returns a non-null archive details object if the "testFile" ends in a known archive extension (zip, tar, tgz, tar.gz).
		 */
		public static ArchiveDetails isRecognizedArchiveType(File testFile)
		{
			ArchiveDetails toReturn = null;

			if (testFile.getPath().endsWith(".zip")) {
				if (_logger.isDebugEnabled())
					_logger.debug("Handling zip file: " + testFile);
				toReturn = new ArchiveDetails(CompressedFileTypes.ZIP_FILE, 4);
			} else if (testFile.getPath().endsWith(".tar")) {
				if (_logger.isDebugEnabled())
					_logger.debug("Handling tar file: " + testFile);
				toReturn = new ArchiveDetails(CompressedFileTypes.TAR_FILE, 4);
			} else if (testFile.getPath().endsWith(".tgz")) {
				if (_logger.isDebugEnabled())
					_logger.debug("Handling tgz file: " + testFile);
				toReturn = new ArchiveDetails(CompressedFileTypes.TGZ_FILE, 4);
			} else if (testFile.getPath().endsWith(".tar.gz")) {
				toReturn = new ArchiveDetails(CompressedFileTypes.TAR_GZ_FILE, 7);
			}

			if (toReturn == null) {
				if (_logger.isDebugEnabled())
					_logger.debug("could not recognize file type as archive: " + testFile);
				return toReturn;
			}

			if (_logger.isDebugEnabled())
				_logger.debug("Handling file type " + toReturn._type);

			/*
			 * now we know some characteristics of the file involved, so let's break up the name and figure out what directory to create from
			 * the archive.
			 */
			if (_logger.isDebugEnabled())
				_logger.debug("before calculation, target path is '" + testFile.getPath());
			String justFilename = new File(testFile.getPath()).getName();
			if (_logger.isDebugEnabled())
				_logger.debug("just the filename of target path is '" + justFilename + "'");
			toReturn._baseName = justFilename.substring(0, justFilename.length() - toReturn._suffixLength);
			if (_logger.isDebugEnabled())
				_logger.debug("basename of archive file calculated as '" + toReturn._baseName + "'");
			File tempTarg = new File(testFile.getPath());
			toReturn._folder = new File(tempTarg.getParent() + "/" + toReturn._baseName);
			if (_logger.isDebugEnabled())
				_logger.debug("folder of archive would be '" + toReturn._folder + "'");

			if (_logger.isDebugEnabled())
				_logger.debug("intuited that '" + testFile + "' had: " + toReturn.toString());

			return toReturn;
		}

	}

	/**
	 * attempts to treat the _target as an archive file and uncompress it.
	 */
	public void uncompressArchive(HistoryContext history) throws JSDLException, IOException
	{
		/*
		 * The target path is, for example, xxx/yyy/fred.zip. I want to create a directory xxx/yyy/fred. First I extract fred.zip, then create
		 * the dir fred.
		 * 
		 * Initially, we need to determine what the file type is (based on extension), and then we can construct the proper directory name.
		 */
		// String baseName = "Unknown";
		if (_logger.isDebugEnabled()) {
			File parent = _target.getParentFile();
			_logger.debug("Parent dir of target is '" + parent + "'");
		}

		ArchiveDetails detail = ArchiveDetails.isRecognizedArchiveType(_target);
		if (detail == null) {
			// this is a failure; we know we needed to figure out the structure of the name before here.
			throw new JSDLException(
				"failure to determine type of archive (due to HandleAsArchive flag) for file '" + _target.getAbsolutePath() + "'");
		}

		File compressedFile = new File(_target.getPath());

		/*
		 * An abiding concern here is that we don't uncompress the archive twice in scratch directories. Thus all of these will blow out an
		 * IOException if the expected target directory is already present and somewhat in the way.
		 */
		switch (detail._type) {
			case TAR_FILE:
				UnpackTar.uncompressTar(compressedFile, detail._folder, true);
				break;
			case ZIP_FILE:
				UnpackTar.uncompressZip(compressedFile, detail._folder, true);
				break;
			case TAR_GZ_FILE: // intentional fall-through to TGZ.
			case TGZ_FILE:
				UnpackTar.uncompressTarGZ(compressedFile, detail._folder, true);
				break;
			default:
				throw new JSDLException("unanticipated file type when uncompressing for HandleAsArchive");
		}

		history.createTraceWriter(detail._baseName + " archive decompressed to " + detail._folder);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageIn);

		history.createInfoWriter("Staging in to %s", _target.getName()).format("Staging in from %s to %s.", _source, _target).close();
		DataTransferStatistics stats;

		try {
			if (_creationFlag.equals(CreationFlagEnumeration.dontOverwrite)) {
				DownloadManagerContainerService service = ContainerServices.findService(DownloadManagerContainerService.class);
				stats = service.download(_source, _target, _usernamePassword);
			} else {
				stats = URIManager.get(_source, _target, _usernamePassword);
			}
			history.createTraceWriter("%s: %d Bytes Transferred", _target.getName(), stats.bytesTransferred())
				.format("%d bytes were transferred in %d ms.", stats.bytesTransferred(), stats.transferTime()).close();
			// The file has been transfered. Now we check if it needs to be handled as an archive.
			try {
				if (_handleAsArchive) {
					/*
					 * they told us to handle this file as an archive. it will be decompressed now. this should not be done twice, if the
					 * folder already exists.
					 */
					uncompressArchive(history);
				}
			} catch (IOException ie) {
				// This is caught when it was already unzipped.
				if ((ie.getMessage() != null) && (ie.getMessage().indexOf("already exists") < 0)) {
					throw ie;
				}
				String msg = "Stage input already extracted";
				history.createTraceWriter(msg);
				System.err.println(msg);
			}

		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Error staging in to %s", _target.getName())
				.format("Error staging in from %s to %s.", _source, _target).close();
			throw cause;
		}
	}
}