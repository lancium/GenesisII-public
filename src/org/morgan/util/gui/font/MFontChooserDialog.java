package org.morgan.util.gui.font;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.morgan.utils.gui.GUIUtils;

public class MFontChooserDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private FontModel _model;
	private Font _selectedFont = null;
	
	private MFontChooserDialog(Window ownerWindow, Font initialFont)
	{
		super(ownerWindow, "Font Chooser");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		_model = new DefaultFontModel(initialFont);
		MFontChooserPanel panel = new MFontChooserPanel(_model);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setResizable(false);
		
		content.add(panel, new GridBagConstraints(
			0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new OKAction()), new GridBagConstraints(
			0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(
			1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("OK");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_selectedFont = _model.selectedFont();
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
		final public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
	
	static public Font selectFont(Window owner, Font initialFont)
	{
		MFontChooserDialog dialog = new MFontChooserDialog(owner, initialFont);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		GUIUtils.centerWindow(dialog);
		dialog.setVisible(true);
		return dialog._selectedFont;
	}
}