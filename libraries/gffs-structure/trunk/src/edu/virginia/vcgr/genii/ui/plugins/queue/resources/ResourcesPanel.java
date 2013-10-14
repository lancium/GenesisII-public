package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourcesPanel extends JPanel implements LazyLoadTabHandler
{
	static final long serialVersionUID = 0L;

	static final private Dimension TABLE_SIZE = new Dimension(1400, 600);

	static private Log _logger = LogFactory.getLog(ResourcesPanel.class);

	private class BESUpdaterCompletionListener implements TaskCompletionListener<Boolean>
	{
		@Override
		public void taskCompleted(Task<Boolean> task, Boolean result)
		{
			JOptionPane.showMessageDialog(_table, "Update Complete.  Note that updates may not show up right away.",
				"Update Complete", JOptionPane.INFORMATION_MESSAGE);

			_model.refresh(_table);
		}

		@Override
		public void taskCancelled(Task<Boolean> task)
		{
			// Do nothing
		}

		@Override
		public void taskExcepted(Task<Boolean> task, Throwable cause)
		{
			ErrorHandler.handleError(_context.uiContext(), _table, cause);
		}
	}

	private class BESUpdaterTask extends AbstractTask<Boolean>
	{
		private Collection<String> _updateList;

		private BESUpdaterTask(Collection<String> updateList)
		{
			_updateList = updateList;
		}

		@Override
		public Boolean execute(TaskProgressListener progressListener) throws Exception
		{
			_model.forceUpdate(_updateList);
			return Boolean.TRUE;
		}
	}

	private class RefreshAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private RefreshAction()
		{
			super("Refresh");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_model.refresh(_table);
		}
	}

	private class PopupListener extends MouseAdapter
	{
		@Override
		final public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}

		@Override
		final public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}

		@Override
		final public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}
	}

	private class ResourceUpdateAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private ResourceUpdateAction(int[] selectedRows)
		{
			super((selectedRows.length > 1) ? "Update Selected Resources" : "Update Selected Resource");

			setEnabled(selectedRows.length > 0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			Collection<String> besNames = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				besNames.add(_model.row(row).name());

			_context
				.uiContext()
				.progressMonitorFactory()
				.createMonitor(_table, "Forcing Resource Update", "Forcing BES Resource Update", 1000L,
					new BESUpdaterTask(besNames), new BESUpdaterCompletionListener()).start();
		}
	}

	private class BrowseAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private BrowseAction(int[] selectedRows)
		{
			super("Browse BES");

			setEnabled(selectedRows.length == 1);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			try {
				UIContext context = (UIContext) _context.uiContext().clone();
				int row = _table.getSelectedRow();
				if (sorter != null)
					row = sorter.convertRowIndexToModel(row);

				context.callingContext().setCurrentPath(new RNSPath(_model.row(row).endpoint()));
				ClientApplication app = new ClientApplication(context, false);
				app.pack();
				GUIUtils.centerComponent(app);
				app.setVisible(true);
			} catch (Throwable cause) {
				// Ignore
				_logger.info("exception occurred in actionPerformed", cause);
			}
		}
	}

	private UIPluginContext _context;

	private ResourcesTableModel _model;
	private JTable _table;

	private void popup(MouseEvent e)
	{
		int[] rows = _table.getSelectedRows();

		JPopupMenu popup = new JPopupMenu("Queue Manager Popup");
		popup.add(new ResourceUpdateAction(rows));
		popup.addSeparator();
		popup.add(new BrowseAction(rows));
		popup.addSeparator();
		popup.add(new RefreshAction());

		popup.show(_table, e.getX(), e.getY());
	}

	public ResourcesPanel(UIPluginContext context) throws ResourceException, GenesisIISecurityException,
		RNSPathDoesNotExistException
	{
		super(new GridBagLayout());

		setName("Resource Management");

		_context = context;

		_model = new ResourcesTableModel(context);
		_table = new JTable(_model);
		_model.prepareTableColumns(_table.getColumnModel());
		_table.setAutoCreateRowSorter(true);
		_table.addMouseListener(new PopupListener());

		_table.getActionMap().put("Refresh", new RefreshAction());
		_table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0x0), "Refresh");

		JScrollPane pane = new JScrollPane(_table);

		add(pane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
			5, 5, 5, 5), 5, 5));
		add(new JButton(new RefreshAction()), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		setPreferredSize(TABLE_SIZE);
	}

	@Override
	final public void load()
	{
		_model.refresh(_table);
	}
}
