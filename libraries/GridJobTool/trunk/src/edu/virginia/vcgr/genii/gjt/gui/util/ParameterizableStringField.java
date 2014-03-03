package edu.virginia.vcgr.genii.gjt.gui.util;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.vcgr.genii.gjt.data.ParameterizableString;

public class ParameterizableStringField extends JTextField {
	static final long serialVersionUID = 0L;

	private ParameterizableString _string;

	public ParameterizableStringField(ParameterizableString string, int columns) {
		super(string.toString(), columns);

		_string = string;

		addCaretListener(new InternalCaretListener());
	}

	private class InternalCaretListener implements CaretListener {
		@Override
		public void caretUpdate(CaretEvent e) {
			_string.set(getText());
		}
	}
}