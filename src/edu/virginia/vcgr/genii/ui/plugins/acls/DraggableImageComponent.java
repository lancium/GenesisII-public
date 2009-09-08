package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;

class DraggableImageComponent extends JComponent
{
	static final long serialVersionUID = 0L;
	
	private BufferedImage _image;
	
	protected BufferedImage getImage()
	{
		return _image;
	}
	
	protected void setImage(BufferedImage image)
	{
		_image = image;
		repaint();
	}
	
	protected DraggableImageComponent(BufferedImage image)
	{
		_image = image;
		
		Dimension d = new Dimension(_image.getWidth(), _image.getHeight());
		setMinimumSize(d);
		setPreferredSize(d);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		SimpleDragHandler handler = new SimpleDragHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		BufferedImage image = getImage();
		
		Graphics2D gg = (Graphics2D)g;
		if (!isEnabled())
		{
			gg.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.2f));
		}
		
		g.drawImage(image,
			(getWidth() - image.getWidth()) / 2,
			(getHeight() - image.getHeight()) / 2, null);
	}
	
	private class SimpleDragHandler extends MouseInputAdapter
	{
		private MouseEvent _firstMouseEvent = null;
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (isEnabled())
			{
				_firstMouseEvent = e;
				e.consume();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (!isEnabled())
				return;
			
			if(_firstMouseEvent != null)
			{
				e.consume();
				int dx = Math.abs(e.getX() - _firstMouseEvent.getX());
				int dy = Math.abs(e.getY() - _firstMouseEvent.getY());
				if(dx > 5 || dy > 5)
				{
					JComponent c = (JComponent)e.getSource();
					TransferHandler handler = getTransferHandler();
					if (handler != null)
						handler.exportAsDrag(c, _firstMouseEvent,
							TransferHandler.LINK);
					_firstMouseEvent = null;
				}
			}
		}
	}
}
