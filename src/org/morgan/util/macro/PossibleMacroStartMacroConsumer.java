package org.morgan.util.macro;

class PossibleMacroStartMacroConsumer extends AbstractMacroConsumer
{
	PossibleMacroStartMacroConsumer(MacroResolver resolver)
	{
		super(resolver);
	}
	
	@Override
	final public MacroConsumer consume(StringBuilder builder, Character c)
	{
		if (c == null)
		{
			builder.append('$');
			return null;
		} else if (c == '{')
			return new MacroStartMacroConsumer(resolver());
		else
		{
			builder.append('$');
			DefaultMacroConsumer consumer = new DefaultMacroConsumer(resolver());
			return consumer.consume(builder, c);
		}
	}
}