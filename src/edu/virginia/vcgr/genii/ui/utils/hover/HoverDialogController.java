package edu.virginia.vcgr.genii.ui.utils.hover;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JDialog;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class HoverDialogController
{
	static final public int DEFAULT_HOVER_TIME = 1500;
	
	private Rectangle _screenSize;
	private boolean _disposed = false;
	private Timer _timer = null;
	private boolean _exited = true;
	private HoverDialogProvider _provider;
	private Point _lastScreenPoint = null;
	private int _hoverTime;
	
	@Override
	protected void finalize() throws Throwable
	{
		dispose();
	}
	
	public HoverDialogController(Component sourceComponent,
		HoverDialogProvider provider, int hoverTime)
	{
		if (sourceComponent == null)
			throw new IllegalArgumentException(
				"Source component cannot be null.");
		
		if (provider == null)
			throw new IllegalArgumentException(
				"Hover dialog provider cannot be null.");
		
		if (hoverTime < 0)
			throw new IllegalArgumentException(
				"Hover time cannot be negative.");
		
		_screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment(
			).getMaximumWindowBounds();
		
		_provider = provider;
		_hoverTime = hoverTime;
		
		MouseListenerImpl listener = new MouseListenerImpl();
		sourceComponent.addMouseListener(listener);
		sourceComponent.addMouseMotionListener(listener);
		
		Container container = sourceComponent.getParent();
		if (container == null)
		{
			sourceComponent.addHierarchyListener(listener);
		} else
		{
			if (container instanceof JViewport)
				((JViewport)container).addChangeListener(listener);
		}
	}
	
	public HoverDialogController(Component sourceComponent,
		HoverDialogProvider provider)
	{
		this(sourceComponent, provider, DEFAULT_HOVER_TIME);
	}
	
	synchronized final public void dispose()
	{
		if (!_disposed)
		{
			_disposed = true;
			
			if (_timer != null)
			{
				_timer.stop();
				_timer = null;
			}
		}
	}
	
	private class MouseListenerImpl extends MouseAdapter
		implements ActionListener, HierarchyListener, ChangeListener
	{
		private Point _lastViewPosition = null;
		private JDialog _dialog = null;
		
		private void destroyDialog()
		{
			if (_dialog != null)
			{
				_dialog.setVisible(false);
				_dialog = null;
			}
		}
		
		@Override
		final public void mouseExited(MouseEvent e)
		{
			if (_timer != null)
			{
				_timer.stop();
				_timer = null;
			}
			
			_exited = true;
		}

		@Override
		final public void mouseMoved(MouseEvent e)
		{
			_exited = false;
			
			if (_timer != null)
			{
				_timer.stop();
				_timer = null;
			}
			
			destroyDialog();
			
			Point point = e.getPoint();
			
			if (_disposed)
				return;
			
			if (_provider.updatePosition(e.getComponent(), e.getPoint()))
			{
				_lastScreenPoint = new Point(point);
				SwingUtilities.convertPointToScreen(
					_lastScreenPoint, e.getComponent());
				
				_timer = new Timer(_hoverTime, this);
				_timer.start();
			}
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			if (_disposed)
				return;
			
			if (_timer != null)
			{
				_timer.stop();
				_timer = null;
			}
			
			destroyDialog();
			
			if (!_exited)
			{
				JDialog dialog = _provider.dialog();
				dialog.setModalityType(ModalityType.MODELESS);
				dialog.addWindowFocusListener(new DialogDestroy(dialog));
				if (!dialog.isUndecorated())
					dialog.setUndecorated(true);
				dialog.pack();
				
				Point location = new Point(
					_lastScreenPoint.x - 1, _lastScreenPoint.y - 1);
				
				dialog.setLocation(location);
				Rectangle dialogSize = dialog.getBounds();
				int deltaX, deltaY;
				
				deltaX = (int)Math.min(0.0, 
					_screenSize.getMaxX() - dialogSize.getMaxX());
				deltaY = (int)Math.min(0.0,
					_screenSize.getMaxY() - dialogSize.getMaxY());
				
				location.translate(deltaX, deltaY);
				dialog.setLocation(location);
				
				dialog.setVisible(true);
				_dialog = dialog;
			}
		}

		@Override
		public void hierarchyChanged(HierarchyEvent e)
		{
			if ((e.getChangeFlags() & (HierarchyEvent.PARENT_CHANGED)) > 0x0)
			{
				if ((e.getChangeFlags() & (HierarchyEvent.COMPONENT_EVENT_MASK)) > 0x0)
				{
					Container c = e.getChangedParent();
					if (c != null && (c instanceof JViewport))
						((JViewport)c).addChangeListener(this);
				}
			}
		}

		@Override
		public void stateChanged(ChangeEvent e)
		{
			Point p = ((JViewport)e.getSource()).getViewPosition();
			if (_lastViewPosition != null)
			{
				if (!p.equals(_lastViewPosition) && _lastScreenPoint != null)
				{
					Point mousePoint = new Point(_lastScreenPoint);
					SwingUtilities.convertPointFromScreen(mousePoint,
						((JViewport)e.getSource()).getView());
					mouseMoved(new MouseEvent(
						((JViewport)e.getSource()).getView(),
						0, System.currentTimeMillis(), 0x0,
						mousePoint.x, mousePoint.y,
						0, false));
				}
			}
			
			_lastViewPosition = p;
		}
	}
	
	private class DialogDestroy implements WindowFocusListener
	{
		private JDialog _dialog;
		
		private DialogDestroy(JDialog dialog)
		{
			_dialog = dialog;
		}

		@Override
		public void windowGainedFocus(WindowEvent e)
		{
			// Do nothing
		}

		@Override
		public void windowLostFocus(WindowEvent e)
		{
			_dialog.setVisible(false);
			_dialog.removeWindowFocusListener(this);
		}
	}
}