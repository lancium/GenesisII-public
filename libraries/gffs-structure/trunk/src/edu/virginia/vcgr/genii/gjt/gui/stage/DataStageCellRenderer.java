package edu.virginia.vcgr.genii.gjt.gui.stage;

import javax.swing.table.DefaultTableCellRenderer;

import edu.virginia.vcgr.genii.gjt.data.stage.DataStage;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class DataStageCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void setValue(Object value)
	{
		DataStage stage = (DataStage) value;

		if (stage != null) {
			StageData stageData = stage.current();
			if (stageData != null && stageData.protocol() != StageProtocol.undefined) {
				setText(stageData.toString());
				setToolTipText(stageData.toString());
			} else
				setText(null);
		} else
			setText(null);

		setIcon(null);
	}
}