/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class ColorCodedTextField extends JTextField 
	implements CaretListener
{
	static final long serialVersionUID = 0;
	
	private boolean _isGood;
	private Color _originalColor;
	private Color _badColor;
	private Pattern _goodPattern;
	
	public ColorCodedTextField(Color badColor, 
		Pattern goodPattern)
	{
		super();
		
		_isGood = true;
		_originalColor = getForeground();
		_badColor = badColor;
		_goodPattern = goodPattern;
		
		addCaretListener(this);
	}
	
	public void caretUpdate(CaretEvent ce)
	{
		Matcher m = _goodPattern.matcher(getText());
		if (!_isGood && m.matches())
		{
			_isGood = true;
			setForeground(_originalColor);
		} else if (_isGood && !m.matches())
		{
			_isGood = false;
			setForeground(_badColor);
		}
	}
	
	public boolean isMatching()
	{
		return _isGood;
	}
	
	static public void main(String []args)
	{
		JDialog d = new JDialog();
		d.getContentPane().setLayout(new GridBagLayout());
		d.getContentPane().add(new JLabel("Test"),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		d.getContentPane().add(new ColorCodedTextField(Color.RED,
			Pattern.compile("[0-9]*")),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, 
				GridBagConstraints.HORIZONTAL, 
				new Insets(5, 5, 5, 5), 5, 5));
		d.pack();
		d.setVisible(true);
	}
}
