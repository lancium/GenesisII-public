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
package org.morgan.util.gui.date;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.morgan.util.gui.ArrowDirection;
import org.morgan.util.gui.BasicTriangleArrowIcon;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class DateDialog extends JDialog
{
	static final long serialVersionUID = 0;

	private DateDialogModel _model;
	
	private MonthComponent _month;
	private JButton _leftMonthArrow;
	private JButton _rightMonthArrow;
	private JLabel _monthLabel;
	private JButton _todayButton;

	public DateDialog()
	{
		this(new DateDialogModel());
	}
	
	public DateDialog(DateDialogModel model)
	{
		_model = model;
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		Controller controller = new Controller();
		model.addDateModelListener(controller);
		
		_leftMonthArrow = new JButton(
			new BasicTriangleArrowIcon(ArrowDirection.LEFT, 10));
		_monthLabel = new JLabel("");
		_rightMonthArrow = new JButton(
			new BasicTriangleArrowIcon(ArrowDirection.RIGHT, 10));
		_month = new MonthComponent(_model.getTodaysDate(), 
			_model.getSelectedDate(), 
			_model.getSelectedMonth());
		_todayButton = new JButton("Today");
	
		Dimension d = new Dimension(120, 10);
		_monthLabel.setMinimumSize(d);
		_monthLabel.setPreferredSize(d);
		_monthLabel.setHorizontalAlignment(JLabel.CENTER);
		
		getContentPane().setLayout(new GridBagLayout());
		
		add(_leftMonthArrow, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_monthLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_rightMonthArrow, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_month, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_todayButton, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		
		_leftMonthArrow.addActionListener(controller);
		_rightMonthArrow.addActionListener(controller);
		_todayButton.addActionListener(controller);
		_month.addMouseListener(controller);
		
		setMonthLabel();
	}
	
	public Date getSelectedDate()
	{
		return _model.getSelectedDate().getTime();
	}
	
	public DateDialogModel getModel()
	{
		return _model;
	}
	
	private void setMonthLabel()
	{
		Calendar month = _model.getSelectedMonth();
		SimpleDateFormat formatter = new SimpleDateFormat("MMMMMMMMMM yyyy");
		
		_monthLabel.setText(formatter.format(month.getTime()));
	}
	
	private class Controller extends MouseAdapter 
		implements IDateDialogModelListener, ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			Object source = event.getSource();
			
			if (source == _leftMonthArrow)
				_model.rollMonth(false);
			else if (source == _rightMonthArrow)
				_model.rollMonth(true);
			else if (source == _todayButton)
			{
				_model.selectDate(_model.getTodaysDate().getTime());
				setVisible(false);
			}
		}
		
		public void selectedDateChange(Calendar newDate)
		{
			_month.selectDate(newDate);
		}
		
		public void selectedMonthChange(Calendar newMonth)
		{
			setMonthLabel();
			_month.showMonth(newMonth);
		}
		
		public void mouseClicked(MouseEvent me)
		{
			if (me.getButton() == MouseEvent.BUTTON1)
			{
				Calendar d = _month.getDate(me.getX(), me.getY());
				if (d != null)
				{
					_model.selectDate(d.getTime());
					setVisible(false);
				}
			}
		}
	}
	
	static public Date selectDate(Date initiallySelected)
	{
		DateDialog d = new DateDialog();
		if (initiallySelected != null)
			d.getModel().selectDate(initiallySelected);
		d.pack();
		d.setModal(true);
		d.setVisible(true);
		return d.getSelectedDate();
	}
	
	static public void main(String []args)
	{
		System.err.println("Value:  " + selectDate(
			new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 5))));
	}	
}
