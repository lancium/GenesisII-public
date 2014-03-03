package org.morgan.dpage;

class PageContextDescription
{
	private String _context;
	private String _resourceBase;
	private ObjectInjectionHandlerFactory _injectionHandlerFactory;

	@SuppressWarnings("unchecked")
	PageContextDescription(String context, String resourceBase, String injectionHandlerFactoryClassName)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		_context = context;
		_resourceBase = resourceBase;

		Class<? extends ObjectInjectionHandlerFactory> factoryClass =
			(Class<? extends ObjectInjectionHandlerFactory>) Thread.currentThread().getContextClassLoader()
				.loadClass(injectionHandlerFactoryClassName);
		_injectionHandlerFactory = factoryClass.newInstance();
	}

	final String context()
	{
		return _context;
	}

	final String resourceBase()
	{
		return _resourceBase;
	}

	final ObjectInjectionHandlerFactory injectionHandlerFactory()
	{
		return _injectionHandlerFactory;
	}
}