package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class QueueManagerDialog extends JFrame
{
	static private Log _logger = LogFactory.getLog(QueueManagerDialog.class);
	static final long serialVersionUID = 0L;
	
	static private void prepareColumns(JTable table)
	{
		TableColumn column;
		
		column = table.getColumnModel().getColumn(0);
		column.setHeaderValue("Ticket");
		column.setPreferredWidth(300);
		
		column = table.getColumnModel().getColumn(1);
		column.setHeaderValue("Submit Time");
		column.setPreferredWidth(250);
		
		column = table.getColumnModel().getColumn(2);
		column.setHeaderValue("Owners");
		column.setPreferredWidth(250);
		
		column = table.getColumnModel().getColumn(3);
		column.setHeaderValue("Attempts");
		
		column = table.getColumnModel().getColumn(4);
		column.setHeaderValue("Job State");
	}
	
	static private class StateRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 0L;

		@Override
		protected void setValue(Object value)
		{
			String sValue = value.toString();
			super.setValue(value);

			if (sValue.equals("ERROR"))
				setForeground(Color.RED);
			else if (sValue.startsWith("On "))
				setForeground(Color.GREEN);
			else
				setForeground(Color.BLUE);
		}
	}
	
	static private class TimestampRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 0L;

		@Override
		protected void setValue(Object value)
		{
			super.setValue(String.format("%1$tH:%1$tM %1$tZ %1$td %1$tb %1$tY",
				value));
		}
	}
	
	private class QueueManipulatorWorker implements Runnable
	{
		private Collection<JobTicket> _tickets;
		private boolean _isComplete;
		
		public QueueManipulatorWorker(Collection<JobTicket> tickets, 
			boolean isComplete)
		{
			_tickets = tickets;
			_isComplete = isComplete;
		}
		
		@Override
		public void run()
		{
			QueueManipulator manipulator = new QueueManipulator(_queueEPR);
			
			try
			{
				if (!_isComplete)
					manipulator.kill(_tickets);
				else
					manipulator.complete(_tickets);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to complete/kill jobs.", cause);
				JOptionPane.showMessageDialog(QueueManagerDialog.this, 
					"Unable to complete/kill jobs.", "Queue Error", 
					JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				((QueueTableModel)_table.getModel()).refresh();
			}
		}
	}
	
	private EndpointReferenceType _queueEPR;
	private JTable _table;
	
	QueueManagerDialog(EndpointReferenceType queue) throws RemoteException
	{
		super("Queue Manager");
		
		_queueEPR = queue;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		RefreshStatusLabel status = new RefreshStatusLabel("Updating contents");
		
		QueueTableModel model = new QueueTableModel(queue);
		model.addRefreshListener(status);
		
		_table = new JTable(model);
		_table.setAutoCreateRowSorter(true);
		_table.getColumnModel().getColumn(4).setCellRenderer(
			new StateRenderer());
		_table.getColumnModel().getColumn(1).setCellRenderer(
			new TimestampRenderer());
		prepareColumns(_table);
		JScrollPane scroller = new JScrollPane(_table);
		scroller.setPreferredSize(new Dimension(1000, 500));
		content.add(scroller,
			new GridBagConstraints(0, 0, 5, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new ShowLogAction(_table)),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new ShowErrorsAction(_table)),
			new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CompleteAction(_table)),
			new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new DeleteAction(_table)),
			new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new RefreshAction()),
			new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		content.add(status, new GridBagConstraints(0, 2, 5, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class CompleteAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private CompleteAction(JTable table)
		{
			super("Complete");
			
			setEnabled(false);
			table.getSelectionModel().addListSelectionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			int []indices = _table.getSelectedRows();
			Collection<JobTicket> jobs = new LinkedList<JobTicket>();
			
			for (int index : indices)
			{
				jobs.add(new JobTicket(
					_table.getValueAt(index, 0).toString()));
			}
			
			Thread th = new Thread(new QueueManipulatorWorker(jobs, true));
			th.setName("Queue Actioner Thread");
			th.setDaemon(true);
			th.start();
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_table.getSelectedRow() >= 0);
		}
	}
	
	private class DeleteAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private DeleteAction(JTable table)
		{
			super("Delete");
			
			setEnabled(false);
			table.getSelectionModel().addListSelectionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			int []indices = _table.getSelectedRows();
			int result = JOptionPane.showConfirmDialog(QueueManagerDialog.this, 
				String.format("Are you sure you want to delete %d jobs?",
					indices.length),
				"Queue Delete Confirmation?", JOptionPane.YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION)
				return;
			
			Collection<JobTicket> jobs = new LinkedList<JobTicket>();
			
			for (int index : indices)
			{
				jobs.add(new JobTicket(
					_table.getValueAt(index, 0).toString()));
			}
			
			Thread th = new Thread(new QueueManipulatorWorker(jobs, false));
			th.setName("Queue Actioner Thread");
			th.setDaemon(true);
			th.start();
		}


		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_table.getSelectedRow() >= 0);
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
		public void actionPerformed(ActionEvent event)
		{
			((QueueTableModel)_table.getModel()).refresh();
		}
	}
	
	private class ShowErrorsAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private ShowErrorsAction(JTable table)
		{
			super("Show Errors");
			
			setEnabled(false);
			table.getSelectionModel().addListSelectionListener(this);
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_table.getSelectedRowCount() == 1);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int index = _table.getSelectedRow();
			
			ErrorDisplay display = new ErrorDisplay(QueueManagerDialog.this,
				_queueEPR, new JobTicket(_table.getValueAt(index, 0).toString()));
			display.pack();
			GuiUtils.centerComponent(display);
			display.setModalityType(ModalityType.DOCUMENT_MODAL);
			display.setVisible(true);
		}
	}
	
	private class ShowLogAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private ShowLogAction(JTable table)
		{
			super("Show Job Log");
			
			setEnabled(false);
			table.getSelectionModel().addListSelectionListener(this);
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_table.getSelectedRowCount() == 1);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int index = _table.getSelectedRow();
			
			LogDisplay display = new LogDisplay(QueueManagerDialog.this,
				_queueEPR, new JobTicket(_table.getValueAt(index, 0).toString()));
			display.pack();
			GuiUtils.centerComponent(display);
			display.setModalityType(ModalityType.DOCUMENT_MODAL);
			display.setVisible(true);
		}
	}
}