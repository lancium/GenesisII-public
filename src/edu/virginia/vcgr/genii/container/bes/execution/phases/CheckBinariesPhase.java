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

import com.fasterxml.jackson.databind.util.ISO8601DateFormat; // Apache 2.0 License

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class CheckBinariesPhase extends AbstractExecutionPhase implements Serializable {
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(CheckBinariesPhase.class);

	static private final String CHECKING_BINARIES_STATE = "checking-binaries";
	static private final long CLOCK_SHIFT_MARGIN = 600000; // 10 minutes

	
	// change these first 4 to locals, pass source and target to doStageInPhase as parameters rather than using globals
	private String _execName;

	public CheckBinariesPhase(String execName) {
		super(new ActivityState(ActivityStateEnumeration.Running, CHECKING_BINARIES_STATE, false));
		// CCH 2020-07-01
		// This phase is only created once in CommonExecutionUnderstanding.java
		// It is created only when the executable name is an image (.simg, .qcow2, .sif)
		// We also assume the execName is in the form Lancium/<image> or just <image>. 
		// Lancium/ indicates if you want to use a Lancium image and we change the source and target paths accordingly
		// We calculate the source and target paths from execName and the username
		_execName = execName;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageIn);
		history.createInfoWriter("Entered CheckBinariesPhase...");
		String[] _execNameArray = _execName.split("/");
		_execName = _execNameArray[_execNameArray.length-1];
		boolean usingLanciumImage = false;
		if (_execNameArray.length >= 2)
			usingLanciumImage = _execNameArray[_execNameArray.length-2].equals("Lancium");
		
		// The following is code to figure out the username
		// We need to username because we build the local and grid paths to the images with it
		ICallingContext callContext = context.getCallingContext();
		String prefId = (PreferredIdentity.getCurrent() != null ? PreferredIdentity.getCurrent().getIdentityString() : null);
		X509Certificate owner = GffsExportConfiguration.findPreferredIdentityServerSide(callContext, prefId);
		String userName = CredentialWallet.extractUsername(owner);
		_logger.info("Calculated username: " + userName);
		
		String sharedDir = context.getCurrentWorkingDirectory().getWorkingDirectory().getParent().toString();
		File userImageDir = new File(sharedDir+"/Images/" + userName);
		if (_logger.isDebugEnabled())
			_logger.debug("User Image Directory: " + userImageDir.toString()); 
		if (!userImageDir.exists()) {
			if (_logger.isDebugEnabled())
				_logger.debug("User Image Directory doesn't exist, creating it... "); 
			userImageDir.mkdirs();
		}
		if (_logger.isDebugEnabled())
			_logger.debug("Shared directory: " + sharedDir); 
		String imageSourceGeniiPathString = (usingLanciumImage ? "/bin/Lancium/Images/" : "/home/CCC/Lancium/" + userName + "/Images/") + _execName;
		String imageTargetFileString = sharedDir + "/Images/" + (usingLanciumImage ? "Lancium/" : userName+"/") + _execName;
		RNSPath sourceRNS = new GeniiPath(imageSourceGeniiPathString).lookupRNS();
		File target = new File(imageTargetFileString);
		URI source;
		try {
			source = new URI("rns:"+imageSourceGeniiPathString);
		} catch (URISyntaxException e) {
			source = null;
		}
		
		if (_logger.isDebugEnabled()) {
			_logger.debug("Finished initial setup of CheckBinariesPhase with the following information:"); 
			if (sourceRNS != null) _logger.debug("Source RNSPath: " + sourceRNS); 
			if (source != null) _logger.debug("Source URI: " + source); 
			if (target != null) _logger.debug("Target file: " + target); 
		}
		if (target.exists()) {
			// Note: The target file will only exist when the download is finished! The DownloadManager copies to a temporary file then moves it to target.
			// If the download is in progress, we will send a request to the DownloadManger, which will handle waiting for the download to finish.
			if (_logger.isDebugEnabled())
				_logger.debug("Target file exists, checking last modified dates");
			
			// get source last modified time (in GFFS)
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, sourceRNS.getEndpoint());
			GetResourcePropertyDocumentResponse resp = common.getResourcePropertyDocument(new GetResourcePropertyDocument());
			MessageElement document = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, "attributes"));
			for (MessageElement child : resp.get_any()) {
				document.addChild(child);
			}
			String sourceLastModified = document.toString().split("ModificationTime")[1].split(">")[1].split("<")[0];
			
			// Get target last modified time (in local FS)
			String targetLastModified = Files.readAttributes(target.toPath(), BasicFileAttributes.class).lastModifiedTime().toString();
			
			// Check if source is newer than target
			ISO8601DateFormat df = new ISO8601DateFormat();
			long sourceLMDateMillis = df.parse(sourceLastModified.substring(0, sourceLastModified.length()-5)+"Z").getTime();
			long targetLMDateMillis = df.parse(targetLastModified).getTime();
			
			// Print debug info. both String and Date objects
			if (_logger.isDebugEnabled()) {
				_logger.debug("Source last modified time: " + sourceLastModified);
				_logger.debug("Target last modified time: " + targetLastModified);
				_logger.debug("Source last modified date in millis: " + sourceLMDateMillis);
				_logger.debug("Target last modified date in millis: " + targetLMDateMillis);
				_logger.debug("Source > (target + 10 minutes)? : " + (sourceLMDateMillis > (targetLMDateMillis + CLOCK_SHIFT_MARGIN))); 
			}
			
			// If file in GFFS (Source) is CLOCK_SHIFT_MARGIN+ minutes older than file in local FS (Target), copy
			// If not, no copy is required because local FS is up-to-date
			if (sourceLMDateMillis > (targetLMDateMillis + CLOCK_SHIFT_MARGIN)) {
				if (_logger.isDebugEnabled())
					_logger.debug("Source newer than target, deleting target file " + target.toString());
				target.delete();
				if (_logger.isDebugEnabled())
					_logger.debug("Calling doStageInPhase to copy down newer source to target location " + target.toString());
				doStageInPhase(context, history, source, target);
			}
			else {
				if (_logger.isDebugEnabled())
					_logger.debug("Source older than target, no need to replace existing image.");
			}
		}
		else {
			if (_logger.isDebugEnabled())
				_logger.debug("Target file does not exist, call doStageInPhase to copy down newer source to target location " + target.toString());
			doStageInPhase(context, history, source, target);
		}
	}
	
	private void doStageInPhase(ExecutionContext context, HistoryContext history, URI source, File target) throws Throwable {
		DataTransferStatistics stats;
		if (_logger.isDebugEnabled())
			_logger.debug("Entering StageinPhase: execute '" + source + "', target = " + target);
		try {
			DownloadManagerContainerService service = ContainerServices.findService(DownloadManagerContainerService.class);
			stats = service.download(source, target, null);
			// 2020-06-30 by CCH - Add o+r to permissions so copied qcow2 images can run properly
			FileSystemUtils.chmod(target.getAbsolutePath(),
					FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE | FileSystemUtils.MODE_USER_EXECUTE
							| FileSystemUtils.MODE_GROUP_READ | FileSystemUtils.MODE_GROUP_WRITE | FileSystemUtils.MODE_GROUP_EXECUTE 
							| FileSystemUtils.MODE_WORLD_READ);
			history.createTraceWriter("%s: %d Bytes Transferred", target.getName(), stats.bytesTransferred())
					.format("%d bytes were transferred in %d ms.", stats.bytesTransferred(), stats.transferTime())
					.close();
		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Error staging in to %s", target.getName())
					.format("Error staging in from %s to %s.", source, target).close();
			throw cause;
		}
	}
}
