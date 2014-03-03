package org.morgan.util.macro;

interface MacroConsumer
{
	public MacroConsumer consume(StringBuilder builder, Character c);
}