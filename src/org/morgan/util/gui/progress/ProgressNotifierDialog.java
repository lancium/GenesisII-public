package org.morgan.util.gui.progress;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

class ProgressNotifierDialog extends JDialog implements ProgressNotifier
{
	static final long serialVersionUID = 0L;
	
	private CancelController _controller;
	private JLabel _note = new JLabel();
	private JProgressBar _progressBar = new JProgressBar();

	public ProgressNotifierDialog(Window owner,
		String title, String note, ProgressTask<?> task,
		int initialProgress, CancelController controller)
	{
		super(owner);
		setTitle(title);
		
		_controller = controller;
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		if (task.isProgressIndeterminate())
			_progressBar.setIndeterminate(true);
		else
		{
			_progressBar.setMinimum(task.getMinimumProgressValue());
			_progressBar.setMaximum(task.getMaximumProgressValue());
			updateProgress(initialProgress);
		}
		
		updateNote(note);
		
		add(_note, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_progressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new CancelAction()), new GridBagConstraints(
			0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	@Override
	public void initialize(CancelController cancelController, 
		ProgressTask<?> task)
	{
		// Ignore
	}

	@Override
	public void updateNote(String newNote)
	{
		if (newNote == null || newNote.length() <= 0)
			newNote = " ";
		
		_note.setText(newNote);
	}

	@Override
	public void updateProgress(int newValue)
	{
		_progressBar.setValue(newValue);
	}

	@Override
	public void finished()
	{
		dispose();
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public CancelAction()
		{
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			_controller.cancelRequested();
		}
	}
}