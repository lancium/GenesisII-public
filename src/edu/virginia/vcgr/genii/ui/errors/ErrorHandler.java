package edu.virginia.vcgr.genii.ui.errors;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.utils.LoggingTarget;

public class ErrorHandler
{
    static private Log _logger = LogFactory.getLog(ErrorHandler.class);

    static public void handleError(UIContext uiContext, JComponent responsibleComponent,
            ClassifiedError cError)
    {
        if ( (uiContext == null) || (cError == null) ) return;
        if (cError.classification() == ErrorClassification.IGNORABLE) {
            // Just ignore it
        } else if (cError.classification() == ErrorClassification.UNEXPECTED) {
            ReportableErrorHandler.handleError(uiContext, responsibleComponent, cError.cause());
        } else {
            String msg = cError.reportTitle() + ": " + cError.reportMessage() + " -- "
                    + cError.cause().getMessage();
            _logger.error(msg);
            LoggingTarget.logInfo(msg, cError.cause());
        }
    }

    static public void handleError(UIContext uiContext, JComponent responsibleComponent,
            Throwable cause)
    {
        if ( (uiContext == null) || (cause == null) ) return;
        handleError(uiContext, responsibleComponent, ErrorClassification.classify(cause));
    }
}
