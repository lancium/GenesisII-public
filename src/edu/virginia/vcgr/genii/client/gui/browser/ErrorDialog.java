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

/**
 * The ErrorDialog class implements a simple error display
 * dialog box that can display both an error message and
 * an exception.
 * 
 * @author mmm2a
 */
public class ErrorDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a new ErrorDialog.
	 * 
	 * @param owner The frame that owns this dialog.
	 * @param msg The error message to display.
	 * @param cause The exception that caused the error condition (or null).
	 */
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
	
	/**
	 * A small internal class that is registered to listen to button clicks.
	 * 
	 * @author mmm2a
	 */
	private class OKHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			dispose();
			setVisible(false);
		}
	}
	
	/**
	 * This helper method is called to generate the component that shows the
	 * stack trace (if any).
	 * 
	 * @param cause The exception that caused an error (or null).
	 * 
	 * @return A component that can be displayed in the dialog to give
	 * information about the error.
	 */
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
	
	/**
	 * A static method to create an error dialog and display the contents.
	 * 
	 * @param owner The frame that owns the error dialog.
	 * @param msg The error message to display.
	 * @param cause An exception that caused the error to happen (or null).
	 */
	static public void showErrorDialog(Frame owner, String msg, Throwable cause)
	{
		ErrorDialog dialog = new ErrorDialog(owner, msg, cause);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);
	}
	
	/**
	 * A static method to create an error dialog and display the contents.
	 * 
	 * @param owner The frame that owns the error dialog.
	 * @param msg The error message to display.
	 */
	static public void showErrorDialog(Frame owner, String msg)
	{
		showErrorDialog(owner, msg, null);
	}
}
