package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonPanel extends JPanel {
	static final long serialVersionUID = 0L;

	private Vector<JButton> _buttons;

	private ButtonPanel(boolean horizontal, Object[] buttonInformation) {
		super(new GridBagLayout());

		boolean containsNulls = false;
		_buttons = new Vector<JButton>(buttonInformation.length);

		for (Object buttonInfo : buttonInformation) {
			if (buttonInfo == null) {
				containsNulls = true;
				break;
			}
		}

		int lcv;
		for (lcv = 0; lcv < buttonInformation.length; lcv++) {
			JButton button;

			Object info = buttonInformation[lcv];
			if (info == null)
				button = null;
			else if (info instanceof JButton)
				button = (JButton) info;
			else if (info instanceof Action)
				button = new JButton((Action) info);
			else
				button = new JButton(info.toString());

			if (button != null)
				_buttons.add(button);

			if (horizontal) {
				add(button == null ? new JLabel() : button,
						new GridBagConstraints(lcv, 0, 1, 1,
								(!containsNulls || button == null) ? 1.0 : 0.0,
								1.0, GridBagConstraints.CENTER,
								GridBagConstraints.NONE,
								new Insets(0, 0, 0, 0), 0, 0));
			} else {
				add(button == null ? new JLabel() : button,
						new GridBagConstraints(0, lcv, 1, 1, 1.0,
								(!containsNulls || button == null) ? 1.0 : 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.NONE,
								new Insets(0, 0, 0, 0), 0, 0));
			}
		}
	}

	final public JButton button(int index) {
		return _buttons.get(index);
	}

	static public ButtonPanel createHorizontalPanel(Object... buttonInformation) {
		return new ButtonPanel(true, buttonInformation);
	}

	static public ButtonPanel createVerticalPanel(Object... buttonInformation) {
		return new ButtonPanel(false, buttonInformation);
	}
}