package edu.virginia.vcgr.genii.ui.prefs.history;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.ui.plugins.queue.history.LevelIcon;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboBox;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboSort;

class PreferredLevelPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private EnumComboBox<HistoryEventLevel> _combo;
	
	PreferredLevelPanel(HistoryEventLevel level)
	{
		super(new GridBagLayout());
		
		_combo = new EnumComboBox<HistoryEventLevel>(
			HistoryEventLevel.class, 
			EnumComboSort.ByOrdinal, false, LevelIcon.ICON_MAP);
		_combo.setSelectedItem(level);
		
		add(_combo, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Preferred Level"));
	}
	
	final HistoryEventLevel selectedItem()
	{
		return (HistoryEventLevel)_combo.getSelectedItem();
	}
}