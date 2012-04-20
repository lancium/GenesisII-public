package edu.virginia.vcgr.genii.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import edu.virginia.vcgr.genii.ui.errors.ReportableErrorHandler;
import edu.virginia.vcgr.genii.ui.utils.ILoggingRecipient;
import edu.virginia.vcgr.genii.ui.utils.LoggingTarget;

/**
 * Connects a JList object up as a recipient of logging messages.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class LoggingLinkage implements ILoggingRecipient
{
	private JList _appLoggingTarget;
	public final String _exceptionNestMessage = "; nested exception is:";
	public final String _patternFodder = ".*" + _exceptionNestMessage + ".*";
	
	LoggingLinkage(JList useAsTarget) {
	    if (useAsTarget == null) return;
		_appLoggingTarget = useAsTarget;
		LoggingTarget.setTarget(this);
		useAsTarget.addMouseListener(new LoggingListMouser());
	}
	
	/**
	 * extremely limited list model for our logging here.
	 * this does not support anything besides appends right now.
	 */
	@SuppressWarnings("serial")
    public static class LoggingListModel extends DefaultListModel
	{
	    // exception causes per element in the main list.
	    private ArrayList<Throwable> causes = new ArrayList<Throwable>();
	    
	    public ArrayList<Throwable> getCauses() { return causes; }
	    
	    public void addWithCause(int index, Object element, Throwable cause) {
	        causes.add(cause);
	        super.add(index, element);
	    }
	}
	
    private class LoggingListMouser extends MouseAdapter
    {
        public void mouseClicked(MouseEvent me)
        {
            if (_appLoggingTarget == null) return;
            JList target = (JList) me.getComponent();
            // we only do double clicks here.
            if (me.getClickCount() == 2) {
                int indies[] = target.getSelectedIndices();
                if (target.getModel() instanceof LoggingListModel) {
                    LoggingListModel model = (LoggingListModel)target.getModel();
                    if (target.getTopLevelAncestor() instanceof ClientApplication) {
                        ClientApplication parent = (ClientApplication)target.getTopLevelAncestor(); 
                        LoggingListModel lm = (LoggingListModel)_appLoggingTarget.getModel();
                        ReportableErrorHandler.displayError(parent.getContext(), target,
                                model.get(indies[0]).toString(), lm.getCauses().get(indies[0]));
                    }
                }
                me.consume();
            }
        }
    }

	@Override
	public boolean consumeLogging(String toConsume, Throwable cause) {
		if (_appLoggingTarget == null) return false;
		// locate any extended noisy messages, and fix message to include just the portion before nested errors.
		Pattern p = Pattern.compile(_patternFodder, Pattern.DOTALL);
		if (p.matcher(toConsume).matches()) {
			int posn = toConsume.indexOf(_exceptionNestMessage);
			toConsume = toConsume.substring(0, posn - 1);
		}
		if (_appLoggingTarget.getModel() instanceof LoggingListModel) {
		    LoggingListModel lm = (LoggingListModel)_appLoggingTarget.getModel();
			lm.addWithCause(lm.getSize(), (new Date()).toString() + ": " + toConsume + "\n", cause);
			_appLoggingTarget.setSelectedIndex(lm.getSize() - 1);
			_appLoggingTarget.ensureIndexIsVisible(_appLoggingTarget.getSelectedIndex());
		}
		return true;
	}
	
}
