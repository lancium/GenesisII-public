package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.EndpointType;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPlugins;
import edu.virginia.vcgr.genii.ui.rns.RNSIcons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class EventSource extends JLabel
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(EventSource.class);

	static final private Color _highlightColor1 = new Color(
		255, 255, 255, 64);
	static final private Color _highlightColor2 = new Color(
		0, 255, 64, 64);
	
	private UIContext _context;
	private boolean _doHighlight = false;
	private HistoryEventSource _source = null;
	
	private WSName getSourceEndpoint()
	{
		if (_source != null)
		{
			Object identity = _source.identity();
			if (identity != null && identity instanceof WSName)
				return (WSName)identity;
		}
		
		return null;
	}
	
	private void popupMenu(MouseEvent e)
	{
		EndpointReferenceType endpoint = getSourceEndpoint().getEndpoint();
		
		UIPlugins plugins = new UIPlugins(new UIPluginContext(
			_context, this, new StaticEndpointRetriever(endpoint)));
		
		EndpointDescription desc = new EndpointDescription(endpoint);
		ArrayList<EndpointDescription> descriptions =
			new ArrayList<EndpointDescription>(1);
		descriptions.add(desc);
		
		plugins.updateStatuses(descriptions);
		JPopupMenu menu = plugins.createPopupMenu();
		
		if (desc.typeInformation().isRNS())
		{
			RNSAction action = new RNSAction(new RNSPath(endpoint));
			
			if (menu == null)
				menu = new JPopupMenu();
			else
				menu.addSeparator();
			
			menu.add(action);	
		}
		
		if (menu != null)
			menu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (_doHighlight)
		{
			Graphics2D g2 = (Graphics2D)g.create();
			Shape dot = new Rectangle(getWidth(), getHeight());
			
			g2.setPaint(new RadialGradientPaint(
				getWidth() / 2.0f, getHeight() / 2.0f,
				Math.max(getWidth(), getHeight()) / 2.0f,
				new float[] { 0.0f, 1.0f },
				new Color[] { _highlightColor1, _highlightColor2 }));
				
			g2.fill(dot);
			
			g2.dispose();
		}
	}
	
	EventSource(UIContext context)
	{
		_context = context;
		setOpaque(true);
		addMouseListener(new MouseListenerImpl());
	}
	
	void setEventSource(HistoryEventSource source)
	{
		_source = source;
		
		setIcon(null);
		
		if (source == null)
			setText("Event Source: ");
		else
		{
			if (source instanceof SimpleStringHistoryEventSource)
				setText(String.format("Event Source: %s", source.toString()));
			else
				setText(String.format("Event Source: <endpoint>"));
			
			WSName name = getSourceEndpoint();
			if (name != null)
			{
				EndpointType et = EndpointType.determineType(
					name.getEndpoint());
				if (et != null)
				{
					Icon icon = RNSIcons.getIcon(et, false);
					setIcon(icon);
				}
			}
		}
	}
	
	private class MouseListenerImpl implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popupMenu(e);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popupMenu(e);
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			WSName name = getSourceEndpoint();
			
			if (name != null)
			{
				_doHighlight = true;
				repaint();
			}
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			_doHighlight = false;
			repaint();
		}
	}
	
	private class RNSAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private RNSPath _rnsRoot;
		
		private RNSAction(RNSPath rnsRoot)
		{
			super("Browse");
			
			_rnsRoot = rnsRoot;
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			try
			{
				UIContext context = (UIContext)_context.clone();
				context.callingContext().setCurrentPath(_rnsRoot);
				ClientApplication app = new ClientApplication(context, false);
				app.pack();
				GUIUtils.centerComponent(app);
				app.setVisible(true);
			}
			catch (Throwable cause)
			{
				// Ignore
				_logger.info("exception in actionPerformed", cause);
			}
		}
	}
}
