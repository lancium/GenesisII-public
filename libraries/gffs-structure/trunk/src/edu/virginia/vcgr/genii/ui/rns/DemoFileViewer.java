package edu.virginia.vcgr.genii.ui.rns;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;

import java.io.File;
import java.io.FileFilter;

/**
 * demo code: to be tossed out eventually.
 * 
 * This code uses a JList in two forms (layout orientation vertical & horizontal wrap) to display a File[]. The renderer displays the file
 * icon obtained from FileSystemView.
 */
class DemoFileViewer
{

	public Component getGui(File[] all, boolean vertical)
	{
		// put File objects in the list..
		JList<File> fileList = new JList<File>(all);
		// ..then use a renderer
		fileList.setCellRenderer(new FileRenderer(!vertical));

		if (!vertical) {
			fileList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
			fileList.setVisibleRowCount(-1);
		} else {
			fileList.setVisibleRowCount(9);
		}
		return new JScrollPane(fileList);
	}

	static class TextFileFilter implements FileFilter
	{

		public boolean accept(File file)
		{
			// implement the logic to select files here..
			String name = file.getName().toLowerCase();
			// return name.endsWith(".java") || name.endsWith(".class");
			return name.length() < 20;
		}
	}

	static class FileRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private boolean pad;
		private Border padBorder = new EmptyBorder(3, 3, 3, 3);

		FileRenderer(boolean pad)
		{
			this.pad = pad;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			JLabel l = (JLabel) c;
			File f = (File) value;
			l.setText(f.getName());
			l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
			if (pad) {
				l.setBorder(padBorder);
			}

			return l;
		}
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				File f = new File(System.getProperty("user.home"));
				DemoFileViewer fl = new DemoFileViewer();
				Component c1 = fl.getGui(f.listFiles(new TextFileFilter()), true);

				// f = new File(System.getProperty("user.home"));
				Component c2 = fl.getGui(f.listFiles(new TextFileFilter()), false);

				JFrame frame = new JFrame("File List");
				JPanel gui = new JPanel(new BorderLayout());
				gui.add(c1, BorderLayout.WEST);
				gui.add(c2, BorderLayout.CENTER);
				c2.setPreferredSize(new Dimension(375, 100));
				gui.setBorder(new EmptyBorder(3, 3, 3, 3));

				frame.setContentPane(gui);
				frame.pack();
				frame.setLocationByPlatform(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}

}
