package org.morgan.util.gui.progress;

public interface ProgressNotifier
{
	public void initialize(CancelController cancelController,
		ProgressTask<?> task);
	
	public void updateNote(String newNote);
	public void updateProgress(int newValue);
	
	public void finished();
}