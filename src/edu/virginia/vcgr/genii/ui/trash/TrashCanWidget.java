package edu.virginia.vcgr.genii.ui.trash;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;

public class TrashCanWidget extends JComponent
{
	static final long serialVersionUID = 0L;

	private ApplicationContext _appContext;
	private UIContext _context;

	public TrashCanWidget(ApplicationContext appContext, UIContext context)
	{
		_appContext = appContext;
		_context = context;

		Dimension size = new Dimension(Math.max(Images.emptyTrashcan().getWidth(), Images.fullTrashcan().getWidth()), Math.max(
			Images.emptyTrashcan().getHeight(), Images.fullTrashcan().getHeight()));

		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);

		_context.trashCan().addTrashCanStateListener(new TrashCanStateListenerImpl());

		setTransferHandler(new TrashCanTransferHandler(context));

		addMouseListener(new MouseClickListener());
	}

	@Override
	protected void paintComponent(Graphics _g)
	{
		Graphics2D g = (Graphics2D) _g.create();

		try {
			int width = getWidth();
			int height = getHeight();
			BufferedImage image = _context.trashCan().isEmpty() ? Images.emptyTrashcan() : Images.fullTrashcan();

			g.drawImage(image, (width - image.getWidth()) / 2, (height - image.getHeight()) / 2, null);
		} finally {
			g.dispose();
		}
	}

	private class TrashCanStateListenerImpl implements TrashCanStateListener
	{
		@Override
		public void trashCanEmptied()
		{
			repaint();
		}

		@Override
		public void trashCanFilled()
		{
			repaint();
		}
	}

	private class MouseClickListener extends MouseAdapter
	{

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
				TrashDialog.popupTrashDialog(TrashCanWidget.this, _appContext, _context);
		}
	}

}