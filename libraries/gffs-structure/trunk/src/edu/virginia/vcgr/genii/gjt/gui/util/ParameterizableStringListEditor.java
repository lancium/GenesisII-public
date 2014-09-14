package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Dialog.ModalityType;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.gjt.data.ParameterizableStringList;

public class ParameterizableStringListEditor implements DescribedFieldEditor<ParameterizableStringList>
{
	private String _title;
	private String _prompt;

	public ParameterizableStringListEditor(String title, String prompt)
	{
		_title = title;
		_prompt = prompt;
	}

	@Override
	public void edit(JComponent parentComponent, ParameterizableStringList currentValue)
	{
		ParameterizableStringListEditorDialog dialog =
			new ParameterizableStringListEditorDialog(SwingUtilities.getWindowAncestor(parentComponent), _title, _prompt,
				currentValue);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();
		GUIUtils.centerComponent(dialog);
		dialog.setVisible(true);

		Set<String> results = dialog.getResults();
		if (results != null) {
			currentValue.clear();
			for (String value : results)
				currentValue.add(value);
		}
	}
}