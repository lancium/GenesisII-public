package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.awt.Component;
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

import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

public class QueueManagerPanel extends JPanel implements LazyLoadTabHandler
{
	static final long serialVersionUID = 0L;

	static final private Dimension TABLE_SIZE = new Dimension(1200, 600);

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

	private class JobHistoryAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JobHistoryAction(int[] selectedRows)
		{
			super("Job History");

			setEnabled(selectedRows.length == 1);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			QueueManipulation.jobHistory(_context, _table, _model, jobTickets);
		}
	}

	private class DumpJobHistoryAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private DumpJobHistoryAction(int[] selectedRows)
		{
			super("Dump Job History");

			setEnabled(selectedRows.length == 1);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			String answer = JOptionPane.showInputDialog((Component) e.getSource(), "Where would you like to store the history dump?");
			if (answer == null)
				return;

			QueueManipulation.dumpJobHistory(_context, _table, _model, jobTickets, new GeniiPath(answer));
		}
	}

	private class JobKillerAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JobKillerAction(int[] rows)
		{
			super("End Jobs");

			setEnabled(rows.length > 0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			QueueManipulation.killJobs(_context, _table, _model, jobTickets);
		}
	}

	private class JobCompleterAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JobCompleterAction(int[] rows)
		{
			super("Remove Jobs");

			setEnabled(rows.length > 0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			QueueManipulation.completeJobs(_context, _table, _model, jobTickets);
		}
	}

	private class JobRescheduleAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JobRescheduleAction(int[] rows)
		{
			super("Reschedule Jobs");

			setEnabled(rows.length > 0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			QueueManipulation.JobRescheduleTask(_context, _table, _model, jobTickets);
		}
	}
	
	private class JobResetAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JobResetAction(int[] rows)
		{
			super("Reset Attempts");

			setEnabled(rows.length > 0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			RowSorter<? extends TableModel> sorter = _table.getRowSorter();

			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(sorter == null ? row : sorter.convertRowIndexToModel(row)).getTicket().toString());

			QueueManipulation.JobResetTask(_context, _table, _model, jobTickets);
		}
	}
	
	private UIPluginContext _context;
	private JTable _table;
	private QueueManagerTableModel _model;

	private void popup(MouseEvent e)
	{
		int[] rows = _table.getSelectedRows();

		JPopupMenu popup = new JPopupMenu("Queue Manager Popup");
		popup.add(new JobHistoryAction(rows));
		popup.add(new DumpJobHistoryAction(rows));
		popup.addSeparator();
		popup.add(new JobKillerAction(rows));
		popup.add(new JobCompleterAction(rows));
		popup.add(new JobRescheduleAction(rows));
		popup.add(new JobResetAction(rows));
		popup.addSeparator();
		popup.add(new RefreshAction());

		popup.show(_table, e.getX(), e.getY());
	}

	public QueueManagerPanel(UIPluginContext context) throws ResourceException, GenesisIISecurityException, RNSPathDoesNotExistException
	{
		super(new GridBagLayout());

		setName("Job Management");

		_context = context;

		_model = new QueueManagerTableModel(context);
		_table = new JTable(_model);
		_model.prepareTableColumns(_table.getColumnModel());
		_table.setAutoCreateRowSorter(true);
		_table.addMouseListener(new PopupListener());

		_table.getActionMap().put("Refresh", new RefreshAction());
		_table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0x0), "Refresh");

		JScrollPane pane = new JScrollPane(_table);

		add(pane,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new RefreshAction()),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		setPreferredSize(TABLE_SIZE);
	}

	@Override
	final public void load()
	{
		_model.refresh(_table);
	}
}