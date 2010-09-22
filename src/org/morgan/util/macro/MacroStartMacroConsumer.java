package org.morgan.util.macro;

class MacroStartMacroConsumer extends AbstractEscapingMacroConsumer
{
	private StringBuilder _macro = new StringBuilder();
	
	MacroStartMacroConsumer(MacroResolver resolver)
	{
		super(resolver);
	}
	
	@Override
	protected MacroConsumer consumeImpl(StringBuilder builder, Character c,
		boolean escaped)
	{
		if (c == null)
		{
			builder.append("${" + _macro);
			return null;
		}
		
		if (escaped || c != '}')
		{
			_macro.append(c);
			return this;
		}
		
		String value = lookup(_macro.toString());
		if (value == null)
			value = "";
		
		builder.append(value);
		return new DefaultMacroConsumer(resolver());
	}
}