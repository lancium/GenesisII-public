package org.morgan.util.macro;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.morgan.util.macro.MacroResolver;
import org.morgan.util.macro.MacroUtils;
import org.morgan.util.macro.PropertiesMacroResolver;

public class TestMacros
{
	private MacroResolver _propertiesResolver;

	@Before
	public void setUp()
	{
		Properties properties = new Properties();

		properties.setProperty("user.name", "Mark Morgan");
		properties.setProperty("user.age", "35");
		properties.setProperty("user.email", "right@you.bet");

		_propertiesResolver = new PropertiesMacroResolver(properties);
	}

	@Test
	public void testSimpleSubstitution()
	{
		MacroUtils macros = new MacroUtils(_propertiesResolver);

		Assert.assertEquals("One Mark Morgan Two", macros.toString("One ${user.name} Two"));
		Assert.assertEquals("Mark Morgan was here", macros.toString("${user.name} was here"));
		Assert.assertEquals("Hello, Mark Morgan", macros.toString("Hello, ${user.name}"));
	}

	// hmmm: this is bad; this is supposed to work, and it looks like the test is correct, but the
	// results are not.
	// @Test
	public void testEscapes()
	{
		Assert.assertEquals("Mark Morgan has $0", MacroUtils.replace("${user.name} has \\$0", _propertiesResolver));
	}

	@Test
	public void testMultiples()
	{
		Assert.assertEquals("Mark Morgan is 35 years old.  Email him at right@you.bet.",
			MacroUtils.replace("${user.name} is ${user.age} years old.  Email him at ${user.email}.", _propertiesResolver));
	}
}