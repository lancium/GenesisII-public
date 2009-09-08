package edu.virginia.vcgr.genii.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class DefaultJMenuBarFactory implements JMenuBarFactory
{
	protected ApplicationContext _context;
	
	public DefaultJMenuBarFactory(
		ApplicationContext context)
	{
		_context = context;
	}
	
	protected JMenu createFileMenu()
	{
		if (_context.isMacOS())
			return null;
		
		JMenu fileMenu = new JMenu("File");
		
		if (!_context.isMacOS())
		{
			fileMenu.add(new PreferencesAction());
			fileMenu.addSeparator();
			fileMenu.add(new QuitAction());
		}
		
		return fileMenu;
	}
	
	protected JMenu createHelpMenu()
	{
		if (_context.isMacOS())
			return null;
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new AboutAction());
		return helpMenu;
	}
	
	@Override
	public JMenuBar createMenuBar(UIContext uiContext)
	{
		JMenuBar bar = new JMenuBar();
		
		JMenu fileMenu = createFileMenu();
		if (fileMenu != null)
			bar.add(fileMenu);
		
		return bar;
	}
	
	@Override
	public void addHelpMenu(UIContext uiContext, JMenuBar bar)
	{
		JMenu helpMenu = createHelpMenu();
		if (helpMenu != null)
		{
			if (!_context.isMacOS())
				bar.add(Box.createHorizontalGlue());
			bar.add(helpMenu);
		}
	}
	
	private class PreferencesAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private PreferencesAction()
		{
			super("Preferences");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			_context.fireApplicationPreferencesRequested();
		}
	}
	
	private class AboutAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private AboutAction()
		{
			super("About Genesis II Application");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			_context.fireApplicationAboutRequested();
		}
	}
	
	private class QuitAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private QuitAction()
		{
			super("Quit");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			_context.fireApplicationQuitRequested();
		}
	}
}