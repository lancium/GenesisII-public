package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

public class ErrorDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private ErrorDialog(Frame owner, String msg, Throwable cause)
	{
		super(owner);
		setTitle("Browser Error");
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		
		contentPane.add(new JLabel(msg),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(createDetails(cause),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new OKHandler());
		contentPane.add(ok,
			new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class OKHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			dispose();
			setVisible(false);
		}
	}
	
	private Component createDetails(Throwable cause)
	{
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		
		if (cause == null)
			printer.println("No further details!");
		else
			cause.printStackTrace(printer);
		
		printer.flush();
		JTextArea area = new JTextArea(writer.toString());
		area.setEditable(false);
		
		JScrollPane pane = new JScrollPane(area);
		Dimension size = new Dimension(500, 500);
		pane.setMinimumSize(size);
		pane.setPreferredSize(size);
		
		pane.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Details"),
				pane.getBorder()));
		return pane;
	}
	
	static public void showErrorDialog(Frame owner, String msg, Throwable cause)
	{
		ErrorDialog dialog = new ErrorDialog(owner, msg, cause);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);
	}
	
	static public void showErrorDialog(Frame owner, String msg)
	{
		showErrorDialog(owner, msg, null);
	}
}
