package edu.virginia.vcgr.genii.ui;

import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;

public class UIFrame extends JFrame
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(UIFrame.class);

	private DisposeListenerImpl _disposeListener = new DisposeListenerImpl();
	protected UIContext _uiContext;

	// track how many frames have been created. we won't exit until the last one is gone.
	static private Integer _uiFrameCount = 0;

	// tracks last offset used so we can not drop windows all on top of each other.
	static private double xOffset = 0.0;
	static private double yOffset = 0.0;

	// a guess at how large the window positioning offset should be.
	static public double MARCH_IN_X_DIRECTION = 32.0;
	static public double MARCH_IN_Y_DIRECTION = -32.0;

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
		super(title);

		initializeUIFrame(uiContext.applicationContext(), uiContext);
	}

	/**
	 * returns the number of active frame windows that are still running. when this goes to zero, the application should close.
	 */
	public static int activeFrames()
	{
		synchronized (_uiFrameCount) {
			return _uiFrameCount;
		}
	}

	private void actuateDispose()
	{
		_uiContext.applicationContext().removeDisposeListener(_disposeListener);
		super.dispose();
		synchronized (_uiFrameCount) {
			// checking out now, since this is the last frame window.
			_uiFrameCount--;
			_logger.debug("UIFrame count reduced to " + _uiFrameCount);
		}
	}

	protected JMenuBarFactory getMenuFactory()
	{
		return new DefaultJMenuBarFactory(_uiContext.applicationContext());
	}

	protected void initializeUIFrame(ApplicationContext context, UIContext uiContext)
	{
		synchronized (_uiFrameCount) {
			// we're a new frame, so up the count.
			_uiFrameCount++;
			_logger.debug("UIFrame count increased to " + _uiFrameCount);
		}

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
	 * centers the window, but if this is not the first, the next is displayed with an offset from the true center.
	 */
	public void centerWindowAndMarch()
	{
		_logger.debug("current offset: [" + xOffset + ", " + yOffset + "]");
		GUIUtils.centerComponentWithOffset(this, (int) xOffset, (int) yOffset);
		_logger.debug("updated offset before checking: [" + xOffset + ", " + yOffset + "]");
		xOffset += MARCH_IN_X_DIRECTION;
		yOffset += MARCH_IN_Y_DIRECTION;

		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		_logger.debug("got a rectangle for the usable bounds of: " + bounds);

		Rectangle winSize = this.getBounds();
		_logger.debug("got a rectangle for the window size of: " + bounds);

		double usableWidth = bounds.getWidth() / 2.0 - winSize.getWidth() / 2.0;
		double usableHeight = bounds.getHeight() / 2.0 - winSize.getHeight() / 2.0;
		_logger.debug("caculated usable offset region as [" + usableWidth + ", " + usableHeight + "]");

		/* we use 8 as a fudge factor to give us breathing room at the viewport edges. */
		double fud = 8.0;

		if (xOffset >= usableWidth - fud) {
			// reset the xOffset to the earliest place that makes sense.
			MARCH_IN_X_DIRECTION *= -1.0;
			xOffset += MARCH_IN_X_DIRECTION * 2.0;
			_logger.debug("had to bounce xOffset from right edge to " + xOffset);
		} else if (xOffset <= -usableWidth + fud) {
			MARCH_IN_X_DIRECTION *= -1.0;
			xOffset += MARCH_IN_X_DIRECTION * 2.0;
			_logger.debug("had to bounce xOffset from left edge to " + xOffset);
		}

		if (yOffset >= usableHeight - fud) {
			// reset the yOffset to the earliest place that makes sense.
			MARCH_IN_Y_DIRECTION *= -1.0;
			yOffset += MARCH_IN_Y_DIRECTION * 2.0;
			_logger.debug("had to bounce yOffset from bottom edge to " + yOffset);
		} else if (yOffset <= -usableHeight + fud) {
			MARCH_IN_Y_DIRECTION *= -1.0;
			yOffset += MARCH_IN_Y_DIRECTION * 2.0;
			_logger.debug("had to bounce yOffset from top edge to " + yOffset);
		}
		_logger.debug("new next offset after validation: [" + xOffset + ", " + yOffset + "]");
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
		public void dispose()
		{
			actuateDispose();
		}
	}
}
