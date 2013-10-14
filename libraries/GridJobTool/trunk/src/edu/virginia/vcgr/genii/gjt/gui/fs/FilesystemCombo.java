package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.gjt.data.FilesystemAssociatedItem;
import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

@SuppressWarnings("rawtypes")
public class FilesystemCombo extends JComboBox
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	public FilesystemCombo(FilesystemMap filesystemMap)
	{
		super(new FilesystemComboBoxModel(filesystemMap));

		setEditable(false);
		setRenderer(new FilesystemBoxRenderer());

		addItemListener(new FilesystemInstantiater(filesystemMap));
	}

	public FilesystemCombo(FilesystemMap filesystemMap, FilesystemAssociatedItem fsItem)
	{
		this(filesystemMap);

		FilesystemType fs = fsItem.getFilesystemType();
		setSelectedItem(fs);

		addItemListener(new ItemListenerImpl(fsItem));
	}

	private class ItemListenerImpl implements ItemListener
	{
		private FilesystemAssociatedItem _item;

		private ItemListenerImpl(FilesystemAssociatedItem item)
		{
			_item = item;
		}

		@Override
		public void itemStateChanged(ItemEvent e)
		{
			FilesystemType selected = (FilesystemType) getSelectedItem();
			if (selected == null)
				return;

			_item.setFilesystemType(selected);
		}
	}

	private class FilesystemInstantiater implements ItemListener
	{
		private FilesystemMap _filesystemMap;
		private FilesystemType _lastValue = FilesystemType.Default;

		private FilesystemInstantiater(FilesystemMap filesystemMap)
		{
			_filesystemMap = filesystemMap;
		}

		@Override
		public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() != ItemEvent.SELECTED)
				return;

			FilesystemType selected = (FilesystemType) getSelectedItem();
			if (selected == null)
				return;

			Filesystem filesystem = _filesystemMap.get(selected);
			if (filesystem == null) {
				filesystem = _filesystemMap.get(SwingUtilities.getWindowAncestor((Component) e.getSource()), selected);
				if (filesystem == null) {
					((JComboBox) e.getSource()).setSelectedItem(_lastValue);
					return;
				}
			}

			_lastValue = selected;
		}
	}
}