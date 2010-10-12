package edu.virginia.vcgr.genii.ui.plugins.queue;

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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

class QueueManagerPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	static final private Dimension TABLE_SIZE = new Dimension(
		1200, 600);
	
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
		
		private JobHistoryAction(int []selectedRows)
		{
			super("Job History");
			
			setEnabled(selectedRows.length == 1);
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(row).getTicket().toString());
			
			QueueManipulation.jobHistory(_context, _table, _model,
				jobTickets);
		}
	}
	
	private class JobKillerAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private JobKillerAction(int []rows)
		{
			super("End Jobs");
			
			setEnabled(rows.length > 0);
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(row).getTicket().toString());
			
			QueueManipulation.killJobs(_context, _table, _model, jobTickets);
		}
	}
	
	private class JobCompleterAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private JobCompleterAction(int []rows)
		{
			super("Remove Jobs");
			
			setEnabled(rows.length > 0);
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			Collection<String> jobTickets = new LinkedList<String>();
			for (int row : _table.getSelectedRows())
				jobTickets.add(_model.row(row).getTicket().toString());
			
			QueueManipulation.completeJobs(
				_context, _table, _model, jobTickets);
		}
	}
	
	private UIPluginContext _context;
	private JTable _table;
	private QueueManagerTableModel _model;
	
	private void popup(MouseEvent e)
	{
		int []rows = _table.getSelectedRows();
		
		JPopupMenu popup = new JPopupMenu("Queue Manager Popup");
		popup.add(new JobHistoryAction(rows));
		popup.addSeparator();
		popup.add(new JobKillerAction(rows));
		popup.add(new JobCompleterAction(rows));
		popup.addSeparator();
		popup.add(new RefreshAction());
		
		popup.show(_table, e.getX(), e.getY());
	}
	
	QueueManagerPanel(UIPluginContext context)
		throws ResourceException, GenesisIISecurityException, 
			RNSPathDoesNotExistException
	{
		super(new GridBagLayout());
		
		_context = context;
		
		_model = new QueueManagerTableModel(context);
		_table = new JTable(_model);
		_model.prepareTableColumns(_table.getColumnModel());
		_table.setAutoCreateRowSorter(true);
		_table.addMouseListener(new PopupListener());
		
		_table.getActionMap().put("Refresh", new RefreshAction());
		_table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0x0), "Refresh");
		
		_model.refresh(_table);
		
		JScrollPane pane = new JScrollPane(_table);
		
		add(pane,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
			new Insets(5, 5, 5, 5), 5, 5));
		
		setPreferredSize(TABLE_SIZE);
	}
}