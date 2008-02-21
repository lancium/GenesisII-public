package org.morgan.util.gui;

import javax.swing.JFileChooser;

public class FileChooserHelper
{
	static final private long _DEFAULT_TIMEOUT = 1000 * 10;
	
	static public JFileChooser createFileChooser()
	{
		Thread thread = null;
		FileChooserCreator creator = new FileChooserCreator();
		thread = new Thread(creator, "File Chooser Creator.");
		thread.setDaemon(true);
		synchronized(creator)
		{
			thread.start();
			try
			{
				creator.wait(_DEFAULT_TIMEOUT);
			}
			catch (InterruptedException ie)
			{
			}
			
			JFileChooser chooser = creator.getChooser();
			if (chooser == null)
			{
				thread.interrupt();
				thread.stop();
			}
			
			return chooser;
		}
	}
	
	static private class FileChooserCreator implements Runnable
	{
		private JFileChooser _chooser = null;
		
		public void run()
		{
			_chooser = new JFileChooser();
			synchronized(this)
			{
				notifyAll();
			}
		}
		
		public JFileChooser getChooser()
		{
			return _chooser;
		}
	}
}