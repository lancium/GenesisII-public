package edu.virginia.vcgr.genii.ui.errors;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.utils.LoggingTarget;

@SuppressWarnings("serial")
public class ReportableErrorHandler extends JDialog
{
	static private Log _logger = LogFactory.getLog(ReportableErrorHandler.class);
	static final private Dimension DETAILS_SIZE = new Dimension(600, 500);
	
	static private JComponent createDetails(Throwable cause)
	{
	    if (cause == null) return null;
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
	String _message; 
	
	private ReportableErrorHandler(Window owner, Throwable cause, String message)
	{
		super(owner);
        if ( (message == null) || (cause == null) ) return;  // bail.
		super.setMinimumSize(new Dimension((int)(DETAILS_SIZE.getWidth() + 40), (int)(DETAILS_SIZE.getHeight() + 30)));
		setTitle("Exception Trace");
		_message = message;
		Container content = getContentPane();		
		content.setLayout(new GridBagLayout());
		_details = createDetails(cause);
        add(new JLabel(_message),
                new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));		
        add(_details, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 5, 5));
	}

	static void handleError(UIContext uiContext,
		JComponent responsibleComponent, Throwable cause)
	{
	    if (cause == null) return;
		String msg = "Unexpected Error: " + cause.getMessage() + " -- " + cause.getMessage(); 
		_logger.error(msg, cause);
		LoggingTarget.logInfo(msg, cause);
	}
	
	public static void displayError(UIContext uiContext, JComponent responsibleComponent,
	        String message, Throwable cause)
	{
	    if ( (uiContext == null) || (message == null) || (cause == null)) return;
		ReportableErrorHandler eh = new ReportableErrorHandler(responsibleComponent == null ? null :
			SwingUtilities.getWindowAncestor(responsibleComponent), cause, message);
		eh.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		eh.setResizable(true);
		eh.pack();
		eh.setLocationRelativeTo(responsibleComponent);
		eh.setModalityType(ModalityType.DOCUMENT_MODAL);
		eh.setVisible(true);
    }
}
