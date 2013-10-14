package edu.virginia.vcgr.appmgr.patch.builder.diffengine;

public interface DifferenceHandler
{
	public void analyzing(DirCompContext context, String relativePath);

	public void directoryAdded(DirCompContext context, String relativePath);

	public void directoryRemoved(DirCompContext context, String relativePath);

	public void fileAdded(DirCompContext context, String relativePath);

	public void fileDeleted(DirCompContext context, String relativePath);

	public void fileModified(DirCompContext context, String relativePath);

	public void directoryChangedToFile(DirCompContext context, String relativePath);

	public void fileChangedToDirectory(DirCompContext context, String relativePath);
}