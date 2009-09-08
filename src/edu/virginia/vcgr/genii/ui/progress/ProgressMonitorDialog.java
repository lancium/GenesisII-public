package edu.virginia.vcgr.genii.ui.progress;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

class ProgressMonitorDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private Window _parent;
	private Task<?> _task = null;
	private Future<?> _taskFuture = null;
	private JLabel _subTitle = new JLabel(" ");
	
	private void updateSubTitle(String subTitle)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new UpdateSubTitleWorker(subTitle));
			return;
		}
		
		if (subTitle == null || subTitle.length() == 0)
			subTitle = " ";
		
		_subTitle.setText(subTitle);
	}
	
	ProgressMonitorDialog(Window owner, String title, String subTitle)
	{
		super(owner, title);
		_parent = owner;
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		
		content.add(_subTitle, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(progressBar, new GridBagConstraints(
			0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(
			0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		updateSubTitle(subTitle);
		setModalityType(ModalityType.MODELESS);
	}
	
	TaskProgressListener taskProgressListener()
	{
		return new TaskProgressListener()
		{	
			@Override
			public void updateSubTitle(String subTitle)
			{
				ProgressMonitorDialog.this.updateSubTitle(subTitle);
			}
		};
	}
	
	void popup(Task<?> task, Future<?> taskFuture)
	{
		_task = task;
		_taskFuture = taskFuture;
		pack();
		setLocationRelativeTo(_parent);
		setVisible(true);
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CancelAction()
		{
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_task.cancel();
			_taskFuture.cancel(true);
		}
	}
	
	private class UpdateSubTitleWorker implements Runnable
	{
		private String _subTitle;
		
		private UpdateSubTitleWorker(String subTitle)
		{
			_subTitle = subTitle;
		}
		
		@Override
		public void run()
		{
			updateSubTitle(_subTitle);
		}
	}
}