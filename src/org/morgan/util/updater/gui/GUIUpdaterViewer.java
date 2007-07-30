/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.updater.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.morgan.util.GraphicsUtils;
import org.morgan.util.Version;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.updater.IUpdateListener;
import org.morgan.util.updater.UpdateManager;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class GUIUpdaterViewer extends JDialog implements IUpdateListener
{
	static final long serialVersionUID = 0;
	
	private JProgressBar _progress;
	private JLabel _status;
	
	public GUIUpdaterViewer()
	{
		setPreferredSize(new Dimension(300, 100));
		setMinimumSize(new Dimension(300, 100));
		
		setTitle("Updater");
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel("Status:  "),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		getContentPane().add( (_status = new JLabel()),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		getContentPane().add( (_progress = new JProgressBar(
			JProgressBar.HORIZONTAL, 0, 10)),
			new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		pack();
	}
	
	public void exceptionOccurred(String msg, IOException ioe)
	{
		JOptionPane.showMessageDialog(this, msg, "Update Error",
			JOptionPane.ERROR_MESSAGE);
	}

	private class StartingUpdateWorker implements Runnable
	{
		private int _filesToUpdate;
		public StartingUpdateWorker(int filesToUpdate)
		{
			_filesToUpdate = filesToUpdate;
		}
		
		public void run()
		{
			startingUpdate(_filesToUpdate);
		}
	}
	
	public void startingUpdate(int filesToUpdate)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new StartingUpdateWorker(filesToUpdate));
			return;
		}
		
		_progress.setMaximum(filesToUpdate);
		_status.setText("Starting Update.");
	}

	public void finishedUpdate()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							finishedUpdate();
						}
					});
			return;
		}
		
		_status.setText("Finished Update.");
	}

	private class StartingFileUpdateWorker implements Runnable
	{
		private String _filename;
		private Version _oldVersion;
		private Version _newVersion;
		
		public StartingFileUpdateWorker(String filename, Version oldV, Version newV)
		{
			_filename = filename;
			_oldVersion = oldV;
			_newVersion = newV;
		}
		
		public void run()
		{
			startingFileUpdate(_filename, _oldVersion, _newVersion);
		}
	}
	
	public void startingFileUpdate(String fileName, Version oldVersion,
			Version newVersion)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new StartingFileUpdateWorker(
				fileName, oldVersion, newVersion));
			return;
		}
		
		_status.setText("Updating file \"" + fileName + "\".");
	}

	private class FinishedFileUpdateWorker implements Runnable
	{
		private String _filename;
		
		public FinishedFileUpdateWorker(String filename)
		{
			_filename = filename;
		}
		
		public void run()
		{
			finishedFileUpdate(_filename);
		}
	}
	
	public void finishedFileUpdate(String fileName)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new FinishedFileUpdateWorker(fileName));
			return;
		}
		
		_progress.setValue(_progress.getValue() + 1);
	}

	public void startingCommit()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							startingCommit();
						}
					});
			return;
		}
		
		_status.setText("Committing Updates.");
	}

	public void finishedCommit()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							finishedCommit();
						}
					});
			return;
		}
		
		_status.setText("Finished.");
	}
	
	static public void main(String []args) throws Exception
	{
		if (args.length != 4)
		{
			System.err.println(
				"USAGE:  GUIUpdaterViewer <inst-path> <base-url> <project> <instance>");
			System.exit(1);
		}
		
		UpdateManager man = new UpdateManager(new GuaranteedDirectory(args[0]),
			args[1], args[2], args[3]);
		GUIUpdaterViewer viewer = new GUIUpdaterViewer();
		GraphicsUtils.centerWindow(viewer);
		man.addUpdateListener(viewer);
		viewer.setVisible(true);
		man.update();
	}
}
