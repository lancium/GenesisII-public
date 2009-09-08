package edu.virginia.vcgr.genii.ui.prefs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.morgan.utils.gui.ButtonPanel;
import org.morgan.utils.gui.GUIUtils;

class UIPreferencesDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static private final Dimension MINIMUM_SIZE =
		new Dimension(300, 300);
	
	private Map<UIPreferenceSet, JPanel> _editors =
		new HashMap<UIPreferenceSet, JPanel>();
	private boolean _cancelled = true;
	
	private UIPreferencesDialog(Window owner,
		Collection<UIPreferenceSet> preferenceSets)
	{
		super(owner);
		setTitle("Preferences");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setMinimumSize(MINIMUM_SIZE);
		tabbedPane.setPreferredSize(MINIMUM_SIZE);
		for (UIPreferenceSet pSet : preferenceSets)
		{
			JPanel editor = pSet.createEditor();
			_editors.put(pSet, editor);
			tabbedPane.addTab(pSet.preferenceSetName(), editor);
		}
		
		add(tabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		add(ButtonPanel.createHorizontalButtonPanel(
			new OKAction(), new CancelAction()), new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("OK");
		}
		
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			_cancelled = false;
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CancelAction()
		{
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			_cancelled = true;
			dispose();
		}
	}
	
	static void launchEditor(Window owner, UIPreferences preferences)
		throws BackingStoreException
	{
		UIPreferencesDialog upd = new UIPreferencesDialog(owner,
			preferences.preferenceSets());
		upd.setModalityType(ModalityType.APPLICATION_MODAL);
		upd.pack();
		GUIUtils.centerWindow(upd);
		upd.setVisible(true);
		if (!upd._cancelled)
		{
			for (UIPreferenceSet pSet : upd._editors.keySet())
			{
				JPanel panel = upd._editors.get(pSet);
				pSet.load(panel);
			}
			
			preferences.store();
		}
	}
}