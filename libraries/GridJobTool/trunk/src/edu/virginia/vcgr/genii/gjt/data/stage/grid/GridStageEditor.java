package edu.virginia.vcgr.genii.gjt.data.stage.grid;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

class GridStageEditor extends StageEditor<GridStageData>
{
	static final long serialVersionUID = 0L;

	private JTextField _path = new JTextField(32);

	@Override
	protected GridStageData getStageDataImpl()
	{
		GridStageData ret = new GridStageData();
		ret.path(_path.getText());
		return ret;
	}

	GridStageEditor(Window owner)
	{
		super(owner, "Grid Data Stage Editor");
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel("Grid Path"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_path, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		// content.add(new BrowseRNSPathAction(container,"Path to file", _path, "Another Path"),new
		// GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		// GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		JButton okButton = new JButton(createDefaultOKAction());
		getRootPane().setDefaultButton(okButton);
		content.add(ButtonPanel.createHorizontalPanel(okButton, createDefaultCancelAction()), new GridBagConstraints(0, 3, 2,
			1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setInitialData(GridStageData stageData)
	{
		_path.setText(stageData.path());
	}
}