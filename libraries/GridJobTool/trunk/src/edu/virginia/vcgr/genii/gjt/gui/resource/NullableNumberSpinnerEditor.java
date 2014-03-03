package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JSpinner.DefaultEditor;

public class NullableNumberSpinnerEditor extends DefaultEditor {
	static final long serialVersionUID = 0L;

	static private class NumberFormatterFactory extends
			AbstractFormatterFactory {
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf) {
			return new NullableNumberFormatter();
		}
	}

	static private class NullableNumberFormatter extends AbstractFormatter {
		static final long serialVersionUID = 0L;

		@Override
		public Object stringToValue(String text) throws ParseException {
			text = text.trim();
			if (text.equals(""))
				return new NullableNumber();
			else
				return new NullableNumber(Long.parseLong(text));
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			return value.toString();
		}
	}

	public NullableNumberSpinnerEditor(JSpinner spinner) {
		super(spinner);
	}

	@Override
	public JFormattedTextField getTextField() {
		JFormattedTextField ret = super.getTextField();
		ret.setFormatterFactory(new NumberFormatterFactory());
		ret.setEditable(true);
		ret.setHorizontalAlignment(JTextField.RIGHT);
		return ret;
	}

	@Override
	public void commitEdit() throws ParseException {
		Long value;
		String text = getTextField().getText().trim();

		if (text.equals(""))
			value = null;
		else
			value = Long.parseLong(text);

		getSpinner().getModel().setValue(new NullableNumber(value));
	}
}
