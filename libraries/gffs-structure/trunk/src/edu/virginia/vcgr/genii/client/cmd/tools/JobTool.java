package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.externalapp.EditableFile;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.gjt.JobToolManager;
import edu.virginia.vcgr.genii.gjt.gui.GridJobToolFrame;
import edu.virginia.vcgr.genii.ui.BasicFrameWindow;

public class JobTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(JobTool.class);

	static final private String USAGE = "config/tooldocs/usage/ujob-tool";
	static final private String DESCRIPTION = "config/tooldocs/description/djob-tool";
	static final private String _MANPAGE = "config/tooldocs/man/job-tool";

	@Override
	protected void verify() throws ToolException
	{
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		Collection<EditableFile> files = new ArrayList<EditableFile>(numArguments());
		for (String arg : getArguments())
			files.add(EditableFile.createEditableFile(new GeniiPath(arg)));

		Collection<File> tmpFiles = new ArrayList<File>(files.size());
		for (EditableFile file : files)
			tmpFiles.add(file.file());

		JobToolManager.launch(tmpFiles, null);

		while (true) {
			if (BasicFrameWindow.activeFrames(GridJobToolFrame.class) <= 0) {
				/*
				 * we have found that it's time to leave since there are no job tool frames left (although we really only think we'll see this as zero
				 * and not negative).
				 */
				break;
			}
			try {
				Thread.sleep(42);
			} catch (InterruptedException e) {
				// ignored.
			}
		}

		_logger.info("all job tool windows have exited; closing application.");
		
		for (EditableFile file : files)
			StreamUtils.close(file);
			
		
		return 0;
	}

	public JobTool()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), false, ToolCategory.EXECUTION);
		addManPage(new LoadFileResource(_MANPAGE));
	}
}