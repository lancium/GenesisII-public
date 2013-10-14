package edu.virginia.vcgr.genii.gjt.gui.icons;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;

import edu.virginia.vcgr.genii.gjt.gui.GuiConstants;
import edu.virginia.vcgr.genii.gjt.gui.util.TurtleDraw;

public enum Shapes {
	Plus(createPlusShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, GuiConstants.SIMPLE_ICON_BUTTON_WIDTH)),
	Minus(createMinusShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, GuiConstants.SIMPLE_ICON_BUTTON_WIDTH)),
	UpArrow(createArrowShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, ArrowDirection.North)),
	RightArrow(createArrowShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, ArrowDirection.East)),
	DownArrow(createArrowShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, ArrowDirection.South)),
	LeftArrow(createArrowShape(GuiConstants.SIMPLE_ICON_BUTTON_SIZE, ArrowDirection.West));

	private Shape _shape;

	private Shapes(Shape shape)
	{
		_shape = shape;
	}

	public Shape shape()
	{
		return _shape;
	}

	static public Shape createMinusShape(int size, int lineWidth)
	{
		return new Rectangle(0, (size - lineWidth) / 2, size, lineWidth);
	}

	static public Shape createPlusShape(int size, int lineWidth)
	{
		Area minusArea = new Area(createMinusShape(size, lineWidth));
		Area verticalArea = new Area(new Rectangle((size - lineWidth) / 2, 0, lineWidth, size));
		minusArea.add(verticalArea);
		return minusArea;
	}

	static public Shape createArrowShape(int size, ArrowDirection direction)
	{
		TurtleDraw stylus = null;

		if (direction == ArrowDirection.North) {
			stylus = new TurtleDraw(size / 2, 0);
			stylus.moveTo(size - 1, size - 1);
			stylus.moveTo(0, size - 1);
		} else if (direction == ArrowDirection.East) {
			stylus = new TurtleDraw(size - 1, size / 2);
			stylus.moveTo(0, size - 1);
			stylus.moveTo(0, 0);
		} else if (direction == ArrowDirection.South) {
			stylus = new TurtleDraw(size / 2, size - 1);
			stylus.moveTo(0, 0);
			stylus.moveTo(size - 1, 0);
		} else {
			stylus = new TurtleDraw(0, size / 2);
			stylus.moveTo(size - 1, 0);
			stylus.moveTo(size - 1, size - 1);
		}

		return stylus.polygon();
	}
}