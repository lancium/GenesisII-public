package edu.virginia.vcgr.genii.ui;

import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.JMenuBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UIFrame extends BasicFrameWindow
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(UIFrame.class);

	private DisposeListenerImpl _disposeListener = new DisposeListenerImpl();
	protected UIContext _uiContext;

	public UIFrame(ApplicationContext context, UIContext uiContext) throws HeadlessException
	{
		super();
		initializeUIFrame(context, uiContext);
	}

	public UIFrame(ApplicationContext context, UIContext uiContext, GraphicsConfiguration gc)
	{
		super(gc);
		initializeUIFrame(context, uiContext);
	}

	public UIFrame(ApplicationContext context, UIContext uiContext, String title, GraphicsConfiguration gc)
	{
		super(title, gc);
		initializeUIFrame(context, uiContext);
	}

	public UIFrame(UIContext uiContext, String title) throws HeadlessException
	{
		super(title, null);
		initializeUIFrame(uiContext.applicationContext(), uiContext);
	}

	public void actuateDispose()
	{
		_uiContext.applicationContext().removeDisposeListener(_disposeListener);
		super.dispose();
	}

	protected JMenuBarFactory getMenuFactory()
	{
		return new DefaultJMenuBarFactory(_uiContext.applicationContext());
	}

	protected void initializeUIFrame(ApplicationContext context, UIContext uiContext)
	{
		_logger.debug("creating UI frame");

		_uiContext = uiContext;
		_uiContext.setApplicationContext(context);
		_uiContext.applicationContext().addDisposeListener(_disposeListener);

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JMenuBarFactory mFactory = getMenuFactory();
		if (mFactory != null) {
			JMenuBar bar = mFactory.createMenuBar(_uiContext);
			if (bar != null)
				setJMenuBar(bar);
		}
	}

	/**
	 * handles sending any pending UI events for all known windows.
	 */
	public static void pumpEventsToWindows()
	{
		while (true) {
			ArrayList<BasicFrameWindow> frames = UIFrame.getFrameList();
			for (BasicFrameWindow fram : frames) {

				try {
					if (fram instanceof ClientApplication)
						((ClientApplication) fram).pulseActivities();
				} catch (Exception e) {
					_logger.error("failed to pulse activities on UI frame", e);
				}
			}

			if (BasicFrameWindow.activeFrames() <= 0) {
				/*
				 * we have found that it's time to leave since there are no frames left (although we really only think we'll see this as zero
				 * and not negative).
				 */
				break;
			}
			try {
				Thread.sleep(42);
			} catch (InterruptedException e) {
				// ignored.
			}
		}
	}

	public UIContext getUIContext()
	{
		return _uiContext;
	}

	@Override
	public void dispose()
	{
		actuateDispose();
	}

	private class DisposeListenerImpl implements DisposeListener
	{
		@Override
		public void disposeInvoked()
		{
			actuateDispose();
		}
	}

	/**
	 * adds a new activity for the UI to take once the main thread calls the pulseActivity method. this method can be implemented by derived
	 * classes but is empty here.
	 */
	public void addActivity(String toAdd)
	{
	}
}
