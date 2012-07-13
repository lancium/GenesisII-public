package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class LogDisplay extends JDialog
{
	static final long serialVersionUID = 0L;

	private EndpointReferenceType _queue;
	private JobTicket _job;
	private JTextPane _text;
	
	private class TextSetter implements Runnable
	{
		private String _text;
		
		public TextSetter(String text)
		{
			_text = text;
		}
		
		@Override
		public void run()
		{
			setLogText(_text);
		}
	}
	
	private class UpdateWorker implements Runnable
	{
		@Override
		public void run()
		{
			InputStream in = null;
			InputStreamReader reader = null;
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter(writer);
			
			try
			{
				QueueManipulator manip = new QueueManipulator(_queue);
				EndpointReferenceType epr = manip.getJobLog(_job);
				in = ByteIOStreamFactory.createInputStream(epr);
				reader = new InputStreamReader(in);
				StreamUtils.copyStream(reader, printer);
			}
			catch (Throwable cause)
			{
				printer.println("Unable to acquire job's log!\n\n");
				cause.printStackTrace(printer);
			}
			
			printer.close();
			SwingUtilities.invokeLater(new TextSetter(writer.toString()));
		}
	}
	
	public LogDisplay(JFrame owner, EndpointReferenceType queue,
		JobTicket job)
	{
		super(owner);
		setTitle("Job Log");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		_queue = queue;
		_job = job;
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		_text = new JTextPane();
		_text.setEditable(false);
		_text.setText("Querying grid queue for job log.");
		
		Dimension d = new Dimension(800, 400);
		_text.setMinimumSize(d);
		_text.setPreferredSize(d);
		JScrollPane scroller = new JScrollPane(_text);
		scroller.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Job Log"));
		content.add(scroller, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new OKAction()),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		Thread th = new Thread(new UpdateWorker());
		th.setName("Job Log Updater Thread");
		th.setDaemon(true);
		th.start();
	}
	
	private void setLogText(String text)
	{
		_text.setText(text);
		_text.setCaretPosition(0);
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public OKAction()
		{
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
}
