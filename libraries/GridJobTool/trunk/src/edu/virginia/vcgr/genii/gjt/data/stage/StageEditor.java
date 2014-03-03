package edu.virginia.vcgr.genii.gjt.data.stage;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;

public abstract class StageEditor<GenericType extends StageData> extends JDialog
{
	static final long serialVersionUID = 0L;

	private boolean _cancelled = true;

	protected abstract GenericType getStageDataImpl();

	protected Action createDefaultOKAction()
	{
		return new OKAction();
	}

	protected Action createDefaultCancelAction()
	{
		return new CancelAction();
	}

	final protected void accept()
	{
		_cancelled = false;
		dispose();
	}

	final protected void cancel()
	{
		_cancelled = true;
		dispose();
	}

	protected StageEditor(Window owner, String title)
	{
		super(owner);
		setTitle(title);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public abstract void setInitialData(GenericType stageData);

	final public StageData getStageData()
	{
		if (_cancelled)
			return null;

		return getStageDataImpl();
	}

	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private OKAction()
		{
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			accept();
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			cancel();
		}
	}
}