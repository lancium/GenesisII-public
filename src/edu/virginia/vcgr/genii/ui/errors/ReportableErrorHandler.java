package edu.virginia.vcgr.genii.ui.errors;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.widgets.ExpandCollapseButton;

public class ReportableErrorHandler extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(ReportableErrorHandler.class);
	
	static final private Dimension DETAILS_SIZE = new Dimension(
		150, 250);
	
	static private JComponent createDetails(Throwable cause)
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		cause.printStackTrace(pw);
		pw.flush();
		StreamUtils.close(writer);
		
		JTextArea area = new JTextArea(writer.toString());
		area.setEditable(false);
		JScrollPane scroller = new JScrollPane(area);
		scroller.setMinimumSize(DETAILS_SIZE);
		scroller.setPreferredSize(DETAILS_SIZE);
		
		return scroller;
	}
	
	private JComponent _details;
	private Throwable _sendInformation = null;
	
	private ReportableErrorHandler(Window owner, Throwable cause)
	{
		super(owner);
		setTitle("Unexpected Error");
		Container content = getContentPane();
		
		_sendInformation = cause;
		content.setLayout(new GridBagLayout());
		
		_details = createDetails(cause);
		add(new JLabel(new FileResource(
			"edu/virginia/vcgr/genii/ui/errors/error-message.html").toString()),
			new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(ButtonPanel.createHorizontalButtonPanel(new SendAction(),
			new DontSendAction()),
			new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Details"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, 
			new Insets(5, 5, 5, 5), 5, 5));
		add(new ExpandCollapseButton(new DetailsListener()), 
			new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class DetailsListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			boolean isSelected = 
				((JToggleButton)event.getSource()).getModel().isSelected();
			
			if (isSelected)
			{
				add(_details, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 5, 5));
				pack();
			} else
			{
				remove(_details);
				pack();
			}
		}
	}
	
	private class SendAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private SendAction()
		{
			super("Send Report");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
	
	private class DontSendAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private DontSendAction()
		{
			super("Don't Send Report");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_sendInformation = null;
			dispose();
		}
	}
	
	static void handleError(UIContext uiContext,
		JComponent responsibleComponent, Throwable cause)
	{
		ReportableErrorHandler eh = new ReportableErrorHandler(responsibleComponent == null ? null :
			SwingUtilities.getWindowAncestor(responsibleComponent), cause);
		eh.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		eh.setResizable(false);
		eh.pack();
		eh.setLocationRelativeTo(responsibleComponent);
		eh.setModalityType(ModalityType.DOCUMENT_MODAL);
		eh.setVisible(true);
		cause = eh._sendInformation;
		if (cause != null)
		{
			_logger.info(
				"Sending error informationto Genesis II Development Team.", 
				cause);
			OutputStream out = null;
			try
			{
				out = uiContext.openErrorReportStream();
				PrintStream ps = new PrintStream(out);
				cause.printStackTrace(ps);
				ps.flush();
			}
			finally
			{
				StreamUtils.close(out);
			}
		}
	}
}
