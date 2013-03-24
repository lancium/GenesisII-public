package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.externalapp.EditableFile;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.gjt.BlockingJobToolListener;

public class JobTool extends BaseGridTool
{
	static final private String USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/ujob-tool";
	static final private String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/djob-tool";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/job-tool";

	@Override
	protected void verify() throws ToolException
	{
	}

	@Override
	protected int runCommand() throws Throwable
	{
		Collection<EditableFile> files = new ArrayList<EditableFile>(numArguments());
		for (String arg : getArguments())
			files.add(EditableFile.createEditableFile(new GeniiPath(arg)));

		Collection<File> tmpFiles = new ArrayList<File>(files.size());
		for (EditableFile file : files)
			tmpFiles.add(file.file());

		BlockingJobToolListener waiter = new BlockingJobToolListener();
		edu.virginia.vcgr.genii.gjt.JobTool.launch(tmpFiles, null, waiter);
		waiter.join();

		for (EditableFile file : files)
			StreamUtils.close(file);
		return 0;
	}

	public JobTool()
	{

		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}
}