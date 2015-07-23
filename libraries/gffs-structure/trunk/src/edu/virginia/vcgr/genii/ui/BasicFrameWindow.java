package edu.virginia.vcgr.genii.ui;

import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

public class BasicFrameWindow extends JFrame
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BasicFrameWindow.class);

	// track how many frames have been created. we won't exit until the last one is gone.
	static private ArrayList<BasicFrameWindow> _frameList = new ArrayList<BasicFrameWindow>();

	// tracks last offset used so we can not drop windows all on top of each other.
	static private double xOffset = 0.0;
	static private double yOffset = 0.0;

	// a guess at how large the window positioning offset should be.
	static public double MARCH_IN_X_DIRECTION = 32.0;
	static public double MARCH_IN_Y_DIRECTION = -32.0;

	public BasicFrameWindow() throws HeadlessException
	{
		super();
		initializeFrame();
	}

	public BasicFrameWindow(GraphicsConfiguration gc)
	{
		super(gc);
		initializeFrame();
	}

	public BasicFrameWindow(String title, GraphicsConfiguration gc)
	{
		super(title, gc);
		initializeFrame();
	}

	/**
	 * returns the number of active frame windows that are still running. when this goes to zero, the application should close.
	 */
	public static int activeFrames()
	{
		synchronized (_frameList) {
			return _frameList.size();
		}
	}

	/**
	 * returns the number of active frame windows of the specified type, which can be useful for deciding when a whole class of windows
	 * are closed and the app running them no longer needs to continue.
	 */
	public static int activeFrames(Class<?> typeToFind)
	{
		int count = 0;
		synchronized (_frameList) {
			for (BasicFrameWindow bfw : _frameList) {
				if (typeToFind.isInstance(bfw)) {
					count++;					
				}
			}
			return count;
		}
	}

	/**
	 * returns a copy of the current list of frames. this is not locked, so the real frames could change after the list is returned.
	 */
	@SuppressWarnings("unchecked")
	static public ArrayList<BasicFrameWindow> getFrameList()
	{
		return (ArrayList<BasicFrameWindow>) _frameList.clone();
	}

	protected void initializeFrame()
	{
		synchronized (_frameList) {
			// we're a new frame, so up the count.
			_frameList.add(this);
			_logger.debug("frame count increased to " + _frameList.size());
		}

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * centers the window, but if this is not the first, the next is displayed with an offset from the true center.
	 */
	public void centerWindowAndMarch()
	{
		_logger.debug("current offset: [" + xOffset + ", " + yOffset + "]");
		GuiUtils.centerComponentWithOffset(this, (int) xOffset, (int) yOffset);
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

	@Override
	public void dispose()
	{
		synchronized (_frameList) {
			if (_frameList.contains(this)) {
				_frameList.remove(this);
			} else {
				_logger.error("somehow was asked to destroy frame that's not in our list.");
				return;
			}
			_logger.debug("frame count reduced to " + _frameList.size());
		}
		super.dispose();
	}
}
