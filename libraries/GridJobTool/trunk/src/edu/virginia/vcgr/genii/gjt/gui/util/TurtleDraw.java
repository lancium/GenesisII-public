package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Polygon;

public class TurtleDraw
{
	private int _lastX;
	private int _lastY;

	private Polygon _ret = new Polygon();

	public TurtleDraw(int startX, int startY)
	{
		moveTo(startX, startY);
	}

	public void moveTo(int x, int y)
	{
		_ret.addPoint(_lastX = x, _lastY = y);
	}

	public void move(int xDelta, int yDelta)
	{
		moveTo(_lastX + xDelta, _lastY + yDelta);
	}

	public int x()
	{
		return _lastX;
	}

	public int y()
	{
		return _lastY;
	}

	public Polygon polygon()
	{
		return _ret;
	}
}