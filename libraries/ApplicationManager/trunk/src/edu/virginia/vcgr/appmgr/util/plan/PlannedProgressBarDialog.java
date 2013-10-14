package edu.virginia.vcgr.appmgr.util.plan;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.Closeable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public class PlannedProgressBarDialog<PlanContext extends Closeable> extends JDialog
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(PlannedProgressBarDialog.class);

	private List<PlannedAction<PlanContext>> _actions;

	private JLabel _label;
	private JProgressBar _bar;
	private OKAction _okAction;
	private Thread _actorThread = null;

	public PlannedProgressBarDialog(JFrame owner, String title, List<PlannedAction<PlanContext>> plan)
	{
		super(owner);
		setTitle(title);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		_actions = plan;

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel("Progress"), new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_bar = new JProgressBar(JProgressBar.HORIZONTAL, 0, _actions.size()), new GridBagConstraints(0, 1, 2, 1,
			1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_label = new JLabel(), new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(_okAction = new OKAction()), new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
			GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTH,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}

	public void start(PlanContext planContext)
	{
		_actorThread = new Thread(new Actor(planContext), "Planned Action Actor Thread");
		_actorThread.setDaemon(false);
		_actorThread.start();

		super.setVisible(true);
	}

	private void update(int progress, String message)
	{
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Updater(progress, message));
		} else {
			_bar.setValue(progress);
			_label.setText(message);

			if (progress >= _actions.size())
				_okAction.setEnabled(true);
		}
	}

	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		static private final String NAME = "OK";

		public OKAction()
		{
			super(NAME);

			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			dispose();
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		static private final String NAME = "Cancel";

		public CancelAction()
		{
			super(NAME);
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			if (_actorThread != null)
				_actorThread.interrupt();
			try {
				_actorThread.join();
			} catch (InterruptedException ie) {
			}

			dispose();
		}
	}

	private class Updater implements Runnable
	{
		private int _progress;
		private String _message;

		public Updater(int progress, String message)
		{
			_progress = progress;
			_message = message;
		}

		@Override
		public void run()
		{
			update(_progress, _message);
		}
	}

	private class Actor implements Runnable
	{
		private PlanContext _planContext;

		public Actor(PlanContext planContext)
		{
			_planContext = planContext;
		}

		@Override
		public void run()
		{
			int count = 0;

			for (PlannedAction<PlanContext> action : _actions) {
				if (Thread.interrupted())
					break;

				update(count, action.toString());
				try {
					action.perform(_planContext);
				} catch (Throwable cause) {
					_logger.error("failure in Actor.run:", cause);
					JOptionPane.showMessageDialog(PlannedProgressBarDialog.this, cause.getLocalizedMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
					break;

				}
				update(++count, "");
			}

			update(count, "Finished");
			IOUtils.close(_planContext);
		}
	}
}