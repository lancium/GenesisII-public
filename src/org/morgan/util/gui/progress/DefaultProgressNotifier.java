package org.morgan.util.gui.progress;

import java.awt.Component;
import java.awt.Window;
import java.awt.Dialog.ModalityType;

import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

public class DefaultProgressNotifier implements ProgressNotifier
{
	private Component _parent;
	private String _title;
	private String _initialNote;
	private int _progress;
	private long _millisToWait;
	
	private CancelController _controller = null;
	private ProgressTask<?> _task = null;
	
	private Object finishLock = new Object();
	
	private boolean _finished = false;
	private ProgressNotifier _dialog = null;
	
	public DefaultProgressNotifier(Component parent, String title,
		String initialNote, long millisToWait)
	{
		_parent = parent;
		_title = title;
		_initialNote = initialNote;
		_millisToWait = millisToWait;
	}
	
	@Override
	public void initialize(CancelController cancelController, 
		ProgressTask<?> task)
	{
		_controller = cancelController;
		_task = task;
		
		Thread th = new Thread(new Waiter(), "Progress Waiter");
		th.setDaemon(true);
		th.start();
	}

	@Override
	public void updateNote(String newNote)
	{
		synchronized(this)
		{
			_initialNote = newNote;
			if (_dialog != null)
				_dialog.updateNote(newNote);
		}
	}

	@Override
	public void updateProgress(int newValue)
	{
		synchronized(this)
		{
			_progress = newValue;
			if (_dialog != null)
				_dialog.updateProgress(newValue);
		}
	}
	
	@Override
	public void finished()
	{
		synchronized(this)
		{
			synchronized(finishLock)
			{
				finishLock.notifyAll();
			}
			
			_finished = true;
			if (_dialog != null)
				_dialog.finished();
		}
	}
	
	private class Waiter implements Runnable
	{
		@Override
		public void run()
		{
			long moveOnTime = System.currentTimeMillis() + _millisToWait;
			
			long toWait = moveOnTime - System.currentTimeMillis();
			while (toWait > 0)
			{
				synchronized(finishLock)
				{
					try { finishLock.wait(toWait); } 
					catch (InterruptedException ie) {}
				}
				
				toWait = moveOnTime - System.currentTimeMillis();
			}
			
			synchronized(DefaultProgressNotifier.this)
			{
				if (!_finished)
				{
					Window owner = null;
					if (_parent != null)
						owner = SwingUtilities.getWindowAncestor(_parent);
					
					ProgressNotifierDialog dialog = new ProgressNotifierDialog(
						owner, _title, _initialNote, _task,
						_progress, _controller);
					
					dialog.pack();
					GuiUtils.centerComponent(dialog);
					dialog.setModalityType(ModalityType.MODELESS);
					dialog.setVisible(true);
					
					_dialog = new SwingInvokeProgressNotifierHolder(dialog);
				}
			}
		}
	}
}