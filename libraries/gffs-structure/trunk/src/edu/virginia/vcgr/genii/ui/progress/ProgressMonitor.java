package edu.virginia.vcgr.genii.ui.progress;

public interface ProgressMonitor
{
	public void addProgressMonitorListener(ProgressMonitorListener listener);

	public void removeProgressMonitorListener(ProgressMonitorListener listener);

	public void start();
}