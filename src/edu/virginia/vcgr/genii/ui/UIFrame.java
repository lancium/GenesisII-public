package edu.virginia.vcgr.genii.ui;

import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class UIFrame extends JFrame
{
	static final long serialVersionUID = 0L;

	private DisposeListenerImpl _disposeListener = new DisposeListenerImpl();
	
	protected ApplicationContext _context;
	protected UIContext _uiContext;
	
	private void actuateDispose()
	{
		_context.removeDisposeListener(_disposeListener);
		super.dispose();
	}
	
	protected JMenuBarFactory getMenuFactory()
	{
		return new DefaultJMenuBarFactory(_context);
	}
	
	protected void initializeUIFrame(ApplicationContext context, UIContext uiContext)
	{
		_context = context;
		_uiContext = uiContext;
		
		_context.addDisposeListener(_disposeListener);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JMenuBarFactory mFactory = getMenuFactory();
		if (mFactory != null)
		{
			JMenuBar bar = mFactory.createMenuBar(_uiContext);
			if (bar != null)
				setJMenuBar(bar);
		}
	}
	
	public UIFrame(ApplicationContext context, UIContext uiContext) throws HeadlessException
	{
		super();

		initializeUIFrame(context, uiContext);
	}

	public UIFrame(ApplicationContext context, UIContext uiContext, 
		GraphicsConfiguration gc)
	{
		super(gc);

		initializeUIFrame(context, uiContext);
	}

	public UIFrame(ApplicationContext context, UIContext uiContext,
		String title, GraphicsConfiguration gc)
	{
		super(title, gc);

		initializeUIFrame(context, uiContext);
	}

	public UIFrame(UIContext uiContext,
		String title)
		throws HeadlessException
	{
		super(title);

		initializeUIFrame(uiContext.applicationContext(), uiContext);
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