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

/**
 * The ExpensiveInitializationPanel class is a JPanel component that makes it
 * easy for developers to write panels which get filled in with information
 * that may take a long time to fill in (for example, because outcalls to
 * the grid are involved).  This component is meant to be used mostly by
 * plugins that are implementing the ITabPlugin interface.
 * 
 * @author mmm2a
 */
public abstract class ExpensiveInitializationPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	/**
	 * Construct a new ExpensiveInitializationPanel panel.
	 */
	protected ExpensiveInitializationPanel()
	{
		super(new GridBagLayout());
		
		add(new JLabel("Initializating panel..."),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
	
		addComponentListener(new LazyResolver());
	}
	
	/**
	 * This is the main abstract method that a derived class must implement.
	 * This operation is called exactly once each time the plugin creates
	 * a new tab component, and then only after the user first tries to view
	 * the component.  In the mean time, a message indicating that the panel is
	 * being initialized will be displayed.
	 * 
	 * @return The GUI component to display in the tab.
	 * 
	 * @throws Throwable
	 */
	protected abstract Component resolveComponent() throws Throwable;
	
	/**
	 * An internal method to remove the "initializing" message and replace
	 * 
	 *it with the real component once the initialization is done.
	 * 
	 * @param newComponent The new component that the user finally resolved
	 * and wants displayed.
	 */
	private void switchComponent(Component newComponent)
	{
		/*
		 * Remove all previous components and add the new one instead
		 */
		removeAll();
		add(newComponent,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		/*
		 * We have to revalidate the panel so that it will be re-drawn
		 */
		revalidate();
	}
	
	/**
	 * This internal class is used as a component listener which is called when
	 * the component is first shown (it will, at that time, have a message
	 * indicating that it is being initialized).  After this listener is first
	 * called, it de-registers itself from the listern queue as it will no-longer
	 * need to be called.
	 * 
	 * @author mmm2a
	 */
	private class LazyResolver extends ComponentAdapter
	{
		@Override
		public void componentShown(ComponentEvent evt)
		{
			/* un-register ourselves so we don't receive further notifications
			 * when the component is shown to the user.
			 */
			removeComponentListener(this);
			
			/*
			 * Start up a new thread to actually do whatever work it is that
			 * the user needs to do in order to create the real component
			 * display. 
			 */
			Thread th = new Thread(new ComponentResolver());
			th.setDaemon(true);
			th.setName("Component Resolver");
			th.start();
		}
	}
	
	/**
	 * An internal method that is called when an exception occurred trying
	 * to create the user's display component.  This will essentially get
	 * displayed instead of the user's component and will consist of a text
	 * area with the stack trace included in it.
	 * 
	 * @param cause The exception that caused the error.
	 * 
	 * @return The text component to display.
	 */
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
	
	/**
	 * This runnable is used internal to call into the user's derived class
	 * code to resolve the component that he or she wants displayed.
	 * 
	 * @author mmm2a
	 */
	private class ComponentResolver implements Runnable
	{
		@Override
		public void run()
		{
			Component c;
			
			try
			{
				/* First, ask the derived classes to resolve the component. */
				c = resolveComponent();
			}
			catch (Throwable cause)
			{
				/* If anything goes wrong, display the exception instead in a
				 * text display.
				 */
				c = createErrorDisplay(cause);
			}
			
			/*
			 * We need to update the panel with the new component, but that can
			 * only be done in the event dispatch thread.  If we are already
			 * in that thread (which is probably never going to be the case),
			 * go ahead and call through to the "swizzler" code.
			 */
			if (SwingUtilities.isEventDispatchThread())
				switchComponent(c);
			else
			{
				/* If we weren't on the event dispatch thread, create a new
				 * "runnable" to call the "swizzler" code and enqueue it onto
				 * the event dispatch thread's event queue.
				 */
				SwingUtilities.invokeLater(new ComponentSwitcher(c));
			}
		}
	}
	
	/**
	 * In the case where the code swizzling needs to be done but the caller
	 * wasn't in the event dispatch thread, we use this runnable class to
	 * enqueue a request into the event dispatch queue.
	 * 
	 * @author mmm2a
	 */
	private class ComponentSwitcher implements Runnable
	{
		private Component _newComponent;
		
		/**
		 * Create a new component switcher with the new component to replace
		 * the old one with.
		 * 
		 * @param newComponent
		 */
		public ComponentSwitcher(Component newComponent)
		{
			_newComponent = newComponent;
		}
		
		@Override
		public void run()
		{
			/* Just call through to the component "swizzler".  We should now
			 * be on the correct thread.
			 */
			switchComponent(_newComponent);
		}
	}
}
