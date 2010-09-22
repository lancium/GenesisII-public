package org.morgan.util.macro;

abstract class AbstractEscapingMacroConsumer extends AbstractMacroConsumer
{
	private boolean _escaped = false;
	
	protected abstract MacroConsumer consumeImpl(StringBuilder builder,
		Character c, boolean escaped);
	
	protected AbstractEscapingMacroConsumer(MacroResolver resolver)
	{
		super(resolver);
	}

	@Override
	public MacroConsumer consume(StringBuilder builder, Character c)
	{
		if (_escaped)
		{
			_escaped = false;
			return consumeImpl(builder, c, true);
		} else if (c != null && c == '\\')
		{
			_escaped = true;
			return this;
		} else
			return consumeImpl(builder, c, false);
	}
}