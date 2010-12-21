package org.morgan.util.macro;

class DefaultMacroConsumer extends AbstractEscapingMacroConsumer
{
	@Override
	protected MacroConsumer consumeImpl(StringBuilder builder, Character c,
		boolean escaped)
	{
		if (c != null)
		{
			if (!escaped && c == '$')
				return new PossibleMacroStartMacroConsumer(resolver());
			
			if (escaped)
				builder.append('\\');
			
			builder.append(c);
		}
		
		return this;
	}
	
	public DefaultMacroConsumer(MacroResolver resolver)
	{
		super(resolver);
	}
}