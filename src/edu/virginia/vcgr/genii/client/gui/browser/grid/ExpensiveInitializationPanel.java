package edu.virginia.vcgr.genii.client.gui.browser.grid;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public abstract class ExpensiveInitializationPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	protected ExpensiveInitializationPanel()
	{
		super(new GridBagLayout());
		
		add(new JLabel("Initializating panel..."),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
	
		addComponentListener(new LazyResolver());
	}
	
	protected abstract Component resolveComponent() throws Throwable;
	
	private void switchComponent(Component newComponent)
	{
		removeAll();
		add(newComponent,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		revalidate();
	}
	
	private class LazyResolver extends ComponentAdapter
	{
		public void componentShown(ComponentEvent evt)
		{
			removeComponentListener(this);
			
			Thread th = new Thread(new ComponentResolver());
			th.setDaemon(true);
			th.setName("Component Resolver");
			th.start();
		}
	}
	
	static private Component createErrorDisplay(Throwable cause)
	{
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		
		printer.println("Error attempting to resolve component.");
		printer.println();
		cause.printStackTrace(printer);
		printer.flush();
		
		JTextArea textArea = new JTextArea(writer.toString());
		textArea.setEditable(false);
		
		return new JScrollPane(textArea);
	}
	
	private class ComponentResolver implements Runnable
	{
		public void run()
		{
			Component c;
			
			try
			{
				c = resolveComponent();
			}
			catch (Throwable cause)
			{
				c = createErrorDisplay(cause);
			}
			
			if (SwingUtilities.isEventDispatchThread())
				switchComponent(c);
			else
			{
				SwingUtilities.invokeLater(new ComponentSwitcher(c));
			}
		}
	}
	
	private class ComponentSwitcher implements Runnable
	{
		private Component _newComponent;
		
		public ComponentSwitcher(Component newComponent)
		{
			_newComponent = newComponent;
		}
		
		public void run()
		{
			switchComponent(_newComponent);
		}
	}
}