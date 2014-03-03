package test.org.morgan.inject;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.morgan.inject.MInject;
import org.morgan.inject.MInjector;
import org.morgan.inject.MNamingInjectionResolver;
import org.morgan.mnaming.InitialMNamingContext;

@SuppressWarnings("deprecation")
public class TestInjection {
	private InitialMNamingContext _context = new InitialMNamingContext();

	private MInjector _injector = new MInjector(new MNamingInjectionResolver());

	@MInject(name = "mem:name")
	private String _field;

	private String _method;

	@MInject(name = "mem:name")
	private void setMethod(String value) {
		_method = value;
	}

	@MInject(recursive = true)
	private SentenceGenerator _generator = new SentenceGenerator();

	@Before
	public void setUp() throws Exception {
		_context.bind("mem:name", "Mark");
		_injector.inject(this);
	}

	@After
	public void tearDown() throws Exception {
		_context.clearAll();
	}

	@Test
	public void testField() throws Exception {
		Assert.assertEquals("Mark", _field);
	}

	@Test
	public void testMethod() throws Exception {
		Assert.assertEquals("Mark", _method);
	}

	@Test
	public void testRecursive() throws Exception {
		Assert.assertEquals("Hello, Mark!", _generator.toString());
	}
}
