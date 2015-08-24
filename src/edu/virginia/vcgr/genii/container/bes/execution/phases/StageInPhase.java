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

	public enum CompressedFileTypes {
		UNKNOWN,
		TAR_FILE,
		TAR_GZ_FILE,
		TGZ_FILE,
		ZIP_FILE,
	};

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
			/*
			 * The file has been transfered. Now we check if it is a zip, tar, or gz. If so we need to extract the directory name to put it
			 * in, and check if the directory already exists. If the unzip/untar/ungz target directory already exists DO NOT UNPACK IT
			 * AGAIN!!!!
			 */
			try {
				String baseName = "Unknown";
				File parent = _target.getParentFile();
				File folder;
				int suffixLength = 0;
				CompressedFileTypes type = CompressedFileTypes.UNKNOWN;

				if (_logger.isDebugEnabled())
					_logger.debug("Parent is '" + parent + "'");

				/*
				 * if stage-in says handle as archive
				 */
				if (_handleAsArchive) {
					/*
					 * The target path is, for example, xxx/yyy/fred.zip. I want to create a directory xxx/yyy/fred. First I extract fred.zip,
					 * then create the dir fred.
					 * 
					 * Initially, we need to determine what the file type is (based on extension), and then we can construct the proper
					 * directory name.
					 */
					if (_target.getPath().endsWith(".zip")) {
						if (_logger.isDebugEnabled())
							_logger.debug("Handling zip file: " + _target);
						type = CompressedFileTypes.ZIP_FILE;
						suffixLength = 4;
					} else if (_target.getPath().endsWith(".tar")) {
						if (_logger.isDebugEnabled())
							_logger.debug("Handling tar file: " + _target);
						type = CompressedFileTypes.TAR_FILE;
						suffixLength = 4;
					} else if (_target.getPath().endsWith(".tgz")) {
						if (_logger.isDebugEnabled())
							_logger.debug("Handling tgz file: " + _target);
						type = CompressedFileTypes.TGZ_FILE;
						suffixLength = 4;
					} else if (_target.getPath().endsWith(".tar.gz")) {
						if (_logger.isDebugEnabled())
							_logger.debug("Handling tar.gz file: " + _target);
						type = CompressedFileTypes.TAR_GZ_FILE;
						suffixLength = 7;
					}

					if (suffixLength <= 0) {
						// this is a failure; we know we needed to figure out the structure of the name before here.
						throw new JSDLException("failure to determine type of archive for HandleAsArchive flag");
					}

					/*
					 * now we know some characteristics of the file involved, so let's break up the name and figure out what directory to
					 * create from the archive.
					 */
					if (_logger.isDebugEnabled())
						_logger.debug("before calculation, target path is '" + _target.getPath());
					String justFilename = new File(_target.getPath()).getName();
					if (_logger.isDebugEnabled())
						_logger.debug("just the filename of target path is '" + justFilename + "'");
					baseName = justFilename.substring(0, justFilename.length() - suffixLength);
					if (_logger.isDebugEnabled())
						_logger.debug("basename of archive file calculated as '" + baseName + "'");
					File tempTarg = new File(_target.getPath());
					folder = new File(tempTarg.getParent() + "/" + baseName);
					if (_logger.isDebugEnabled())
						_logger.debug("will stage archive to path '" + folder + "'");

					File compressedFile = new File(_target.getPath());

					/*
					 * An abiding concern here is that we don't uncompress the archive twice in scratch directories. Thus all of these will
					 * blow out an IOException if the expected target directory is already present and somewhat in the way.
					 */
					switch (type) {
						case TAR_FILE:
							UnpackTar.uncompressTar(compressedFile, folder);
							break;
						case ZIP_FILE:
							UnpackTar.uncompressZip(compressedFile, folder);
							break;
						case TAR_GZ_FILE: // intentional fall-through to TGZ.
						case TGZ_FILE:
							UnpackTar.uncompressTarGZ(compressedFile, folder);
							break;
						default:
							throw new JSDLException("unanticipated switch for HandleAsArchive; unknown file type");
					}

					history.createTraceWriter(baseName + " archive decompressed");
				}
			} catch (IOException ie) {
				// This is caught when it either was already unzipped or
				if (ie.getMessage().indexOf("already exists") < 0) {
					throw ie;
				}
				history.createTraceWriter("Stage input already extracted");
				System.err.println("Stage input already extracted");
			}

		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Error staging in to %s", _target.getName())
				.format("Error staging in from %s to %s.", _source, _target).close();
			throw cause;
		}
	}
}