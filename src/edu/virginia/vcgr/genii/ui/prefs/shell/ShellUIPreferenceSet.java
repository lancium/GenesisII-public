package edu.virginia.vcgr.genii.ui.prefs.shell;

import java.awt.Font;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.ui.prefs.AbstractUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.shell.InputBindings;
import edu.virginia.vcgr.genii.ui.shell.InputBindingsType;

public class ShellUIPreferenceSet extends AbstractUIPreferenceSet
{
	static final private String PREFERENCE_SET_TITLE = "Shell";
	static final private String PREFERENCE_NODE_NAME = "shell";
	
	static final private String BINDINGS_KEY = "bindings";
	
	static final private String FONT_FAMILY_KEY = "font-family";
	static final private String FONT_STYLE_KEY = "font-style";
	static final private String FONT_SIZE_KEY = "font-size";
	
	static final private String DEFAULT_FONT_FAMILY = "Courier New";
	static final private int DEFAULT_FONT_STYLE = Font.PLAIN;
	static final private int DEFAULT_FONT_SIZE = 12;
	
	private InputBindingsType _bindings;
	private Font _font;
	
	@Override
	protected Preferences preferenceNode(Preferences uiPreferencesRoot)
	{
		return uiPreferencesRoot.node(PREFERENCE_NODE_NAME);
	}

	@Override
	protected void loadImpl(Preferences prefNode)
	{
		String bindingsName = prefNode.get(
			BINDINGS_KEY, InputBindingsType.defaultBindings().name());
		_bindings = InputBindingsType.valueOf(bindingsName);
		
		String fontFamily = prefNode.get(
			FONT_FAMILY_KEY, DEFAULT_FONT_FAMILY);
		int fontStyle = prefNode.getInt(
			FONT_STYLE_KEY, DEFAULT_FONT_STYLE);
		int fontSize = prefNode.getInt(
			FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
		
		_font = new Font(fontFamily, fontStyle, fontSize);
	}

	@Override
	protected void storeImpl(Preferences prefNode)
	{
		prefNode.put(BINDINGS_KEY, _bindings.name());
		
		prefNode.put(FONT_FAMILY_KEY, _font.getFamily());
		prefNode.putInt(FONT_STYLE_KEY, _font.getStyle());
		prefNode.putInt(FONT_SIZE_KEY, _font.getSize());
	}
	
	InputBindingsType inputBindingsType()
	{
		return _bindings;
	}
	
	public ShellUIPreferenceSet()
	{
		super(PREFERENCE_SET_TITLE);
		
		_bindings = InputBindingsType.defaultBindings();
	}

	@Override
	public JPanel createEditor()
	{
		return new ShellUIPreferenceSetEditor(this);
	}

	@Override
	public void load(JPanel editor)
	{
		_bindings = 
			((ShellUIPreferenceSetEditor)editor).selectedBindingsType();
		_font =
			((ShellUIPreferenceSetEditor)editor).selectedFont();
	}
	
	final public InputBindings createBindings()
	{
		return _bindings.createBindings();
	}
	
	final public Font shellFont()
	{
		return _font;
	}
}