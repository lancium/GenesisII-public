package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.ws.addressing.EndpointReferenceType;

public class QueueManagerDialog extends JFrame
{
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
	
	QueueManagerDialog(EndpointReferenceType queue) throws RemoteException
	{
		super("Queue Manager");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		JTable table = new JTable(new QueueTableModel(queue));
		prepareColumns(table);
		JScrollPane scroller = new JScrollPane(table);
		scroller.setPreferredSize(new Dimension(1000, 500));
		content.add(scroller,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
	}
}