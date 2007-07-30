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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;

import javax.swing.JComponent;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
class MonthComponent extends JComponent
{
	static final String []_LETTER_ARRAY = new String[] 
	 {
		"S", "M", "T", "W", "T", "F", "S"
	 };
	
	static final long _MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
	static final long serialVersionUID = 0;
	
	static private final int _CELLS_PER_ROW = 7;
	static private final int _CELLS_PER_COL = 9;
	static private final int _MIN_CELL_WIDTH = 20;
	static private final int _MIN_CELL_HEIGHT = 20;
	
	static private int _cellWidth;
	static private int _cellHeight;
	
	private Calendar _firstDisplayedDate;
	private Calendar _today;
	private Calendar _selected;
	private Calendar _viewed;
	
	MonthComponent(Calendar today, Calendar selected, Calendar viewedMonth)
	{
		_today = today;
		_selected = selected;
		_viewed = viewedMonth;
		calculateFirstDisplayedDate();
		
		Dimension size = new Dimension(_MIN_CELL_WIDTH * _CELLS_PER_ROW, 
			_MIN_CELL_HEIGHT * _CELLS_PER_COL);
		setPreferredSize(size);
		setMinimumSize(size);
	}
	
	public void selectDate(Calendar c)
	{
		_selected = (Calendar)c.clone();
		repaint();
	}
	
	public void showMonth(Calendar month)
	{
		_viewed = (Calendar)month.clone();
		calculateFirstDisplayedDate();
		repaint();
	}
	
	public void setSize(Dimension d)
	{
		super.setSize(d);
		
		_cellWidth = d.width / _CELLS_PER_ROW;
		_cellHeight = d.height / _CELLS_PER_COL;
	}
	
	public void setSize(int x, int y)
	{
		super.setSize(x, y);
		
		_cellWidth = x / _CELLS_PER_ROW;
		_cellHeight = y / _CELLS_PER_COL;
	}
	
	public void setBounds(Rectangle bounds)
	{
		super.setBounds(bounds);
		
		_cellWidth = bounds.width / _CELLS_PER_ROW;
		_cellHeight = bounds.height / _CELLS_PER_COL;
	}
	
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		
		_cellWidth = width / _CELLS_PER_ROW;
		_cellHeight = height / _CELLS_PER_COL;
	}
	
	final private void calculateFirstDisplayedDate()
	{
		_firstDisplayedDate = (Calendar)_viewed.clone();
		_firstDisplayedDate.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = _firstDisplayedDate.get(Calendar.DAY_OF_WEEK);
		int daysOff = dayOfWeek - Calendar.SUNDAY;
		rollDay(_firstDisplayedDate, -1 * daysOff);
	}
	
	final private Point getCellUL(int row, int col)
	{
		return new Point(col * _cellWidth, _cellHeight * row);
	}
	
	final private void rollDay(Calendar c, int days)
	{
		c.setTimeInMillis(c.getTimeInMillis() + (days * _MILLIS_PER_DAY));
	}
	
	final private void paintCenteredString(Graphics2D g, FontMetrics metrics,
		Point ul, int cellWidth, int cellHeight, String str)
	{
		Rectangle2D sbounds = metrics.getStringBounds(str, g);
		ul.x += (cellWidth - sbounds.getWidth()) / 2;
		ul.y += (cellHeight - sbounds.getHeight()) / 2 + sbounds.getHeight();
		g.drawString(str, ul.x, ul.y);
	}
	
	final private boolean sameDay(Calendar test, Calendar orig)
	{
		if (test.get(Calendar.DAY_OF_YEAR) != orig.get(Calendar.DAY_OF_YEAR))
			return false;
		return test.get(Calendar.YEAR) == orig.get(Calendar.YEAR);
	}
	
	protected void paintComponent(Graphics _g)
	{
		Graphics2D g = (Graphics2D)_g;
		
		Rectangle bounds = getBounds();
		
		g.drawLine(0, _cellHeight / 2 + _cellHeight, 
			bounds.width - 1, _cellHeight / 2 + _cellHeight);
		g.drawLine(0, bounds.height - 1 - (_cellHeight / 2),
			bounds.width - 1, bounds.height - 1 - (_cellHeight / 2));
		
		Calendar firstDayOfMonth = (Calendar)_viewed.clone();
		firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
		Calendar current = (Calendar)_firstDisplayedDate.clone();
		
		Font f = g.getFont();
		FontMetrics metrics = g.getFontMetrics();
		
		g.setFont(f.deriveFont(Font.BOLD));
		for (int col = 0; col < _CELLS_PER_ROW; col++)
		{
			Point ul = getCellUL(0, col);
			String str = _LETTER_ARRAY[col];
			paintCenteredString(g, metrics, ul, _cellWidth, _cellHeight, str);
		}
		
		boolean inReal = false;
		boolean ignore = true;
		g.setFont(f.deriveFont(Font.ITALIC));
		g.setColor(Color.GRAY);
		for (int row = 2; row < _CELLS_PER_COL - 1; row++)
		{
			for (int col = 0; col < _CELLS_PER_ROW; col++)
			{
				Point ul = getCellUL(row, col);
				
				if (sameDay(_selected, current))
				{
					Color old = g.getColor();
					g.setColor(Color.YELLOW);
					g.fillRect(ul.x, ul.y, _cellWidth, _cellHeight);
					g.setColor(old);
				}
				
				if (sameDay(_today, current))
				{
					Color old = g.getColor();
					g.setColor(Color.RED);
					g.drawRect(ul.x, ul.y, _cellWidth, _cellHeight);
					g.setColor(old);
				}
				
				ignore = false;
				if (!inReal && current.equals(firstDayOfMonth))
				{
					inReal = true;
					ignore = true;
					g.setFont(f);
					g.setColor(Color.BLACK);
				}
				
				if (!ignore && inReal && 
					current.get(Calendar.DAY_OF_MONTH) == 1)
				{
					g.setFont(f.deriveFont(Font.ITALIC));
					g.setColor(Color.GRAY);
					inReal = false;
				}
				
				String str = Integer.toString(current.get(Calendar.DAY_OF_MONTH));
				paintCenteredString(g, metrics, ul, _cellWidth, _cellHeight, str);
				
				rollDay(current, 1);
			}
		}
	}
	
	public Calendar getDate(int x, int y)
	{
		int row = y / _cellHeight;
		int col = x / _cellWidth;
		
		if (row < 2 || row >= (_CELLS_PER_COL - 1))
			return null;
		
		int daysOff = _CELLS_PER_ROW * (row - 2) + col;
		Calendar ret = (Calendar)_firstDisplayedDate.clone();
		rollDay(ret, daysOff);
		return ret;
	}
}
