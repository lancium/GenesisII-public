package edu.virginia.vcgr.genii.ui.errors;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import edu.virginia.vcgr.genii.ui.UIContext;

public class ErrorHandler
{
	static public void handleError(UIContext uiContext,
		JComponent responsibleComponent, ClassifiedError cError)
	{
		if (cError.classification() == ErrorClassification.IGNORABLE)
		{
			// Just ignore it
		} else if (cError.classification() == ErrorClassification.UNEXPECTED)
		{
			ReportableErrorHandler.handleError(uiContext,
				responsibleComponent, cError.cause());
		} else
		{
			JOptionPane.showMessageDialog(responsibleComponent,
				cError.reportMessage(), cError.reportTitle(), 
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	static public void handleError(UIContext uiContext,
		JComponent responsibleComponent, Throwable cause)
	{
		handleError(
			uiContext, responsibleComponent, 
			ErrorClassification.classify(cause));
	}
}