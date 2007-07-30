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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public final class BasicTriangleArrowIcon implements Icon
{
	private int _dimension;
	private Polygon _triangle;
	
	public BasicTriangleArrowIcon(ArrowDirection direction, int sideLength)
	{
		if (sideLength < 1)
			throw new IllegalArgumentException("Side length must be positive.");
		
		int halfLength = sideLength / 2;
		int weirdDimension = 
			(int)Math.sqrt(sideLength * sideLength - halfLength *halfLength);
		_dimension = sideLength;
		
		_triangle = new Polygon();
		
		if (direction == ArrowDirection.DOWN)
		{
			_triangle.addPoint(0, _dimension - 1);
			_triangle.addPoint(halfLength, sideLength - weirdDimension);
			_triangle.addPoint(_dimension -1, _dimension - 1);
		} else if (direction == ArrowDirection.UP)
		{
			_triangle.addPoint(0, 0);
			_triangle.addPoint(_dimension - 1, 0);
			_triangle.addPoint(halfLength, weirdDimension);
		} else if (direction == ArrowDirection.LEFT)
		{
			_triangle.addPoint(_dimension - weirdDimension, halfLength);
			_triangle.addPoint(_dimension - 1, 0);
			_triangle.addPoint(_dimension - 1, _dimension - 1);
		} else
		{
			_triangle.addPoint(0, 0);
			_triangle.addPoint(weirdDimension, halfLength);
			_triangle.addPoint(0, _dimension - 1);
		}
	}
	
	public void paintIcon(Component target, Graphics g, int x, int y)
	{
		g.setColor(target.getForeground());
		
		g.translate(x, y);
		g.fillPolygon(_triangle);
		g.translate(-1 * x, -1 * y);
	}

	public int getIconWidth()
	{
		return _dimension;
	}

	public int getIconHeight()
	{
		return _dimension;
	}
	
	static public void main(String []args) throws Exception
	{
		JDialog dialog = new JDialog();
		dialog.getContentPane().setLayout(new GridBagLayout());
		
		int width = 2;
		for (int line = 0; line < 4; line++)
		{
			width = (int)(width * 1.8);
			dialog.getContentPane().add(
				new JButton(new BasicTriangleArrowIcon(ArrowDirection.LEFT, width)),
				new GridBagConstraints(0, line, 1, 1, 1.0, 1.0, 
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(5, 5, 5, 5), 5, 5));
			dialog.getContentPane().add(
					new JButton(new BasicTriangleArrowIcon(ArrowDirection.LEFT, width)),
					new GridBagConstraints(1, line, 1, 1, 1.0, 1.0, 
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 5, 5));
			dialog.getContentPane().add(
					new JButton(new BasicTriangleArrowIcon(ArrowDirection.LEFT, width)),
					new GridBagConstraints(2, line, 1, 1, 1.0, 1.0, 
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 5, 5));
			dialog.getContentPane().add(
					new JButton(new BasicTriangleArrowIcon(ArrowDirection.LEFT, width)),
					new GridBagConstraints(3, line, 1, 1, 1.0, 1.0, 
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 5, 5));
		}
		
		dialog.pack();
		dialog.setVisible(true);
	}
}
