package org.morgan.util.gui.font;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.morgan.util.gui.TitledPanel;

public class MFontChooserPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	static private JComponent createFontFamilyList(
		FontModel model)
	{
		final Dimension DEFAULT_SIZE = new Dimension(200, 100);
		
		FontFamilyList list = new FontFamilyList(model);
		JScrollPane scroller = new JScrollPane(list);
		scroller.setMinimumSize(DEFAULT_SIZE);
		scroller.setPreferredSize(DEFAULT_SIZE);
		
		return new TitledPanel("Font Family", scroller);
	}
	
	private JComponent createFontStylePanel()
	{
		JCheckBox bold = new JCheckBox(new StyleAction("Bold", Font.BOLD));
		JCheckBox italic = new JCheckBox(new StyleAction(
			"Italic", Font.ITALIC));
		
		bold.setSelected(_model.selectedFont().isBold());
		italic.setSelected(_model.selectedFont().isItalic());
		
		TitledPanel panel = new TitledPanel("Font Style");
		panel.add(bold, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(italic, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		return panel;
	}
	
	private JComponent createFontSizePanel()
	{
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(
			_model.selectedFont().getSize(), _model.minimumSize(),
			_model.maximumSize(), 1));
		
		spinner.addChangeListener(new SizeChangeListener());
		
		TitledPanel panel = new TitledPanel("Font Size", spinner);
		return panel;
	}
	
	private JComponent createFontSamplePanel()
	{
		_sample = new FontSample(_model);
		
		TitledPanel panel = new TitledPanel("Sample");
		panel.add(_sample, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		return panel;
	}
	
	private FontModel _model;
	private FontSample _sample;
	
	public MFontChooserPanel(FontModel model)
	{
		super(new GridBagLayout());
		
		if (model == null)
			model = new DefaultFontModel();
		
		_model = model;
		
		add(createFontFamilyList(model), new GridBagConstraints(
			0, 0, 1, 3, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(createFontStylePanel(), new GridBagConstraints(
			1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(createFontSizePanel(), new GridBagConstraints(
			1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(createFontSamplePanel(), new GridBagConstraints(
			1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	public MFontChooserPanel()
	{
		this(null);
	}
	
	public void setSampleText(String sampleText)
	{
		_sample.setText(sampleText);
	}
	
	private class StyleAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private int _style;
		
		private StyleAction(String name, int style)
		{
			super(name);
			
			_style = style;
			
			setEnabled((_model.styleMask() & style) > 0x0);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			JToggleButton button = (JToggleButton)e.getSource();
			
			if (button.isSelected())
				_model.setStyle(_style);
			else
				_model.clearStyle(_style);
		}
	}
	
	private class SizeChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			JSpinner spinner = (JSpinner)e.getSource();
			_model.setSize((Integer)spinner.getValue());
		}
	}
	
	static public void main(String []args)
	{
		JDialog dialog = new JDialog();
		dialog.setTitle("Font Chooser");
		Container content = dialog.getContentPane();
		content.setLayout(new GridBagLayout());
		content.add(new MFontChooserPanel(), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		dialog.pack();
		dialog.setVisible(true);
	}
}