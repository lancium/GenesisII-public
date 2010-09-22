package org.morgan.util.gui.font;

import java.awt.Font;

public class DefaultFontModel extends AbstractFontModel
{
	private Font _selectedFont;
	private int _styleMask;
	private int _minimumSize;
	private int _maximumSize;
	
	public DefaultFontModel(Font selectedFont)
	{
		if (selectedFont == null)
			selectedFont = FontConstants.DEFAULT_FONT;
		
		_selectedFont = selectedFont;
		
		_styleMask = Font.BOLD | Font.ITALIC;
		_minimumSize = 4;
		_maximumSize = 48;
	}
	
	public DefaultFontModel()
	{
		this(null);
	}
	
	@Override
	public Font selectedFont()
	{
		return _selectedFont;
	}
	
	@Override
	public void setStyle(int fontStyle)
	{
		_selectedFont = _selectedFont.deriveFont(
			_selectedFont.getStyle() | fontStyle);
		fireFontChanged(_selectedFont);
	}
	
	@Override
	public void clearStyle(int fontStyle)
	{
		_selectedFont = _selectedFont.deriveFont(
			_selectedFont.getStyle() & ~fontStyle);
		fireFontChanged(_selectedFont);
	}
	
	@Override
	public void setSize(int newSize)
	{
		_selectedFont = _selectedFont.deriveFont((float)newSize);
		fireFontChanged(_selectedFont);
	}
	
	@Override
	public void setFamily(String newFamily)
	{
		_selectedFont = new Font(newFamily,
			_selectedFont.getStyle(), _selectedFont.getSize());
		fireFontChanged(_selectedFont);
	}

	@Override
	public int styleMask()
	{
		return _styleMask;
	}

	@Override
	public int minimumSize()
	{
		return _minimumSize;
	}

	@Override
	public int maximumSize()
	{
		return _maximumSize;
	}
}