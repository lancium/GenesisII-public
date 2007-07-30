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
package org.morgan.util.math.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.morgan.util.math.RationalNumber;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class RationalNumberField extends JTextField
{
	static final long serialVersionUID = 0;
	
	private Color _normalBackground;
	private Color _normalForeground;
	private Color _badBackground;
	private Color _badForeground;
	
	private RationalNumberField(String str)
	{
		super(str);
		
		addCaretListener(new Listener());
		
		_normalBackground = getBackground();
		_normalForeground = getForeground();
		_badBackground = Color.RED;
		_badForeground = Color.BLACK;
	}
	
	public RationalNumberField()
	{
		this("0");
	}
	
	public RationalNumberField(RationalNumber num)
	{
		this(num.toString());
	}
	
	public RationalNumber getValue()
	{
		return RationalNumber.fromString(getText());
	}
	
	private class Listener implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			System.err.println("Action event.");
			try
			{
				getValue();
				
				setBackground(_normalBackground);
				setForeground(_normalForeground);
			}
			catch (NumberFormatException nfe)
			{
				setBackground(_badBackground);
				setForeground(_badForeground);
			}
		}
	}
	
	static public void main(String []args) throws Exception
	{
		JDialog diag = new JDialog();
		diag.getContentPane().setLayout(new GridBagLayout());

		diag.getContentPane().add(new RationalNumberField(),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		diag.pack();
		diag.setVisible(true);
	}
}
